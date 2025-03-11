package DAO;

import Entity.GiornoDisponibilità;
import Entity.Lezione;
import Exception.DAOException;
import Exception.DBConnectionException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class LezioneDAO 
{
    //CREATE
    public static void createLezione(Lezione eL) throws DAOException, DBConnectionException
    {    
       try
       {
           Connection conn = DBManager.getConnection();

           if(checkDataOraLezione(eL.getOra(), eL.getIdData(), eL.getIdMaestro())==1)
           {
               throw new DAOException("Lezione gi� esistente");
           }

           String query = "INSERT INTO LEZIONE (ora, livello, idMaestro, idData, Disponibile) VALUES (?, ?, ?, ?, ?);";

           try
           {
               PreparedStatement stmt = conn.prepareStatement(query);

               stmt.setTime(1, Time.valueOf(eL.getOra()));
               stmt.setString(2, eL.getLivello());
               stmt.setInt(3, eL.getIdMaestro());
               stmt.setInt(4, eL.getIdData());
               stmt.setString(5, "si");
               

               int rows = stmt.executeUpdate();
                if (rows > 0) {
                    System.out.println("Una nuova lezione è stata creata con successo.");
                } else {
                    System.out.println("Nessuna lezione inserita.");
                }

           }
           catch(SQLException e) 
           {
               throw new DAOException("Errore creazione Lezione:  "+e.getMessage());
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
    public static Lezione readLezione(LocalDate data, LocalTime ora, String strumento, String Maestro) throws DAOException, DBConnectionException
    {
        Lezione eL=null;
      
    
        try 
        {
            Connection conn = DBManager.getConnection();

            try
            {
                String query ="SELECT * FROM LEZIONE WHERE IDDATA = (SELECT IDDATA FROM GIORNODISPONIBILITA WHERE DATA = ? AND IDMAESTRO = (SELECT IDMAESTRO FROM STRUMENTO WHERE IDMAESTRO = (SELECT IDMAESTRO FROM MAESTRO WHERE COGNOME = ?) AND NOME = ?)) AND ORA = ?";

                PreparedStatement stmt = conn.prepareStatement(query);

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                // Converte LocalDate in stringa
                String dataStringa = data.format(formatter);

                stmt.setString(1, dataStringa);
                stmt.setString(2, Maestro);
                stmt.setString(3, strumento);
                stmt.setTime(4, Time.valueOf(ora));

                ResultSet result = stmt.executeQuery();

                if(result.next())
                {
                    eL= new Lezione(result.getInt(1), ora, result.getString(3), result.getInt(4), result.getInt(5));
                }
            }
            catch(SQLException e)
            {
                throw new DAOException("Errore lettura lezione: "+e.getMessage());
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

        return eL;
    }



    //READ 
    public static Lezione readLezioneSenzaStrumento(LocalDate data, LocalTime ora, int idMaestro) throws DAOException, DBConnectionException 
    {
        Lezione eL=null;
      
    
        try 
        {
            Connection conn = DBManager.getConnection();

            try
            {
                String query ="SELECT * FROM LEZIONE WHERE IDDATA=("+"SELECT IDDATA FROM GIORNODISPONIBILITA WHERE DATA=?"+") AND IDMAESTRO=? AND ORA=?;";

                PreparedStatement stmt = conn.prepareStatement(query);

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                // Converte LocalDate in stringa
                String dataStringa = data.format(formatter);

                stmt.setString(1, dataStringa);
                stmt.setInt(2, idMaestro);
                stmt.setTime(3, Time.valueOf(ora));

                ResultSet result = stmt.executeQuery();

                if(result.next())
                {
                    eL= new Lezione(result.getInt(1), ora, "BASE", result.getInt(4), result.getInt(5));
                }
            }
            catch(SQLException e)
            {
                throw new DAOException("Errore lettura lezione");
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

        return eL;
    }






    public static ArrayList<Lezione> readLezioniDisponibili(GiornoDisponibilità giornoDisponibilita, int idMaestro) throws DAOException, DBConnectionException {
        ArrayList<Lezione> lezioniDisponibili = new ArrayList<>();
    
        try {
            Connection conn = DBManager.getConnection();
    
            String query = "SELECT * FROM LEZIONE WHERE IDDATA = ? AND IDMAESTRO = ? AND DISPONIBILE = 'si'";
    
            try {
                PreparedStatement stmt = conn.prepareStatement(query);
    
                stmt.setInt(1, giornoDisponibilita.getIdData());
                stmt.setInt(2, idMaestro);
    
                ResultSet result = stmt.executeQuery();
    
                while (result.next()) {
                    int idLezione = result.getInt(1);
                    LocalTime ora = result.getTime(2).toLocalTime();
                    String livello = result.getString(3);
                    int idStudente = result.getInt(4);
                    int idData = result.getInt(5);
    
                    if(result.getString(6).equals("si")) {
                        Lezione lezione = new Lezione(idLezione, ora, livello, idStudente, idData);
                        lezioniDisponibili.add(lezione);
                    }
                }
            } catch (SQLException e) {
                throw new DAOException("Errore lettura lezioni disponibili: " + e.getMessage());
            } finally {
                DBManager.closeConnection();
            }
        } catch (SQLException e) {
            throw new DBConnectionException("Errore di connessione DB");
        }
    
        return lezioniDisponibili;
    }
    





    //UPDATE
    public static void updateLezione(Lezione lezione) throws DAOException, DBConnectionException 
    {
        try
        {
            if (checkIdLezione(lezione.getIdLezione()) == 0) 
            {
                throw new DAOException("La Lezione selezionata non esiste! ");
            }
        }
        catch(DAOException | DBConnectionException e)
        {
            System.out.println(" Errore checkIdLezione in updateLezione: "+e.getMessage());
        }
        
        try 
        {
            Connection conn = DBManager.getConnection();
            
            String query = "UPDATE LEZIONE SET ORA=?, LIVELLO=?, IDMAESTRO=?, IDDATA=?, DISPONIBILE=? WHERE IDLEZIONE=?;";
            try 
            {
                PreparedStatement stmt = conn.prepareStatement(query);

                stmt.setTime(1, Time.valueOf(lezione.getOra()));
                stmt.setString(2, lezione.getLivello());
                stmt.setInt(3, lezione.getIdMaestro());
                stmt.setInt(4, lezione.getIdData());
                if(lezione.isDisponibile())
                    stmt.setString(5, "si");
                else
                    stmt.setString(5, "no");
                stmt.setInt(6, lezione.getIdLezione());
 
                stmt.executeUpdate();

            }
            catch(SQLException e)
            {
                throw new DAOException("Errore update Lezione in LezioneDAO: "+e.getMessage());
            } 
            finally 
            {
				DBManager.closeConnection();
			}
        }
        catch(SQLException e) 
        {
            throw new DBConnectionException("Errore connessione database in LezioneDAO ");
        }

    }




    //UPDATE
    public static void updateDisponibilità(int IdLezione, boolean valore) throws DAOException, DBConnectionException 
    {
        
        try
        {
            if (checkIdLezione(IdLezione) == 0) 
            {
                throw new DAOException("La Lezione selezionata non esiste! ");
            }
        }
        catch(DAOException | DBConnectionException e)
        {
            System.out.println(" Errore checkIdLezione in updateLezione: "+e.getMessage());
        }

        try
        {
            Connection conn = DBManager.getConnection();

            String query = "UPDATE LEZIONE SET DISPONIBILE=? WHERE IDLEZIONE=?;";
            try 
            {
                PreparedStatement stmt = conn.prepareStatement(query);

                stmt.setInt(2, IdLezione);
                if(valore==true)
                    stmt.setString(1, "si");
                else
                    stmt.setString(1, "no");
                
                stmt.executeUpdate();

            }
            catch(SQLException e)
            {
                throw new DAOException("Errore update Lezione in LezioneDAO : "+e.getMessage());
            } 
            finally
            {
                DBManager.closeConnection();
            }
        }
        catch(SQLException e) 
        {
            throw new DBConnectionException("Errore connessione database in LezioneDAO ");
        }
            
    }



    //CONTROLLO ORA LEZIONE
    private static int checkDataOraLezione(LocalTime ora, int idData, int IdMaestro) throws DAOException, DBConnectionException
    {
        int check=0;

        try
        {
            Connection conn = DBManager.getConnection();

            String query = "SELECT COUNT(*) FROM LEZIONE WHERE ORA=? AND IDDATA=? AND IDMAESTRO=?;";

            try
            {
                PreparedStatement stmt = conn.prepareStatement(query);

                stmt.setTime(1, Time.valueOf(ora));
                stmt.setInt(2, idData);
                stmt.setInt(3, IdMaestro);
                ResultSet result = stmt.executeQuery();

                if(result.next()) 
                {
					check = result.getInt(1);
				}
            }
            catch(SQLException e) 
            {
				throw new DAOException("Errore Lezione checkOraLezione: "+ e.getMessage());
			} 

        }
        catch(SQLException e) 
        {
			throw new DBConnectionException("Errore connessione database");
		}

        return check;
    }




    //VERIFICA DISPONIBILITA
    public static boolean checkDisponibilità(int IdLezione) throws DAOException, DBConnectionException
    {
        try
        {
            Connection conn = DBManager.getConnection();

            String query = "SELECT DISPONIBILE FROM LEZIONE WHERE IDLEZIONE=?;";

            try
            {
                PreparedStatement stmt = conn.prepareStatement(query);

                stmt.setInt(1, IdLezione);

                ResultSet result = stmt.executeQuery();

                if(result.next()) 
                {
                    String risultato=result.getString(1);				
                    if(risultato.equals("si"))
                        return true;
				}
            }
            catch(SQLException e) 
            {
				throw new DAOException("Errore Lezione checkDisponibilità: "+e.getMessage());
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

        return false;
    }



    
    //VERIFICA CORRETTEZZA ID LEZIONE
    private static int checkIdLezione(int IdLezione) throws DAOException, DBConnectionException
    {
        int check=0;

        try
        {
            Connection conn = DBManager.getConnection();

            String query = "SELECT COUNT(*) FROM LEZIONE WHERE IDLEZIONE=?;";

            try
            {
                PreparedStatement stmt = conn.prepareStatement(query);

                stmt.setInt(1, IdLezione);
                ResultSet result = stmt.executeQuery();

                if(result.next()) 
                {
					check = result.getInt(1);
				}
            }
            catch(SQLException e) 
            {
				throw new DAOException("Errore Lezione checkIdLezione: "+e.getMessage());
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



    //VERIFICA LIVELLO
    public static String checkLivello(int IdLezione) throws DAOException, DBConnectionException
    {
        String check= null;

        try
        {
            Connection conn = DBManager.getConnection();

            String query = "SELECT LIVELLO FROM LEZIONE WHERE IDLEZIONE=?;";

            try
            {
                PreparedStatement stmt = conn.prepareStatement(query);

                stmt.setInt(1, IdLezione);

                ResultSet result = stmt.executeQuery();

                if(result.next()) 
                {
					check = result.getString(1);
				}
            }
            catch(SQLException e) 
            {
				throw new DAOException("Errore Lezione checkLivello: "+e.getMessage());
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

}