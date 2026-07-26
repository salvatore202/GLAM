package DAO;

import Entity.Strumento;
import Exception.DAOException;
import Exception.DBConnectionException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * DAO aggiuntivo (non presente nel progetto originale), di sola lettura,
 * per la tabella STRUMENTO. Segue esattamente le convenzioni delle altre
 * classi DAO del progetto (stessa gestione delle eccezioni, stesso uso di
 * DBManager) e non modifica nessun file esistente.
 */
public class StrumentoDAO
{
    // READ - tutti gli strumenti insegnati, con relativo Maestro
    public static ArrayList<Strumento> readAll() throws DAOException, DBConnectionException
    {
        ArrayList<Strumento> strumenti = new ArrayList<>();

        try
        {
            Connection conn = DBManager.getConnection();

            String query = "SELECT * FROM STRUMENTO ORDER BY NOME;";

            try
            {
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet result = stmt.executeQuery();

                while (result.next())
                {
                    strumenti.add(new Strumento(result.getInt(1), result.getString(2), result.getInt(3)));
                }
            }
            catch (SQLException e)
            {
                throw new DAOException("Errore lettura Strumento: " + e.getMessage());
            }
            finally
            {
                DBManager.closeConnection();
            }
        }
        catch (SQLException e)
        {
            throw new DBConnectionException("Errore di connessione DB");
        }

        return strumenti;
    }

    // READ - strumenti insegnati da un determinato Maestro
    public static ArrayList<Strumento> readByMaestro(int idMaestro) throws DAOException, DBConnectionException
    {
        ArrayList<Strumento> strumenti = new ArrayList<>();

        try
        {
            Connection conn = DBManager.getConnection();

            String query = "SELECT * FROM STRUMENTO WHERE IDMAESTRO=?;";

            try
            {
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, idMaestro);

                ResultSet result = stmt.executeQuery();

                while (result.next())
                {
                    strumenti.add(new Strumento(result.getInt(1), result.getString(2), result.getInt(3)));
                }
            }
            catch (SQLException e)
            {
                throw new DAOException("Errore lettura Strumento per Maestro: " + e.getMessage());
            }
            finally
            {
                DBManager.closeConnection();
            }
        }
        catch (SQLException e)
        {
            throw new DBConnectionException("Errore di connessione DB");
        }

        return strumenti;
    }
}
