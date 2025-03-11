package Entity;

import java.sql.Date;
public class StudenteRegistrato extends Studente 
{
    String username;
    String email;
    String Password;
    int IdStudente;
    
    
    public StudenteRegistrato(int IDSTUDENTE, String NOME, String COGNOME, Date DATA, long NUMERODITELEFONO, String USERNAME, String EMAIL, String PASSWORD)
    {
        this.Nome=NOME;
        this.Cognome=COGNOME;
        this.DataDiNascita=DATA;
        this.numeroDiTelefono=NUMERODITELEFONO;
        this.username=USERNAME;
        this.email=EMAIL;
        this.Password=PASSWORD;
        this.IdStudente=IDSTUDENTE;
    }
    
    //getter functions
    public String getUsername() {return username;}
    public String getEmail() {return email;}
    public String getPassword() {return Password;}
    public int getIdStudente() {return IdStudente;}

    //setter functions
    public void setUsername(String username) {this.username = username;}
    public void setEmail(String email) {this.email = email;}
    public void setPassword(String password) {Password = password;}
    public void setIdStudente(int idStudente) {IdStudente = idStudente;}
}
