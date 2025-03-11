package Entity;
//import java.text.SimpleDateFormat;
import DAO.GiornoDisponibilitàDAO;
import Exception.DAOException;
import Exception.DBConnectionException;
import java.time.LocalDate;

public class GiornoDisponibilità 
{
    //SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    int IdData;
    int IdMaestro;
    LocalDate Data;
    //String formattedDate = dateFormat.format(Data);
    

    public GiornoDisponibilità (int IDDATA, LocalDate DATA, int IDMAESTRO)
    {
        this.Data=DATA;
        this.IdData=IDDATA;
        this.IdMaestro=IDMAESTRO;
    }

    //getter functions
    public int getIdData() {return IdData;}
    public LocalDate getData() {return Data;}
    public int getIdMaestro() {return IdMaestro;}

    //setter functions
    public void setIdData(int idData) {IdData = idData;}
    public void setData(LocalDate data) {Data = data;}    
    public void setIdMaestro(int idMaestro) {IdMaestro = idMaestro;}

    //save
    public void saveGiornoDisponibilità() throws DAOException, DBConnectionException 
    {
        GiornoDisponibilitàDAO.createGiornoDisponibilità(this);
    }
}
