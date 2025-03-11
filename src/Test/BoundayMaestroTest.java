package Test;

import Exception.DAOException;
import Exception.DBConnectionException;
import static org.junit.Assert.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.junit.Test;

//import Boundary.*;
import DAO.GiornoDisponibilitàDAO;
import DAO.MaestroDAO;
import Entity.GiornoDisponibilità;
import Entity.Maestro;;


public class BoundayMaestroTest 
{
    @Test
    public void inserisciGiorno_inputValidiTest()
    {
        Maestro eM= new Maestro(1, "Paolo", "Ventresca", 1234567890, "paolo.ventresca@example.com", "password123");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate data = LocalDate.parse("22/05/2500", formatter);
        GiornoDisponibilità eG=new GiornoDisponibilità(0, data, eM.getId());
        int i;
        
        try
        {
            //readMaestro
            eM=MaestroDAO.readMaestro(eM.getEmail(), eM.getPassword());
            assertTrue("L'utente non e' registrato/credenziali errate", eM != null);

            //checkGiornoMaestro
            i=GiornoDisponibilitàDAO.checkGiornoMaestro(data, eM.getId());
            assertTrue(" ERRORE: data non consentita. Il Maestro ha gia inserito questo giorno di Disponibilita", i==0 );

        }
        catch(DBConnectionException|DAOException e)
        {
            e.printStackTrace();
        }
    }
    
    

    @Test
    public void inserisciGiorno_GiornoGiaInseritoTest()
    {
        Maestro eM= new Maestro(1, "Paolo", "Ventresca", 1234567890, "paolo.ventresca@example.com", "password123");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate data = LocalDate.parse("20/05/2025", formatter);
        GiornoDisponibilità eG=new GiornoDisponibilità(0, data, eM.getId());
        int i;
        
        try
        {
            //readMaestro
            eM=MaestroDAO.readMaestro(eM.getEmail(), eM.getPassword());
            assertTrue("Il maestro non è registrato/credenziali errate", eM != null);

            //checkGiornoMaestro
            i=GiornoDisponibilitàDAO.checkGiornoMaestro(data, eM.getId());
            assertTrue(" ERRORE: data non consentita.Il Maestro ha gia inserito questo giorno di Disponibilita", i!=0 );

        }
        catch(DBConnectionException|DAOException e)
        {
            e.printStackTrace();
        }
    }




    @Test
    public void modificaGiorno_inputValidiTest()
    {
        Maestro eM= new Maestro(1, "Paolo", "Ventresca", 1234567890, "paolo.ventresca@example.com", "password123");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate data = LocalDate.parse("20/05/2025", formatter);
        LocalDate nuovadata = LocalDate.parse("25/05/2500", formatter);
        GiornoDisponibilità eG=new GiornoDisponibilità(0, data, eM.getId());
        int i;

        try
        {
            //readMaestro
            eM=MaestroDAO.readMaestro(eM.getEmail(), eM.getPassword());
            assertTrue("L'utente non e' registrato/credenziali errate", eM != null);

            //checkGiornoMaestro
            i=GiornoDisponibilitàDAO.checkGiornoMaestro(data, eM.getId());
            assertTrue(" ERRORE: data non presente, impossibile modificare", i!=0 );

            //checkGiornoMaestro
            i=GiornoDisponibilitàDAO.checkGiornoMaestro(nuovadata, eM.getId());
            assertTrue(" ERRORE: data non consentita.Il Maestro ha gia inserito questo giorno di Disponibilita", i==0 );

        }
        catch(DBConnectionException|DAOException e)
        {
            e.printStackTrace();
        }
    }




    @Test
    public void modificaGiorno_dataDaModificareNonPresenteTest()
    {
        Maestro eM= new Maestro(1, "Paolo", "Ventresca", 1234567890, "paolo.ventresca@example.com", "password123");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate data = LocalDate.parse("25/05/2500", formatter);
        LocalDate nuovadata = LocalDate.parse("25/05/2500", formatter);
        GiornoDisponibilità eG=new GiornoDisponibilità(0, data, eM.getId());
        int i;
        
        try
        {
            //readMaestro
            eM=MaestroDAO.readMaestro(eM.getEmail(), eM.getPassword());
            assertTrue("L'utente non e' registrato/credenziali errate", eM != null);

            //checkGiornoMaestro
            i=GiornoDisponibilitàDAO.checkGiornoMaestro(data, eM.getId());
            assertTrue(" ERRORE: data non presente, impossibile modificare", i==0 );

            //checkGiornoMaestro
            i=GiornoDisponibilitàDAO.checkGiornoMaestro(nuovadata, eM.getId());
            assertTrue(" ERRORE: data non consentita.Il Maestro ha gia inserito questo giorno di Disponibilita", i==0 );

        }
        catch(DBConnectionException|DAOException e)
        {
            e.printStackTrace();
        }
    }




    @Test
    public void modificaGiorno_nuovaDataPresenteTest()
    {
        Maestro eM= new Maestro(1, "Paolo", "Ventresca", 1234567890, "paolo.ventresca@example.com", "password123");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate data = LocalDate.parse("20/05/2025", formatter);
        LocalDate nuovadata = LocalDate.parse("20/05/2025", formatter);
        GiornoDisponibilità eG=new GiornoDisponibilità(0, data, eM.getId());
        int i;
        
        try
        {
            //readMaestro
            eM=MaestroDAO.readMaestro(eM.getEmail(), eM.getPassword());
            assertTrue("L'utente non e' registrato/credenziali errate", eM != null);

            //checkGiornoMaestro
            i=GiornoDisponibilitàDAO.checkGiornoMaestro(data, eM.getId());
            assertTrue(" ERRORE: data non presente, impossibile modificare", i!=0 );

            
            //checkGiornoMaestro
            i=GiornoDisponibilitàDAO.checkGiornoMaestro(nuovadata, eM.getId());
            assertTrue(" ERRORE: data non consentita.Il Maestro ha gia inserito questo giorno di Disponibilita", i!=0 );

        }
        catch(DBConnectionException|DAOException e)
        {
            e.printStackTrace();
        }
    }




}
