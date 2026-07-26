package Boundary.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Router HTTP minimale sopra com.sun.net.httpserver (incluso nel JDK).
 *
 * Il progetto originale non usa Maven/Gradle, quindi introdurre un framework
 * web (Spring, Javalin, ecc.) avrebbe richiesto accesso a Maven Central sia
 * qui che, soprattutto, in GitHub Actions - con relativo rischio di rotture
 * di rete/versioni. com.sun.net.httpserver e' incluso nel JDK stesso: zero
 * dipendenze nuove, stessa filosofia "solo javac" del resto del progetto.
 *
 * Supporta path parameter in stile "/api/maestri/{id}/disponibilita".
 */
public final class Router implements HttpHandler
{
    @FunctionalInterface
    public interface RouteHandler
    {
        void handle(RequestContext ctx) throws Exception;
    }

    private record Route(String method, Pattern pattern, List<String> paramNames, RouteHandler handler) {}

    private final List<Route> routes = new ArrayList<>();

    public void add(String method, String pathTemplate, RouteHandler handler)
    {
        List<String> paramNames = new ArrayList<>();
        StringBuilder regex = new StringBuilder("^");
        for (String segment : pathTemplate.split("/", -1))
        {
            if (segment.isEmpty()) continue;
            regex.append('/');
            if (segment.startsWith("{") && segment.endsWith("}"))
            {
                paramNames.add(segment.substring(1, segment.length() - 1));
                regex.append("([^/]+)");
            }
            else
            {
                regex.append(Pattern.quote(segment));
            }
        }
        regex.append("/?$");
        routes.add(new Route(method.toUpperCase(), Pattern.compile(regex.toString()), paramNames, handler));
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException
    {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        // Il preflight CORS e' gia' gestito dal Filter globale in ApiServer,
        // ma rispondiamo comunque per sicurezza se arriva fin qui.
        if ("OPTIONS".equalsIgnoreCase(method))
        {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        for (Route route : routes)
        {
            if (!route.method().equals(method)) continue;
            Matcher m = route.pattern().matcher(path);
            if (!m.matches()) continue;

            Map<String, String> pathParams = new LinkedHashMap<>();
            for (int idx = 0; idx < route.paramNames().size(); idx++)
            {
                pathParams.put(route.paramNames().get(idx), urlDecode(m.group(idx + 1)));
            }

            RequestContext ctx = new RequestContext(exchange, pathParams);
            try
            {
                route.handler().handle(ctx);
            }
            catch (ApiException e)
            {
                ctx.sendJson(e.getStatusCode(), Map.of("successo", false, "errore", e.getMessage()));
            }
            catch (Exception e)
            {
                ctx.sendJson(500, Map.of("successo", false, "errore", "Errore interno: " + e.getMessage()));
            }
            finally
            {
                exchange.close();
            }
            return;
        }

        // nessuna rotta corrispondente
        String body = Json.encode(Map.of("successo", false, "errore", "Rotta non trovata: " + method + " " + path));
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(404, bytes.length);
        try (var os = exchange.getResponseBody()) { os.write(bytes); }
        exchange.close();
    }

    private static String urlDecode(String s)
    {
        return URLDecoder.decode(s, StandardCharsets.UTF_8);
    }

    /** Wrapper di comodo attorno a HttpExchange: query string, body JSON, risposta JSON. */
    public static final class RequestContext
    {
        private final HttpExchange exchange;
        private final Map<String, String> pathParams;
        private Map<String, String> queryParams;
        private Map<String, Object> bodyCache;

        RequestContext(HttpExchange exchange, Map<String, String> pathParams)
        {
            this.exchange = exchange;
            this.pathParams = pathParams;
        }

        public String pathParam(String name) { return pathParams.get(name); }

        public String queryParam(String name)
        {
            if (queryParams == null) queryParams = parseQuery(exchange.getRequestURI().getRawQuery());
            return queryParams.get(name);
        }

        /** Header HTTP (case-insensitive, gestito da HttpExchange). Usato per le credenziali sulle GET, per non metterle in query string. */
        public String header(String name)
        {
            return exchange.getRequestHeaders().getFirst(name);
        }

        public Map<String, Object> jsonBody() throws IOException
        {
            if (bodyCache == null)
            {
                String raw = readBody(exchange.getRequestBody());
                bodyCache = Json.decodeObject(raw);
            }
            return bodyCache;
        }

        public String requireString(Map<String, Object> body, String key)
        {
            Object v = body.get(key);
            if (v == null || String.valueOf(v).isBlank())
                throw ApiException.badRequest("Campo obbligatorio mancante: " + key);
            return String.valueOf(v);
        }

        public String optionalString(Map<String, Object> body, String key, String fallback)
        {
            Object v = body.get(key);
            return v == null ? fallback : String.valueOf(v);
        }

        public void sendJson(int statusCode, Object data)
        {
            try
            {
                String body = Json.encode(data);
                byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
                exchange.sendResponseHeaders(statusCode, bytes.length);
                try (var os = exchange.getResponseBody()) { os.write(bytes); }
            }
            catch (IOException e)
            {
                // la connessione e' stata chiusa dal client: non c'e' altro da fare qui
            }
        }

        private static String readBody(InputStream in) throws IOException
        {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[4096];
            int read;
            while ((read = in.read(chunk)) != -1) buffer.write(chunk, 0, read);
            return buffer.toString(StandardCharsets.UTF_8);
        }

        private static Map<String, String> parseQuery(String rawQuery)
        {
            Map<String, String> map = new LinkedHashMap<>();
            if (rawQuery == null || rawQuery.isBlank()) return map;
            for (String pair : rawQuery.split("&"))
            {
                int eq = pair.indexOf('=');
                if (eq < 0)
                    map.put(urlDecode(pair), "");
                else
                    map.put(urlDecode(pair.substring(0, eq)), urlDecode(pair.substring(eq + 1)));
            }
            return map;
        }
    }
}
