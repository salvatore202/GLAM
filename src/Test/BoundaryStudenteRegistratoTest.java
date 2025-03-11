package Test;



import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import static org.junit.Assert.*;
import org.junit.Test;
import DAO.LezioneDAO;
import DAO.PrenotazioneDAO;
import DAO.StudenteRegistratoDAO;
import Entity.Lezione;
import Entity.Prenotazione;
import Entity.StudenteRegistrato;
import Exception.DAOException;
import Exception.DBConnectionException;


public class BoundaryStudenteRegistratoTest 
{
    
    @Test
    public void prenotaLezione_inputValidiTest()
    {
        String username="vdeluca";
        String password="password111";

        StudenteRegistrato studente;

        ArrayList<String> cognomiMaestro= new ArrayList<>();
        ArrayList<String> nomiMaestro=new ArrayList<>();
        ArrayList<LocalDate> date = new ArrayList<>();
        ArrayList<LocalTime> ore = new ArrayList<>();
        ArrayList<String> strumenti=new ArrayList<>();
        ArrayList<String> livelli= new ArrayList<>();
        
        Lezione eL;

        cognomiMaestro.add("Ventresca");
        cognomiMaestro.add("Circuito");


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate nuovadata_temp = LocalDate.parse("20/05/2025", formatter);
        date.add(nuovadata_temp);
        date.add(nuovadata_temp);

        strumenti.add("PIANOFORTE");
        strumenti.add("VIOLINO");

        livelli.add("BASE");
        livelli.add("INTERMEDIO");

        ore.add(LocalTime.parse("18:00"));
        ore.add(LocalTime.parse("17:00"));

        
        try
        {
            
            for (int i = 0; i < nomiMaestro.size(); i++) 
            {
                studente=StudenteRegistratoDAO.readStudenteRegistrato(username, password);
              
                assertTrue(" Studente non Registrato, operazione non consentita ", studente!=null);

                eL=LezioneDAO.readLezione(date.get(i), ore.get(i) , strumenti.get(i), cognomiMaestro.get(i));
                assertTrue(" Lezione non trovata", eL!=null);

                boolean controllo = LezioneDAO.checkDisponibilità(eL.getIdLezione());
                assertTrue(" La Lezione "+(i+1)+" non è disponibile", controllo==true);

            }
            
        }
        catch(DBConnectionException|DAOException e)
        {
            e.printStackTrace();
        }
    }



    @Test
    public void prenotaLezione_lezioneNonPresenteTest()
    {
        String username="vdeluca";
        String password="password111";

        StudenteRegistrato studente;

        ArrayList<String> cognomiMaestro= new ArrayList<>();
        ArrayList<String> nomiMaestro=new ArrayList<>();
        ArrayList<LocalDate> date = new ArrayList<>();
        ArrayList<LocalTime> ore = new ArrayList<>();
        ArrayList<String> strumenti=new ArrayList<>();
        ArrayList<String> livelli= new ArrayList<>();
        
        Lezione eL;

        cognomiMaestro.add("Ventresca");
        cognomiMaestro.add("Circuito");


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate nuovadata_temp = LocalDate.parse("20/05/2025", formatter);
        date.add(nuovadata_temp);
        date.add(nuovadata_temp);

        strumenti.add("PIANOFORTE");
        strumenti.add("VIOLINO");

        livelli.add("BASE");
        livelli.add("INTERMEDIO");

        ore.add(LocalTime.parse("21:00"));
        ore.add(LocalTime.parse("21:00"));

        
        try
        {
            int i=0;
            
                studente=StudenteRegistratoDAO.readStudenteRegistrato(username, password);
                assertTrue(" Studente non Registrato, operazione non consentita ", studente!=null);

                eL=LezioneDAO.readLezione(date.get(i), ore.get(i) , strumenti.get(i), cognomiMaestro.get(i));
                assertTrue(" Lezione non trovata", eL==null);

        }
        catch(DBConnectionException|DAOException e)
        {
            e.printStackTrace();
        }
    }




    @Test
    public void prenotaLezione_lezioneNonDisponibileTest()
    {
        String username="vdeluca";
        String password="password111";

        StudenteRegistrato studente;

        ArrayList<String> cognomiMaestro= new ArrayList<>();
        ArrayList<String> nomiMaestro=new ArrayList<>();
        ArrayList<LocalDate> date = new ArrayList<>();
        ArrayList<LocalTime> ore = new ArrayList<>();
        ArrayList<String> strumenti=new ArrayList<>();
        ArrayList<String> livelli= new ArrayList<>();
        
        Lezione eL;

        cognomiMaestro.add("Ventresca");
        cognomiMaestro.add("Circuito");


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate nuovadata_temp = LocalDate.parse("20/05/2025", formatter);
        date.add(nuovadata_temp);
        date.add(nuovadata_temp);

        strumenti.add("PIANOFORTE");
        strumenti.add("VIOLINO");

        livelli.add("BASE");
        livelli.add("INTERMEDIO");

        ore.add(LocalTime.parse("17:00"));
        ore.add(LocalTime.parse("17:00"));

        
        try
        {
            int i=0;

            studente=StudenteRegistratoDAO.readStudenteRegistrato(username, password);
                assertTrue(" Studente non Registrato, operazione non consentita ", studente!=null);

                eL=LezioneDAO.readLezione(date.get(i), ore.get(i) , strumenti.get(i), cognomiMaestro.get(i));
                assertTrue(" Lezione non trovata", eL!=null);

                boolean controllo = LezioneDAO.checkDisponibilità(eL.getIdLezione());
                assertTrue(" La Lezione "+(i+1)+" non è disponibile", controllo==false);

            
            
        }
        catch(DBConnectionException|DAOException e)
        {
            e.printStackTrace();
        }
    }



    @Test
    public void prenotaLezione_studenteNonRegistratoTest()
    {
        String username="abcdefgh";
        String password="password333";

        StudenteRegistrato studente;

        ArrayList<String> cognomiMaestro= new ArrayList<>();
        ArrayList<String> nomiMaestro=new ArrayList<>();
        ArrayList<LocalDate> date = new ArrayList<>();
        ArrayList<LocalTime> ore = new ArrayList<>();
        ArrayList<String> strumenti=new ArrayList<>();
        ArrayList<String> livelli= new ArrayList<>();
        
        Lezione eL;

        cognomiMaestro.add("Ventresca");
        cognomiMaestro.add("Circuito");


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate nuovadata_temp = LocalDate.parse("19/05/2025", formatter);
        date.add(nuovadata_temp);
        date.add(nuovadata_temp);

        strumenti.add("PIANOFORTE");
        strumenti.add("VIOLINO");

        livelli.add("BASE");
        livelli.add("INTERMEDIO");

        ore.add(LocalTime.parse("17:00"));
        ore.add(LocalTime.parse("17:00"));

        
        try
        {
            int i=0;
            
            studente=StudenteRegistratoDAO.readStudenteRegistrato(username, password);
            assertTrue(" Studente non Registrato, operazione non consentita ", studente==null);

            
            eL=LezioneDAO.readLezione(date.get(i), ore.get(i) , strumenti.get(i), cognomiMaestro.get(i));
            assertTrue(" Lezione non trovata", eL!=null);

            boolean controllo = LezioneDAO.checkDisponibilità(eL.getIdLezione());
            assertTrue(" La Lezione "+(i+1)+" non è disponibile", controllo==false);
            
        }
        catch(DBConnectionException|DAOException e)
        {
            e.printStackTrace();
        }
    }


    @Test
    public void disdiciLezione_inputValidiTest()
    {
        String username="vdeluca";
        String password="password111";

        String cognomeMaestro="Ventresca";
        String nomeMaestro="Paolo";
        String strumento="PIANOFORTE";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate data=LocalDate.parse("20/05/2025", formatter);

        LocalTime ora=LocalTime.parse("17:00");

        try
        {
            StudenteRegistrato studenteRegistrato = StudenteRegistratoDAO.readStudenteRegistrato(username, password);
            assertTrue(" Studente non Registrato, operazione non consentita  ", studenteRegistrato!=null);

            Lezione lezioneprenotata = LezioneDAO.readLezione(data, ora, strumento, cognomeMaestro);
            assertTrue(" Lezione non trovata, disdetta non disponibile", lezioneprenotata!=null);

            Prenotazione prenotazioneEffettata = PrenotazioneDAO.readPrenotazione(lezioneprenotata.getIdLezione(), studenteRegistrato.getIdStudente());
            assertTrue(" Lezione non prenotata, disdetta non disponibile", prenotazioneEffettata!=null);

        }
        catch(DBConnectionException|DAOException e)
        {
            e.printStackTrace();
        }
    }




    @Test
    public void disdiciLezione_studenteNonRegistratoTest()
    {
        String username="oooooooooo";
        String password="password33333";

        String cognomeMaestro="Ventresca";
        String nomeMaestro="Paolo";
        String strumento="PIANOFORTE";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate data=LocalDate.parse("19/05/2025", formatter);

        LocalTime ora=LocalTime.parse("17:00");

        try
        {
            StudenteRegistrato studenteRegistrato = StudenteRegistratoDAO.readStudenteRegistrato(username, password);
            assertTrue(" Studente non Registrato, operazione non consentita  ", studenteRegistrato==null);

            Lezione lezioneprenotata = LezioneDAO.readLezione(data, ora, strumento, cognomeMaestro);
            assertTrue(" Lezione non trovata, disdetta non disponibile", lezioneprenotata!=null);

           if(studenteRegistrato!=null)
           {
            try
            {
                Prenotazione prenotazioneEffettata = PrenotazioneDAO.readPrenotazione(lezioneprenotata.getIdLezione(), studenteRegistrato.getIdStudente());
                assertTrue(" Lezione non prenotata, disdetta non disponibile", prenotazioneEffettata!=null);
            }
            catch(DBConnectionException|DAOException e1)
            {
                e1.printStackTrace();
            }
           }
        }
        catch(DBConnectionException|DAOException e)
        {
            e.printStackTrace();
        }
    }




    @Test
    public void disdiciLezione_lezioneNonPresenteTest()
    {
        String username="vdeluca";
        String password="password111";

        String cognomeMaestro="Ventresca";
        String nomeMaestro="Paolo";
        String strumento="PIANOFORTE";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate data=LocalDate.parse("19/05/7400", formatter);

        LocalTime ora=LocalTime.parse("17:00");

        try
        {
            StudenteRegistrato studenteRegistrato = StudenteRegistratoDAO.readStudenteRegistrato(username, password);
            assertTrue(" Studente non Registrato, operazione non consentita  ", studenteRegistrato!=null);

            Lezione lezioneprenotata = LezioneDAO.readLezione(data, ora, strumento, cognomeMaestro);
            assertTrue(" Lezione non trovata, disdetta non disponibile", lezioneprenotata==null);
              

        }
        catch(DBConnectionException|DAOException e)
        {
            e.printStackTrace();
        }
    }




    @Test
    public void disdiciLezione_lezioneNonPrenotataTest()
    {
        String username="vdeluca";
        String password="password111";

        String cognomeMaestro="Ventresca";
        String nomeMaestro="Paolo";
        String strumento="PIANOFORTE";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate data=LocalDate.parse("20/05/2025", formatter);

        LocalTime ora=LocalTime.parse("18:00");

        try
        {
            StudenteRegistrato studenteRegistrato = StudenteRegistratoDAO.readStudenteRegistrato(username, password);
            assertTrue(" Studente non Registrato, operazione non consentita  ", studenteRegistrato!=null);

            Lezione lezioneprenotata = LezioneDAO.readLezione(data, ora, strumento, cognomeMaestro);
            assertTrue(" Lezione non trovata, disdetta non disponibile", lezioneprenotata!=null);

            Prenotazione prenotazioneEffettata = PrenotazioneDAO.readPrenotazione(lezioneprenotata.getIdLezione(), studenteRegistrato.getIdStudente());
            assertTrue(" Lezione non prenotata, disdetta non disponibile", prenotazioneEffettata==null);

        }
        catch(DBConnectionException|DAOException e)
        {
            e.printStackTrace();
        }
    }

}
