package Control;

import DAO.*;
import Entity.*;
import Exception.*;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

/**
 *
 * @author salva
 */
public class GestioneLezioni 
{
    private final float PREZZO_STANDARD=15;
    private static GestioneLezioni gL=null;

    public static GestioneLezioni getInstance()
    {
        if(gL==null)
            gL=new GestioneLezioni();
        return gL;
    }

    public static boolean verificaDisdettaConAnticipo(LocalDate dataPrenotazione) 
    {
        // Calcola la differenza in giorni tra la data della lezione e la data di disdetta
        long giorniDiDifferenza = ChronoUnit.DAYS.between(LocalDate.now(), dataPrenotazione);

        // Verifica se la differenza è di almeno 3 giorni
        return giorniDiDifferenza >= 3;
    }


    public class id 
    {
        // Variabile di classe (statica) che memorizza il valore del contatore
        private static int count = 0;
    
        // Metodo che incrementa e restituisce il valore del contatore
        public static int getNextId() 
        {
            return ++count;
        }
    }




    //UC3 - PrenotaLezione
    public void prenotaLezione(String username, String password, ArrayList<String> nomiMaestro, ArrayList<String> cognomiMaestro, ArrayList<LocalDate> date, ArrayList<LocalTime> ore, ArrayList<String> strumenti, ArrayList<String> livelli) 
    {
        float costoTot;
        StudenteRegistrato studenteRegistrato;

        Prenotazione prenotazioneEffettuata= new Prenotazione();
        float costolivello;

        Lezione eL;

        try
        {
            studenteRegistrato=StudenteRegistratoDAO.readStudenteRegistrato(username, password);

            try
            {

                for (int i = 0; i < nomiMaestro.size(); i++) 
                {
                    eL=LezioneDAO.readLezione(date.get(i), ore.get(i) , strumenti.get(i), cognomiMaestro.get(i));

                    if(eL==null)
                        System.out.println(" Lezione non trovata ");
                    else
                    {
                                if(LezioneDAO.checkDisponibilità(eL.getIdLezione()))
                                {
                                    try
                                    {                                        
                                        eL.setLivello(livelli.get(i));
                                        eL.setDisponibile(false);
                                        LezioneDAO.updateLezione(eL);  
                    
                                            if(livelli.get(i).toUpperCase().equals("BASE"))
                                                costolivello=1;
                                            else
                                                costolivello=2;
        
                                            costoTot=PREZZO_STANDARD*costolivello;
            
                                            prenotazioneEffettuata.setIdLezione(eL.getIdLezione());
                                            prenotazioneEffettuata.setIdStudente(studenteRegistrato.getIdStudente());
                                            prenotazioneEffettuata.setCosto(costoTot);
                                            
                                            try
                                            {
                                                prenotazioneEffettuata.savePrenotazione();
                                                System.out.println(" PRENOTAZIONE AVVENUTA CON SUCCESSO !");
                                                System.err.println();
                                            }
                                            catch (DAOException | DBConnectionException e) 
                                            {
                                                System.out.println("Qualcosa e' andato storto durante il salvataggio della prenotazione in prenotaLezione: "+e.getMessage());
                                            }
                                    }
                                    catch (DAOException | DBConnectionException e) 
                                    {
                                        System.out.println("Qualcosa e' andato storto in updateLezione di prenotaLezione : " +e.getMessage());
                                    }
                                    
                                }
                                else
                                    System.out.println(" La Lezione "+(i+1)+" non è disponibile ");
                    }                        
                }

            }
            catch (DAOException | DBConnectionException e) 
            {
                System.out.println("Qualcosa e' andato storto durante la lettura della lezione in prenotaLezione: "+e.getMessage() );
            }
            
        }
        catch (DAOException | DBConnectionException e) 
        {
            System.out.println("Qualcosa e' andato storto durante la lettura dello studente registrato in prenotaLezione " );
        }

    }




    //UC4 - DisdiciLezione
    public void disdiciLezione(String username, String password, LocalDate data, LocalTime ora, String nomeMaestro, String cognomeMaestro, String strumento)
    {
        StudenteRegistrato studenteRegistrato;
        Lezione lezioneprenotata;
        Prenotazione prenotazioneEffettata;

        
        try
        {
            studenteRegistrato=StudenteRegistratoDAO.readStudenteRegistrato(username, password);

            try
            {

                lezioneprenotata=LezioneDAO.readLezione(data, ora, strumento, cognomeMaestro);
                if(lezioneprenotata==null)
                    System.out.println(" Lezione non trovata ");
                try
                {
                    prenotazioneEffettata=PrenotazioneDAO.readPrenotazione(lezioneprenotata.getIdLezione(), studenteRegistrato.getIdStudente());

                    try
                    {
                        LezioneDAO.updateDisponibilità(lezioneprenotata.getIdLezione(), true);

                        try
                        {
                            if(verificaDisdettaConAnticipo(data))
                                PrenotazioneDAO.deletePrenotazione(prenotazioneEffettata.getIdPrenotazione());

                            System.out.println();
                            System.out.println(" DISDETTA AVVENUTA CON SUCCESSO !");
                            System.out.println();
                        }
                        catch (DAOException | DBConnectionException e) 
                        {
                            System.out.println("Qualcosa e' andato storto durante l'eliminazione della prenotazione in disdiciLezione: "+e.getMessage() );
                        }
                    }
                    catch (DAOException | DBConnectionException e) 
                    {
                        System.out.println("Qualcosa e' andato storto durante l'update della lezione in disdiciLezione: "+e.getMessage());
                    }

                }
                catch (DAOException | DBConnectionException e) 
                {
                    System.out.println("Qualcosa e' andato storto durante la lettura della Prenotazione in disdiciLezione: "+e.getMessage() );
                }
            }
            catch (DAOException | DBConnectionException e) 
            {
                System.out.println("Qualcosa e' andato storto durante la lettura della lezione in disdiciLezione: "+e.getMessage() );
            }
        }
        catch (DAOException | DBConnectionException e) 
        {
            System.out.println("Qualcosa e' andato storto durante la lettura dello studente registrato in prenotaLezione " );
        }
    
        //SERVIZIO MAIL
    }



    //UC6 - inserisciGiorno
    public void inserisciGiorno(LocalDate data, String email, String password) 
    {
        GiornoDisponibilità eG;
        Maestro eM;
        Lezione eL= new Lezione(0, null, "BASE", 0, 0);
        Time oraStart = Time.valueOf("17:00:00");
        LocalTime localStart = oraStart.toLocalTime();

        try
        {
            eM=MaestroDAO.readMaestro(email, password);

                        eG=new GiornoDisponibilità(0, data, eM.getId());
                        eL.setDisponibile(true);
                        eL.setIdMaestro(eM.getId());

                        
                            if(GiornoDisponibilitàDAO.checkGiornoMaestro(data, eM.getId())==0)
                            {
                                System.out.println(" Possibile inserire il giorno\n" );

                                try
                                {
                                    eG.saveGiornoDisponibilità();

                                    try
                                    {
                                        eG=GiornoDisponibilitàDAO.readGiornoDisponibilità(data, eM.getId());
                                        eL.setIdData(eG.getIdData());

                                        for (int i = 0; i < 4; i++) 
                                        {   
                                            eL.setOra(localStart.plusHours(i));
                                            
                                            try
                                            {
                                                eL.saveLezione();
                                            }
                                            catch(DAOException | DBConnectionException e2)
                                            {
                                                System.out.println("Qualcosa e' andato storto durante il savataggio di una lezione in inserisciGiorno: "+e2.getMessage() );
                                            }
                                           
                                        }
                                    }
                                    catch(DAOException | DBConnectionException e3)
                                    {
                                        System.out.println("Qualcosa e' andato storto durante il savataggio del Giorno di Disponibilità in inserisciGiorno: "+e3.getMessage() );
                                    }

                                }
                                catch(DAOException | DBConnectionException e1)
                                {
                                    System.out.println("Qualcosa e' andato storto durante il salvataggio del giorno in inserisciGiorno:   "+e1.getMessage() );
                                }
                            }
                            else
                                System.out.println(" Giorno già presente ");                  
        }
        catch(DAOException | DBConnectionException e)
        {
            System.out.println("Errore Lettura Maestro in inserisciGiorno");
        }
    }



 
    public void modificaGiorno(LocalDate data, LocalDate nuovadata, String email, String password) 
    {
        GiornoDisponibilità eG;
        Maestro eM;

        try
        {
            eM=MaestroDAO.readMaestro(email, password);
            try
            {
                eG=GiornoDisponibilitàDAO.readGiornoDisponibilità(data, eM.getId());


                if(eG!=null)
                {

                    if(GiornoDisponibilitàDAO.checkGiornoMaestro(nuovadata, eM.getId())==0)
                    {
                        eG.setData(nuovadata);

                        try
                        {
                            GiornoDisponibilitàDAO.updateGiornoDisponibilità(eG);
                            System.out.println(" GIORNO DI DISPONIBILITA MODIFICATO CON SUCCESSO !");
                        }
                        catch(DAOException|DBConnectionException e)
                        {
                            System.out.println("Qualcosa e' andato storto durante l'aggiornamento del Giorno di Disponibilità in modificaGiorno: "+e.getMessage() );
                        }
                    }
                    else
                        System.out.println(" Nuovo Giorno indicato gia presente nel sistema ");             
                
                }
                else
                    System.out.println(" Giorno indicato Non presente nel sistema ");
            }
            catch (DAOException | DBConnectionException e) 
            {
                System.out.println("Qualcosa e' andato storto durante la lettura del Giorno di Disponibilità in modificaGiorno: "+e.getMessage() );
            }
        }
        catch (DAOException | DBConnectionException e) 
        {
            System.out.println("Qualcosa e' andato storto durante la lettura del Maestro in modificaGiorno: "+e.getMessage() );
        }
                          
    }



    //AUTENTICAZIONE MAESTRO
    public static  boolean verificaMaestro(String email, String password)
    {
        Maestro eM;
        try
        {
            eM=MaestroDAO.readMaestro(email, password);

            if(eM!=null)
                return true;
        }
        catch (DAOException | DBConnectionException e) 
        {
            System.out.println("Qualcosa e' andato storto durante l'Autenticazione del Maestro in verificaMaestro" );
        }
        
        return false;
    }


    //AUTENTICAZIONE STUDENTE REGISTRATO
    public static boolean verificaStudente(String username, String password)
    {
        StudenteRegistrato eSR;
        try
        {
            eSR=StudenteRegistratoDAO.readStudenteRegistrato(username, password);

            if(eSR!=null)
                return true;
        }
        catch (DAOException | DBConnectionException e) 
        {
            System.out.println("Qualcosa e' andato storto durante l'Autenticazione dello Studente in verificaStudente" );
        }
        
        return false;
    }

}




