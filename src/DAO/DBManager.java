package DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DBManager 
{
	
    private static Connection conn = null;
	
	private DBManager() {}
	
	public static Connection getConnection() throws SQLException 
    {	
		if(conn == null || conn.isClosed()) 
        {
			conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/databaseglam10", "root", "");
			System.out.println("Connessione al database riuscita!");
		}	
		return conn;
		
	}
	
	public static void closeConnection() throws SQLException 
    {	
		if(conn != null) {conn.close();}
	}

}
