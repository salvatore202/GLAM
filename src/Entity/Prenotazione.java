package Entity;

import DAO.PrenotazioneDAO;
import Exception.DAOException;
import Exception.DBConnectionException;

public class Prenotazione 
{
    int IdPrenotazione;
    float costo=0;
    int IdStudente;
    int IdLezione;
    public Prenotazione(){}
    public Prenotazione(int IDPRENOTAZIONE, float COSTO, int IDLEZIONE, int IDSTUDENTE)
    {
        this.IdPrenotazione=IDPRENOTAZIONE;
        this.costo=COSTO;
        this.IdStudente=IDSTUDENTE;
        this.IdLezione=IDLEZIONE;
    }

    //getter functions
    public int getIdPrenotazione() {return IdPrenotazione;}
    public float getCosto() {return costo;}
    public int getIdStudente() {return IdStudente;}
    public int getIdLezione() {return IdLezione;}

    //setter functions
    public void setIdPrenotazione(int idPrenotazione) {IdPrenotazione = idPrenotazione;}
    public void setCosto(float costo) {this.costo = costo;}
    public void setIdStudente(int idStudente) {IdStudente = idStudente;}
    public void setIdLezione(int idLezione) {IdLezione = idLezione;}

    //save
    public void savePrenotazione() throws DAOException, DBConnectionException 
    {
       PrenotazioneDAO.createPrenotazione(this);
    }
}
