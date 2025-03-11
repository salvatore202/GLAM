package Entity;

public abstract class Studente 
{
    protected String Nome;
    protected String Cognome;
    protected java.util.Date DataDiNascita;
    protected long numeroDiTelefono;

    //getter functions
    public String getNome(){return Nome;}
    public String getCognome(){return Cognome;}
    public java.util.Date getDatadiNascita(){return DataDiNascita;}
    public long getNumeroDiTelefono(){return numeroDiTelefono;}

    //setter functions
    public void setNome(String NOME){this.Nome=NOME;}
    public void setCognome(String COGNOME){this.Cognome=COGNOME;}
    public void setData(java.util.Date DATA){this.DataDiNascita=DATA;}
    public void setNumeroDiTelefono(long NUMERO){this.numeroDiTelefono=NUMERO;}
}
