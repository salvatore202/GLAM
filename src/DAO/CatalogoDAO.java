package DAO;

import Entity.Maestro;
import Exception.DAOException;
import Exception.DBConnectionException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO aggiuntivo (non presente nel progetto originale), di sola lettura,
 * con le query di aggregazione/elenco necessarie al frontend web che non
 * esistevano in nessuna delle classi DAO originali (es. "tutti i Maestri",
 * "prenotazioni di uno Studente", "lezioni svolte da un Maestro").
 *
 * Implementa concretamente, lato dati, le due voci di menu della CLI
 * originale marcate come "Operazione non ancora disponibile":
 *  - BoundaryMaestro:            "3. Consulta Lezioni Svolte"
 *  - BoundaryStudenteRegistrato: "4. Visualizza elenco lezioni svolte in questo mese"
 *
 * Nota di modellazione (non e' un bug introdotto qui, e' cosi' nello schema
 * originale): la tabella LEZIONE non memorizza l'idStrumento, quindi una
 * volta svolta/prenotata una lezione non e' possibile risalire con certezza
 * allo strumento se il Maestro ne insegna piu' di uno. Per questo le righe
 * restituite da questa classe non includono un campo "strumento" inventato.
 *
 * Nessun file esistente e' stato modificato per aggiungere questa classe.
 */
public class CatalogoDAO
{
    // READ - tutti i Maestri (senza password)
    public static ArrayList<Maestro> readAllMaestri() throws DAOException, DBConnectionException
    {
        ArrayList<Maestro> maestri = new ArrayList<>();

        try
        {
            Connection conn = DBManager.getConnection();

            String query = "SELECT * FROM MAESTRO ORDER BY COGNOME;";

            try
            {
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet result = stmt.executeQuery();

                while (result.next())
                {
                    maestri.add(new Maestro(
                        result.getInt(1),
                        result.getString(2),
                        result.getString(3),
                        result.getLong(4),
                        result.getString(5),
                        result.getString(6)
                    ));
                }
            }
            catch (SQLException e)
            {
                throw new DAOException("Errore lettura elenco Maestri: " + e.getMessage());
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

        return maestri;
    }

    // READ - singolo Maestro per id (MaestroDAO originale legge solo per email+password, per cognome, o per strumento)
    public static Maestro readMaestroById(int idMaestro) throws DAOException, DBConnectionException
    {
        Maestro eM = null;

        try
        {
            Connection conn = DBManager.getConnection();

            String query = "SELECT * FROM MAESTRO WHERE IDMAESTRO=?;";

            try
            {
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, idMaestro);

                ResultSet result = stmt.executeQuery();

                if (result.next())
                {
                    eM = new Maestro(
                        result.getInt(1),
                        result.getString(2),
                        result.getString(3),
                        result.getLong(4),
                        result.getString(5),
                        result.getString(6)
                    );
                }
            }
            catch (SQLException e)
            {
                throw new DAOException("Errore lettura Maestro per id: " + e.getMessage());
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

        return eM;
    }

    // READ - lezioni svolte/prenotate presso un Maestro (per "Consulta Lezioni Svolte")
    public static List<Map<String, Object>> readLezioniSvolteByMaestro(int idMaestro) throws DAOException, DBConnectionException
    {
        List<Map<String, Object>> righe = new ArrayList<>();

        try
        {
            Connection conn = DBManager.getConnection();

            String query =
                "SELECT l.idLezione, l.ora, l.livello, g.data, p.idPrenotazione, p.costo, " +
                "s.idStudente, s.nome, s.cognome " +
                "FROM LEZIONE l " +
                "JOIN GIORNODISPONIBILITA g ON l.idData = g.idData " +
                "LEFT JOIN PRENOTAZIONE p ON p.idLezione = l.idLezione " +
                "LEFT JOIN STUDENTEREGISTRATO s ON s.idStudente = p.idStudente " +
                "WHERE l.idMaestro = ? AND l.Disponibile = 'no' " +
                "ORDER BY g.data DESC, l.ora DESC;";

            try
            {
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, idMaestro);

                ResultSet rs = stmt.executeQuery();

                while (rs.next())
                {
                    Map<String, Object> riga = new LinkedHashMap<>();
                    riga.put("idLezione", rs.getInt("idLezione"));
                    riga.put("ora", rs.getTime("ora").toLocalTime().toString());
                    riga.put("livello", rs.getString("livello"));
                    riga.put("data", rs.getDate("data").toLocalDate().toString());

                    Object idPren = rs.getObject("idPrenotazione");
                    if (idPren == null)
                    {
                        riga.put("prenotazione", null);
                    }
                    else
                    {
                        Map<String, Object> studente = new LinkedHashMap<>();
                        studente.put("idStudente", rs.getInt("idStudente"));
                        studente.put("nome", rs.getString("nome"));
                        studente.put("cognome", rs.getString("cognome"));

                        Map<String, Object> prenotazione = new LinkedHashMap<>();
                        prenotazione.put("idPrenotazione", rs.getInt("idPrenotazione"));
                        prenotazione.put("costo", rs.getFloat("costo"));
                        prenotazione.put("studente", studente);

                        riga.put("prenotazione", prenotazione);
                    }

                    righe.add(riga);
                }
            }
            catch (SQLException e)
            {
                throw new DAOException("Errore lettura lezioni svolte: " + e.getMessage());
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

        return righe;
    }

    // READ - tutte le prenotazioni di uno Studente (per "Visualizza elenco lezioni svolte")
    public static List<Map<String, Object>> readPrenotazioniByStudente(int idStudente) throws DAOException, DBConnectionException
    {
        List<Map<String, Object>> righe = new ArrayList<>();

        try
        {
            Connection conn = DBManager.getConnection();

            String query =
                "SELECT p.idPrenotazione, p.costo, l.idLezione, l.ora, l.livello, g.data, " +
                "m.idMaestro, m.nome, m.cognome " +
                "FROM PRENOTAZIONE p " +
                "JOIN LEZIONE l ON p.idLezione = l.idLezione " +
                "JOIN GIORNODISPONIBILITA g ON l.idData = g.idData " +
                "JOIN MAESTRO m ON l.idMaestro = m.idMaestro " +
                "WHERE p.idStudente = ? " +
                "ORDER BY g.data DESC, l.ora DESC;";

            try
            {
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, idStudente);

                ResultSet rs = stmt.executeQuery();

                while (rs.next())
                {
                    Map<String, Object> riga = new LinkedHashMap<>();
                    riga.put("idPrenotazione", rs.getInt("idPrenotazione"));
                    riga.put("costo", rs.getFloat("costo"));
                    riga.put("idLezione", rs.getInt("idLezione"));
                    riga.put("ora", rs.getTime("ora").toLocalTime().toString());
                    riga.put("livello", rs.getString("livello"));
                    riga.put("data", rs.getDate("data").toLocalDate().toString());

                    Map<String, Object> maestro = new LinkedHashMap<>();
                    maestro.put("idMaestro", rs.getInt("idMaestro"));
                    maestro.put("nome", rs.getString("nome"));
                    maestro.put("cognome", rs.getString("cognome"));
                    riga.put("maestro", maestro);

                    righe.add(riga);
                }
            }
            catch (SQLException e)
            {
                throw new DAOException("Errore lettura prenotazioni Studente: " + e.getMessage());
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

        return righe;
    }
}
