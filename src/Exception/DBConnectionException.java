package Exception;

public class DBConnectionException extends Exception
{
    public DBConnectionException() {}

    public DBConnectionException(String msg) {
        super(msg);
    }
}
