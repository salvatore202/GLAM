package Boundary.web;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Codificatore/decodificatore JSON scritto a mano.
 *
 * Il progetto originale non usa nessun tool di build (ne' Maven ne' Gradle):
 * le dipendenze sono jar copiati a mano in src/lib. Aggiungere una libreria
 * JSON (es. Jackson/Gson) avrebbe richiesto un repository Maven non
 * raggiungibile da questo ambiente e avrebbe complicato la build su
 * GitHub Actions. Questa classe copre esattamente cio' che serve alle rotte
 * REST: oggetti/liste/stringhe/numeri/booleani/null, senza dipendenze esterne.
 */
public final class Json
{
    private Json() {}

    // ---------- ENCODE ----------

    public static String encode(Object value)
    {
        StringBuilder sb = new StringBuilder();
        write(sb, value);
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private static void write(StringBuilder sb, Object value)
    {
        if (value == null)
        {
            sb.append("null");
        }
        else if (value instanceof String s)
        {
            writeString(sb, s);
        }
        else if (value instanceof Boolean b)
        {
            sb.append(b.toString());
        }
        else if (value instanceof Number n)
        {
            sb.append(n.toString());
        }
        else if (value instanceof Map<?, ?> map)
        {
            sb.append('{');
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet())
            {
                if (!first) sb.append(',');
                first = false;
                writeString(sb, String.valueOf(entry.getKey()));
                sb.append(':');
                write(sb, entry.getValue());
            }
            sb.append('}');
        }
        else if (value instanceof Iterable<?> iterable)
        {
            sb.append('[');
            boolean first = true;
            for (Object item : iterable)
            {
                if (!first) sb.append(',');
                first = false;
                write(sb, item);
            }
            sb.append(']');
        }
        else if (value instanceof Object[] arr)
        {
            sb.append('[');
            for (int i = 0; i < arr.length; i++)
            {
                if (i > 0) sb.append(',');
                write(sb, arr[i]);
            }
            sb.append(']');
        }
        else
        {
            // fallback di sicurezza: qualsiasi altro tipo viene reso come stringa
            writeString(sb, value.toString());
        }
    }

    private static void writeString(StringBuilder sb, String s)
    {
        sb.append('"');
        for (int i = 0; i < s.length(); i++)
        {
            char c = s.charAt(i);
            switch (c)
            {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20)
                        sb.append(String.format("\\u%04x", (int) c));
                    else
                        sb.append(c);
                }
            }
        }
        sb.append('"');
    }

    // ---------- DECODE ----------

    /** Esegue il parse di un body JSON in Map / List / String / Double / Boolean / null. */
    public static Object decode(String json)
    {
        if (json == null || json.isBlank())
            return new LinkedHashMap<String, Object>();
        Parser p = new Parser(json);
        p.skipWhitespace();
        Object result = p.parseValue();
        return result;
    }

    /** Comodo per le rotte: interpreta il body come oggetto {chiave: valore}. */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> decodeObject(String json)
    {
        Object value = decode(json);
        if (value instanceof Map)
            return (Map<String, Object>) value;
        return new LinkedHashMap<>();
    }

    private static final class Parser
    {
        private final String s;
        private int i = 0;

        Parser(String s) { this.s = s; }

        void skipWhitespace()
        {
            while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
        }

        char peek()
        {
            if (i >= s.length()) throw new IllegalArgumentException("JSON troncato in posizione " + i);
            return s.charAt(i);
        }

        Object parseValue()
        {
            skipWhitespace();
            char c = peek();
            return switch (c)
            {
                case '{' -> parseObject();
                case '[' -> parseArray();
                case '"' -> parseString();
                case 't', 'f' -> parseBoolean();
                case 'n' -> parseNull();
                default -> parseNumber();
            };
        }

        Map<String, Object> parseObject()
        {
            Map<String, Object> map = new LinkedHashMap<>();
            i++; // {
            skipWhitespace();
            if (peek() == '}') { i++; return map; }
            while (true)
            {
                skipWhitespace();
                String key = parseString();
                skipWhitespace();
                if (peek() != ':') throw new IllegalArgumentException("Atteso ':' in posizione " + i);
                i++;
                Object value = parseValue();
                map.put(key, value);
                skipWhitespace();
                char c = peek();
                if (c == ',') { i++; continue; }
                if (c == '}') { i++; break; }
                throw new IllegalArgumentException("Atteso ',' o '}' in posizione " + i);
            }
            return map;
        }

        List<Object> parseArray()
        {
            List<Object> list = new ArrayList<>();
            i++; // [
            skipWhitespace();
            if (peek() == ']') { i++; return list; }
            while (true)
            {
                Object value = parseValue();
                list.add(value);
                skipWhitespace();
                char c = peek();
                if (c == ',') { i++; continue; }
                if (c == ']') { i++; break; }
                throw new IllegalArgumentException("Atteso ',' o ']' in posizione " + i);
            }
            return list;
        }

        String parseString()
        {
            if (peek() != '"') throw new IllegalArgumentException("Attesa stringa in posizione " + i);
            i++;
            StringBuilder sb = new StringBuilder();
            while (true)
            {
                char c = s.charAt(i++);
                if (c == '"') break;
                if (c == '\\')
                {
                    char esc = s.charAt(i++);
                    switch (esc)
                    {
                        case '"' -> sb.append('"');
                        case '\\' -> sb.append('\\');
                        case '/' -> sb.append('/');
                        case 'b' -> sb.append('\b');
                        case 'f' -> sb.append('\f');
                        case 'n' -> sb.append('\n');
                        case 'r' -> sb.append('\r');
                        case 't' -> sb.append('\t');
                        case 'u' -> {
                            String hex = s.substring(i, i + 4);
                            sb.append((char) Integer.parseInt(hex, 16));
                            i += 4;
                        }
                        default -> throw new IllegalArgumentException("Escape JSON non valido: \\" + esc);
                    }
                }
                else
                {
                    sb.append(c);
                }
            }
            return sb.toString();
        }

        Boolean parseBoolean()
        {
            if (s.startsWith("true", i)) { i += 4; return Boolean.TRUE; }
            if (s.startsWith("false", i)) { i += 5; return Boolean.FALSE; }
            throw new IllegalArgumentException("Booleano non valido in posizione " + i);
        }

        Object parseNull()
        {
            if (s.startsWith("null", i)) { i += 4; return null; }
            throw new IllegalArgumentException("Token non valido in posizione " + i);
        }

        Object parseNumber()
        {
            int start = i;
            if (peek() == '-') i++;
            while (i < s.length() && Character.isDigit(s.charAt(i))) i++;
            boolean isDouble = false;
            if (i < s.length() && s.charAt(i) == '.')
            {
                isDouble = true;
                i++;
                while (i < s.length() && Character.isDigit(s.charAt(i))) i++;
            }
            if (i < s.length() && (s.charAt(i) == 'e' || s.charAt(i) == 'E'))
            {
                isDouble = true;
                i++;
                if (i < s.length() && (s.charAt(i) == '+' || s.charAt(i) == '-')) i++;
                while (i < s.length() && Character.isDigit(s.charAt(i))) i++;
            }
            String token = s.substring(start, i);
            if (token.isEmpty() || token.equals("-"))
                throw new IllegalArgumentException("Numero non valido in posizione " + start);
            return isDouble ? (Object) Double.parseDouble(token) : (Object) Integer.parseInt(token);
        }
    }
}
