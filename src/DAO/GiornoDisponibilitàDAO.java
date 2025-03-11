package DAO;

import Entity.GiornoDisponibilità;
import Exception.DAOException;
import Exception.DBConnectionException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class GiornoDisponibilitàDAO 
{
    
    //CREATE
    public static void createGiornoDisponibilità(GiornoDisponibilità eG) throws DAOException, DBConnectionException 
    {
        try
        {
            Connection conn = DBManager.getConnection();

            String query = "INSERT INTO GiornoDisponibilita (data, idMaestro) VALUES (?, ?)";

            try
            {
                PreparedStatement stmt = conn.prepareStatement(query);

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                // Converte LocalDate in stringa
                String dataStringa = (eG.getData()).format(formatter);

                stmt.setString(1, dataStringa);
                stmt.setInt(2, eG.getIdMaestro());
                
                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    System.out.println("Un nuovo giorno di disponibilità è stato inserito con successo.");
                } else {
                    System.out.println("Nessuna riga inserita.");
                }

            }
            catch(SQLException e) 
            {
				throw new DAOException("Errore creazione GiornoDisponibilità  " + e.getMessage());
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
    public static GiornoDisponibilità readGiornoDisponibilità(LocalDate data, int IdMaestro) throws DAOException, DBConnectionException 
    {
        GiornoDisponibilità eG=null;

        try
        {
            Connection conn = DBManager.getConnection();

            String query = "SELECT * FROM GIORNODISPONIBILITA WHERE DATA=? AND IDMAESTRO=?;";

            try
            {
                PreparedStatement stmt = conn.prepareStatement(query);


                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                // Converte LocalDate in stringa
                String dataStringa = data.format(formatter);

                stmt.setString(1, dataStringa);
                stmt.setInt(2, IdMaestro);
                
                ResultSet result = stmt.executeQuery();

                if(result.next())
                {
                    eG= new GiornoDisponibilità(result.getInt(1),data,IdMaestro);
                }
            }
            catch(SQLException e) 
            {
				throw new DAOException("Errore lettura GiornoDisponibilità: "+e.getMessage());
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

        return eG;
    }



    public static ArrayList<GiornoDisponibilità> readGiorniDisponibilità(int IdMaestro) throws DAOException, DBConnectionException 
    {
        ArrayList<GiornoDisponibilità> giorniDisponibilita = new ArrayList<>();

    try {
        Connection conn = DBManager.getConnection();

        String query = "SELECT * FROM GIORNODISPONIBILITA WHERE IDMAESTRO=?;";

        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, IdMaestro);

            ResultSet result = stmt.executeQuery();

            while (result.next()) {
                int id = result.getInt("idData");
                LocalDate data = result.getDate("DATA").toLocalDate();  // Conversione da java.sql.Date a java.time.LocalDate
                GiornoDisponibilità giornoDisponibilità = new GiornoDisponibilità(id, data, IdMaestro);
                giorniDisponibilita.add(giornoDisponibilità);
            }
        } catch (SQLException e) {
            throw new DAOException("Errore lettura GiornoDisponibilità: " + e.getMessage());
        } finally {
            DBManager.closeConnection();
        }
    } catch (SQLException e) {
        throw new DBConnectionException("Errore di connessione DB");
    }

    return giorniDisponibilita;
}





    //UPDATE
    public static void updateGiornoDisponibilità(GiornoDisponibilità giorno) throws DAOException, DBConnectionException {
        
        try 
        {
            Connection conn = DBManager.getConnection();
            
            
            String query = "UPDATE GIORNODISPONIBILITA SET DATA=?, IDDATA=?, IDMAESTRO=? WHERE IDDATA=?;";

            try {
                PreparedStatement stmt = conn.prepareStatement(query);

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                // Converte LocalDate in stringa
                String dataStringa = giorno.getData().format(formatter);

                stmt.setString(1, dataStringa);
                stmt.setInt(2, giorno.getIdData());
                stmt.setInt(3, giorno.getIdMaestro());
                stmt.setInt(4, giorno.getIdData());
                
                stmt.executeUpdate();

            }
            catch(SQLException e)
            {
                throw new DAOException("Errore update GiornoDisponibilità in GiornoDisponibilitàDAO: "+e.getMessage());
            } 
            finally
            {
                DBManager.closeConnection();
            }

        }
        catch(SQLException e) 
        {
            throw new DBConnectionException("Errore connessione database in GiornoDisponibilitàDAO ");
        }
    }




    //CONTROLLO ESISTENZA GIORNODISPONIBILITA
    public static int checkData(LocalDate data) throws DAOException, DBConnectionException
    {
        int check=0;

        try
        {
            Connection conn = DBManager.getConnection();

            String query = "SELECT COUNT (*) FROM GIORNIDISPONIBILITA WHERE DATA=?;";

            try
            {
                PreparedStatement stmt = conn.prepareStatement(query);

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                Date dataformat = Date.valueOf(data.format(formatter));

                stmt.setDate(1, dataformat);
                ResultSet result = stmt.executeQuery(query);

                if(result.next()) 
                {
					check = result.getInt(2);
				}
            }
            catch(SQLException e) 
            {
				throw new DAOException("Errore GiornoDisponibilità checkData");
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

        return check;
    }




    //CONTROLLO ESISTENZA GIORNODISPONIBILITA DEL MAESTRO
    public static int checkGiornoMaestro(LocalDate data, int IdMaestro) 
    {
        int check=0;

        try
        {
            Connection conn = DBManager.getConnection();

            String query = "SELECT COUNT(*) FROM GIORNODISPONIBILITA WHERE DATA=? AND IDMAESTRO=?;";

            try
            {
                PreparedStatement stmt = conn.prepareStatement(query);


                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                // Converte LocalDate in stringa
                String dataStringa = data.format(formatter);

                stmt.setString(1, dataStringa);
                stmt.setInt(2, IdMaestro);

                ResultSet result = stmt.executeQuery();

                if(result.next()) 
                {
					check = result.getInt(1);
				}
            }
            catch(SQLException e) 
            {
				System.out.println( "Errore query in CheckGiornoMaestro: non esiste un giorno con la stessa data" );
			} 
            finally 
            {
				DBManager.closeConnection();
			}

        }
        catch(SQLException e) 
        {
            System.out.println("Errore di connesione al database ");
		}

        return check;
    }

}
