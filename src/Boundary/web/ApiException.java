package Boundary.web;

/**
 * Eccezione "di controllo" usata dalle rotte REST per restituire una risposta
 * di errore pulita (codice HTTP + messaggio JSON) invece di uno stack trace.
 * Riguarda solo il nuovo layer web: non sostituisce ne' modifica le eccezioni
 * originali del progetto (DAOException, DBConnectionException, OperationException).
 */
public class ApiException extends RuntimeException
{
    private final int statusCode;

    public ApiException(int statusCode, String message)
    {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode()
    {
        return statusCode;
    }

    public static ApiException badRequest(String message) { return new ApiException(400, message); }
    public static ApiException unauthorized(String message) { return new ApiException(401, message); }
    public static ApiException notFound(String message) { return new ApiException(404, message); }
    public static ApiException conflict(String message) { return new ApiException(409, message); }
    public static ApiException serverError(String message) { return new ApiException(500, message); }
}
