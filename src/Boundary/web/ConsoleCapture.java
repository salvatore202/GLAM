package Boundary.web;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Control.GestioneLezioni (classe originale, non modificata) comunica l'esito
 * delle operazioni SOLO stampando su console (System.out/System.err): i suoi
 * metodi sono void e inghiottono internamente le eccezioni. Per una CLI questo
 * va benissimo; per una risposta HTTP no, perche' il client non vede la
 * console del server.
 *
 * Questa classe cattura temporaneamente System.out/System.err durante la
 * chiamata a un metodo della Control originale, cosi' il testo esatto che il
 * backend avrebbe stampato nel terminale puo' essere restituito nella
 * risposta JSON (utile anche per mostrare, nella UI, che le operazioni
 * passano davvero dal codice Java originale). Il layer web usa
 * l'HttpServer con executor sequenziale di default (vedi ApiServer), quindi
 * non ci sono richieste concorrenti che si contendono System.out/err.
 */
public final class ConsoleCapture
{
    private ConsoleCapture() {}

    public record Result<T>(T value, String output) {}

    public static <T> Result<T> capture(java.util.concurrent.Callable<T> action) throws Exception
    {
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream captured = new PrintStream(buffer, true, StandardCharsets.UTF_8);

        System.setOut(captured);
        System.setErr(captured);
        try
        {
            T value = action.call();
            captured.flush();
            return new Result<>(value, buffer.toString(StandardCharsets.UTF_8).trim());
        }
        finally
        {
            System.setOut(originalOut);
            System.setErr(originalErr);
        }
    }

    public static Result<Void> captureVoid(Runnable action) throws Exception
    {
        return capture(() -> { action.run(); return null; });
    }
}
