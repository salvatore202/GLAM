package DAO;

import Entity.Prenotazione;
import Exception.DAOException;
import Exception.DBConnectionException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class PrenotazioneDAO 
{

    //CREATE
    public static void createPrenotazione(Prenotazione prenotazione) throws DAOException, DBConnectionException
    {
        try {

            Connection conn = DBManager.getConnection();
            
            String query = "INSERT INTO PRENOTAZIONE (costo, idLezione, idStudente) VALUES (?,?,?);";
            try 
            {
                PreparedStatement stmt = conn.prepareStatement(query);
                
                stmt.setInt(3, prenotazione.getIdStudente());
                stmt.setInt(2, prenotazione.getIdLezione());                         
                stmt.setFloat(1, prenotazione.getCosto());

                stmt.executeUpdate();

            }
            catch(SQLException e) 
            {
                throw new DAOException("Errore creazione Prenotazione: "+e.getMessage());
            } 
            finally 
            {
                DBManager.closeConnection();
            }

        }
        catch(SQLException e) 
        {
            throw new DBConnectionException("Errore connessione database");
        }   
    }




    //READ
    public static Prenotazione readPrenotazione(int IdLezione, int IdStudente) throws DAOException, DBConnectionException 
    {
        Prenotazione eP=null;
    
        try 
        {
            Connection conn = DBManager.getConnection();

            String query ="SELECT * FROM PRENOTAZIONE WHERE IDLEZIONE=? AND IDSTUDENTE=?;";

            try
            {
                PreparedStatement stmt = conn.prepareStatement(query);

                stmt.setInt(1, IdLezione);  
                stmt.setInt(2, IdStudente);

                ResultSet result = stmt.executeQuery();

                if(result.next())
                {
                    eP= new Prenotazione(result.getInt(1), result.getFloat(2), IdLezione, IdStudente);
                }
            }
            catch(SQLException e)
            {
                throw new DAOException("Errore lettura Prenotazione: "+e.getMessage());
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

        return eP;
    }




    //DELETE
    public static void deletePrenotazione(int IdPrenotazione) throws DAOException, DBConnectionException 
    {
        try 
        {
            Connection conn = DBManager.getConnection();

            String query = "DELETE FROM PRENOTAZIONE WHERE IDPRENOTAZIONE=?;";

            try {
                PreparedStatement stmt = conn.prepareStatement(query);

                stmt.setInt(1, IdPrenotazione);

                stmt.executeUpdate();

            }
            catch(SQLException e) 
            {
                throw new DAOException("Errore lettura Prenotazione in deletePrenotazione: "+e.getMessage());
            } 
            finally 
            {
                DBManager.closeConnection();
            }
        }
        catch(SQLException e) 
        {
            throw new DBConnectionException("Errore connessione database in OrdineDAO ");
        }
    }
    
}