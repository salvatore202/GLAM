package DAO;

import Entity.Maestro;
import Exception.DAOException;
import Exception.DBConnectionException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class MaestroDAO 
{
    
    //READ
    public static Maestro readMaestro(String email, String password) throws DAOException, DBConnectionException 
    {
        Maestro eM=null;
    
        try 
        {
            Connection conn = DBManager.getConnection();

            String query ="SELECT * FROM MAESTRO WHERE EMAIL=? AND PASSWORD=?;";

            try
            {
                PreparedStatement stmt = conn.prepareStatement(query);

                stmt.setString(1, email);
                stmt.setString(2, password);

                ResultSet result = stmt.executeQuery();

                if(result.next())
                {
                    eM= new Maestro(result.getInt(1), result.getString(2), result.getString(3), result.getLong(4), email, password);
                }
            }
            catch(SQLException e)
            {
                throw new DAOException("Errore lettura Maestro");
            }
            finally 
            {
				DBManager.closeConnection();
			}
        }
        catch(SQLException e) 
        {
			throw new DBConnectionException("Errore di connessione DB");
		}

        return eM;
    }


//READ CON COGNOME
public static Maestro readMaestrobySurname(String cognome) throws DAOException, DBConnectionException 
{
    Maestro eM=null;

    try 
    {
        Connection conn = DBManager.getConnection();

        String query ="SELECT * FROM MAESTRO WHERE COGNOME=?;";

        try
        {
            PreparedStatement stmt = conn.prepareStatement(query);

            stmt.setString(1, cognome);

            ResultSet result = stmt.executeQuery();

            if(result.next())
            {
                eM= new Maestro(result.getInt(1), result.getString(2), cognome, result.getLong(4), result.getString(5), result.getString(6));
            }
        }
        catch(SQLException e)
        {
            throw new DAOException("Errore lettura Maestro");
        }
        finally 
        {
            DBManager.closeConnection();
        }
    }
    catch(SQLException e) 
    {
        throw new DBConnectionException("Errore di connessione DB");
    }

    return eM;
}



//READ CON STRUMENTO
public static ArrayList<Maestro> CercaMaestro(String instrument) throws DAOException, DBConnectionException {
    ArrayList<Maestro> maestri = new ArrayList<>();

    try {
        Connection conn = DBManager.getConnection();

        String query = "SELECT m.* FROM Maestro m JOIN Strumento s ON m.idMaestro = s.idMaestro WHERE s.nome = ?";

        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, instrument);

            ResultSet result = stmt.executeQuery();

            while (result.next()) {
                Maestro maestro = new Maestro(
                    result.getInt(1),    
                    result.getString(2), 
                    result.getString(3), 
                    result.getLong(4),   
                    result.getString(5), 
                    result.getString(6)  
                );
                maestri.add(maestro);
            }
        } catch (SQLException e) {
            throw new DAOException("Errore lettura Maestro per strumento: " + e.getMessage());
        } finally {
            DBManager.closeConnection();
        }
    } catch (SQLException e) {
        throw new DBConnectionException("Errore di connessione DB");
    }

    return maestri;
}

}



