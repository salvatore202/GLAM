package Entity;

/**
 * Entity aggiuntiva rispetto al progetto originale.
 *
 * La tabella STRUMENTO esiste gia' nello schema SQL (Database/MySql/databaseglam10.sql)
 * ed e' gia' interrogata "inline" da MaestroDAO.CercaMaestro e da LezioneDAO.readLezione,
 * ma non aveva mai avuto una classe Entity dedicata. E' stata aggiunta qui, senza
 * modificare nessun file esistente, per poter costruire in modo pulito le rotte
 * REST del frontend (elenco strumenti, strumenti insegnati da un Maestro).
 */
public class Strumento
{
    int idStrumento;
    String nome;
    int idMaestro;

    public Strumento(int IDSTRUMENTO, String NOME, int IDMAESTRO)
    {
        this.idStrumento = IDSTRUMENTO;
        this.nome = NOME;
        this.idMaestro = IDMAESTRO;
    }

    public int getIdStrumento() { return idStrumento; }
    public String getNome() { return nome; }
    public int getIdMaestro() { return idMaestro; }
}
