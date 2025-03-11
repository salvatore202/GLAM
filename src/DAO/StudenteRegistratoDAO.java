package DAO;

import Entity.StudenteRegistrato;
import Exception.DAOException;
import Exception.DBConnectionException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StudenteRegistratoDAO
{
    
    //READ
    public static StudenteRegistrato readStudenteRegistrato(String username, String password) throws DAOException, DBConnectionException 
    {
        StudenteRegistrato eSR=null;

        try
        {
            Connection conn = DBManager.getConnection();

            String query = "SELECT * FROM STUDENTEREGISTRATO WHERE USERNAME=? AND PASSWORD=?;";

            try
            {
                PreparedStatement stmt = conn.prepareStatement(query);

                stmt.setString(1, username);
                stmt.setString(2, password);
                
                ResultSet result = stmt.executeQuery();

                if(result.next())
                {
                    eSR= new StudenteRegistrato(result.getInt(1), result.getString(2), result.getString(3), result.getDate(4), result.getLong(5), username, result.getString(7), password);
                }
            }
            catch(SQLException e) 
            {
				throw new DAOException("Errore lettura StudenteRegistrato");
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

        return eSR;
    }

}
