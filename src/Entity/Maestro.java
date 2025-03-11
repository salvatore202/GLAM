package Entity;

public class Maestro 
{
    String nome;
    String cognome;
    long numeroDiTelefono;
    String email;
    String password;
    int idMaestro;

    public Maestro(int IDMAESTRO, String NOME, String COGNOME, long NUMERODITELEFONO, String EMAIL, String PASSWORD)
    {
        this.idMaestro=IDMAESTRO;
        this.nome=NOME;
        this.cognome=COGNOME;
        this.numeroDiTelefono=NUMERODITELEFONO;
        this.email=EMAIL;
        this.password=PASSWORD;
    }   

    //getter functions 
    public String getPassword() {return password;}
    public int getId(){return idMaestro;}
    public String getNome(){return nome;}
    public String getCognome(){return cognome;}
    public String getEmail(){return email;}
    public long getNumeroDiTelefono(){return numeroDiTelefono;}

    //setter functions
    public void setNumeroDiTelefono(long NUMERODITELEFONO){numeroDiTelefono=NUMERODITELEFONO;}
    public void setEmail(String EMAIL){email=EMAIL;}
    public void setPassword(String password) {this.password = password;}
    public void setId(int ID){idMaestro=ID;}
    public void setNome(String NOME){nome=NOME;}
    public void setCognome(String COGNOME){cognome=COGNOME;}
}
