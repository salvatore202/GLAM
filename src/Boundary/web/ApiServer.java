package Boundary.web;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Boundary.web.ApiServer - nuovo layer "Boundary" (nel senso dell'architettura
 * BCE descritta nel README del progetto) che espone via HTTP/JSON le stesse
 * Use Case gia' implementate in Control.GestioneLezioni, finora raggiungibili
 * solo da BoundaryMaestro/BoundaryStudenteRegistrato (CLI a menu con
 * Scanner). Non sostituisce la CLI: la affianca. Nessun file esistente del
 * progetto e' stato modificato per aggiungere questa classe.
 *
 * Costruito solo con classi del JDK (com.sun.net.httpserver, incluso da
 * Java 6 in poi) per restare fedele allo stile "solo javac, senza build
 * tool" del resto del progetto, e per evitare di dover scaricare dipendenze
 * Maven sia qui che nella pipeline di GitHub Actions.
 *
 * Esecuzione:
 *   javac -d out $(find src/Boundary src/Control src/DAO src/Entity src/Exception -name "*.java")
 *   java -cp "out:src/lib/mysql-connector-j-8.3.0.jar" Boundary.web.ApiServer
 *
 * Porta configurabile con la variabile d'ambiente PORT (default 8080).
 */
public class ApiServer
{
    public static void main(String[] args) throws IOException
    {
        int port = 8080;
        String portEnv = System.getenv("PORT");
        if (portEnv != null && !portEnv.isBlank())
        {
            try { port = Integer.parseInt(portEnv.trim()); }
            catch (NumberFormatException ignored) { /* usa la porta di default */ }
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        Router router = new Router();
        router.add("GET", "/api/health", ApiServer::health);
        CatalogoRoutes.register(router);
        AuthRoutes.register(router);
        MaestroRoutes.register(router);
        StudenteRoutes.register(router);

        var context = server.createContext("/", router);
        context.getFilters().add(corsFilter());

        // NB: eseguiamo le richieste in sequenza (executor single-thread), non in
        // parallelo. DAO.DBManager (classe originale, non modificata) tiene una
        // singola Connection statica condivisa e la chiude in ogni "finally" di
        // ogni chiamata DAO: sotto richieste concorrenti su thread diversi questo
        // produrrebbe "Connection is closed" in modo intermittente. La CLI
        // originale e' comunque single-threaded (un solo Scanner, un solo menu),
        // quindi processare le richieste HTTP in sequenza rispetta esattamente le
        // stesse assunzioni di concorrenza del codice esistente, senza doverlo
        // modificare. Per un uso con piu' utenti reali in contemporanea, DBManager
        // andrebbe riscritto con un connection pool.
        server.setExecutor(Executors.newSingleThreadExecutor());

        server.start();
        System.out.println("GLAM API in ascolto sulla porta " + port);
    }

    private static void health(Router.RequestContext ctx)
    {
        boolean dbOk;
        try
        {
            DAO.DBManager.getConnection();
            dbOk = true;
        }
        catch (SQLException e)
        {
            dbOk = false;
        }
        finally
        {
            try { DAO.DBManager.closeConnection(); } catch (SQLException ignored) {}
        }
        ctx.sendJson(dbOk ? 200 : 503, Map.of("status", dbOk ? "ok" : "db_unreachable"));
    }

    private static Filter corsFilter()
    {
        return new Filter()
        {
            @Override
            public String description() { return "CORS"; }

            @Override
            public void doFilter(HttpExchange exchange, Chain chain) throws IOException
            {
                String origin = exchange.getRequestHeaders().getFirst("Origin");
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", origin != null ? origin : "*");
                exchange.getResponseHeaders().add("Vary", "Origin");
                exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
                exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, X-Username, X-Password, X-Email");

                if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod()))
                {
                    exchange.sendResponseHeaders(204, -1);
                    exchange.close();
                    return;
                }
                chain.doFilter(exchange);
            }
        };
    }
}
