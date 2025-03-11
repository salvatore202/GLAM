package Entity;
import DAO.LezioneDAO;
import Exception.DAOException;
import Exception.DBConnectionException;
import java.time.LocalTime;
;
public class Lezione 
{
    LocalTime Ora;
    String Livello="BASE";
    boolean Disponibile=true;
    int IdLezione;
    int IdMaestro;
    int IdData;

    //Costruttore
    public Lezione(int IDLEZIONE, LocalTime ORA, String LIVELLO, int IDMAESTRO, int IDDATA)
    {
        this.Ora=ORA;
        this.Livello=LIVELLO;
        this.IdLezione=IDLEZIONE;
        this.IdData=IDDATA;
        this.IdMaestro=IDMAESTRO;
    }

    //getter functions
    public LocalTime getOra(){return Ora;}
    public String getLivello(){return Livello;}
    public int getIdLezione(){return IdLezione;}
    public boolean isDisponibile() {return Disponibile;}
    public int getIdMaestro() {return IdMaestro;}
    public int getIdData() {return IdData;}

    //setter functions
    public void setIdLezione(int IDLEZIONE){IdLezione=IDLEZIONE;}
    public void setOra(LocalTime ORA){Ora=ORA;}
    public void setLivello(String LIVELLO){Livello=LIVELLO;}
    public void setDisponibile(boolean disponibile) {Disponibile = disponibile;}
    public void setIdMaestro(int idMaestro) {IdMaestro = idMaestro;}
    public void setIdData(int idData) {IdData = idData;}

    //save
    public void saveLezione() throws DAOException, DBConnectionException 
    {
        LezioneDAO.createLezione(this);
    }

}
