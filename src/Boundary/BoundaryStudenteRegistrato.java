package Boundary;

import Control.GestioneLezioni;
import DAO.GiornoDisponibilitàDAO;
import DAO.LezioneDAO;
import DAO.MaestroDAO;
import Entity.GiornoDisponibilità;
import Entity.Maestro;
import Exception.DAOException;
import Exception.DBConnectionException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BoundaryStudenteRegistrato 
{
     static Scanner scan= new Scanner(System.in);

    public static void main(String[] args) 
    {
        boolean exit=false;

        while(!exit)
        {
            System.out.println();
            System.out.println(" Studente Registrato");
            System.out.println(" 1. Visualizza Disponibilità");
            System.out.println(" 2. Cerca Maestro");
            System.out.println(" 3. Prenota Lezione");
            System.out.println(" 4. Visualizza elenco lezioni svolte in questo mese");
            System.out.println(" 5. Disdici Lezione");
            System.out.println(" 6. ESCI");


            String sceltaOP = scan.nextLine();

            switch (sceltaOP) 
            {
                case "1" -> visualizzaDisponibilità();

                case "2" -> CercaMaestro();
                
                case "3" -> prenotaLezione();
                                                
                case "4" -> System.out.println(" Operazione non ancora disponibilie. Ci scusiamo per il disagio");

                case "5" -> disdiciLezione();

                case "6" -> exit=true;

                default -> throw new AssertionError(" scelta non consentita ");
            }
        }
    }




    //UC3 - PRENOTALEZIONE
    public static void prenotaLezione() 
    {   
        String username=null;
        String password=null;
        ArrayList<String> cognomiMaestro= new ArrayList<>();
        ArrayList<String> nomiMaestro=new ArrayList<>();
        ArrayList<LocalDate> date = new ArrayList<>();
        ArrayList<LocalTime> ore = new ArrayList<>();
        ArrayList<String> strumenti=new ArrayList<>();

        boolean stop=true;
        boolean inputValido = false;
        boolean inputStrumento=false;
        boolean inputData=false;
        boolean inputOra=false;
        boolean inputLivello=false;

        ArrayList<String> livelli= new ArrayList<>();

        GestioneLezioni gestioneLezioni = GestioneLezioni.getInstance();


        while(!inputValido) 
        {
			System.out.println("Inserisci username ");
			username = scan.nextLine();

			if(username.length() <= 50) 
				inputValido = true;
            else
                System.out.println("username non valido");
		}			
		
		inputValido = false;
		
		while(!inputValido) 
        {
			System.out.println("Inserisci la password ");
			password = scan.nextLine();

			if(password.length() <= 50 && password.length() >= 8) 
				inputValido = true;
            else
                System.out.println("password non valida");
		}
				
                if(GestioneLezioni.verificaStudente(username, password)) 
                {
                    do
                    {
                        inputValido = false;

                        while (!inputValido) 
                        {
                            try 
                            {
                                //INPUT STRUMENTO
                                do
                                {
                                    System.out.println(" Inserisci lo STRUMENTO della lezione :"); 
                                    String sceltaStrumento=scan.nextLine().toUpperCase();
                                    if(sceltaStrumento.equals("PIANOFORTE") || sceltaStrumento.equals("CHITARRA") || sceltaStrumento.equals("VIOLINO") || sceltaStrumento.equals("BATTERIA") || sceltaStrumento.equals("BASSO") || sceltaStrumento.equals("SASSOFONO"))
                                    {
                                        strumenti.add(sceltaStrumento); //verificare che il database funzioni con le lettere maiuscole
                                        inputStrumento=true;
                                    }
                                    else
                                        System.out.println(" Non vengono erogate lezioni per lo Strumento scelto. Riprovare scegliendo un altro strumento...");

                                }while(!inputStrumento);

                                boolean inputNome=false;


                                while(!inputNome) 
                                {
                                    //INPUT NOME MAESTRO
                                    System.out.println("Inserisci il NOME del Maestro");
                                    nomiMaestro.add(scan.nextLine());
                        
                                    if(scan.nextLine().length() <= 50) 
                                        inputNome = true;
                                    else
                                        System.out.println("nome Maestro non valido");
                                }		
                                
                                
                                boolean inputCognome=false;
                                
                                
                                while(!inputCognome) 
                                {
                                    //INPUT COGNOME MAESTRO
                                    System.out.println(" Inserisci il COGNOME del Maestro ");
                                    cognomiMaestro.add(scan.nextLine());

                                    if(scan.nextLine().length() <= 50 ) 
                                    inputCognome = true;
                                    else
                                        System.out.println("cognome Maestro non valido");
                                }
                                

                                //INPUT DATA
                                do
                                {
                                    System.out.println("Inserisci la DATA (dd/MM/yyyy) :");
                                    String sceltaData=scan.nextLine();

                                    //CONTROLLO FRORMATO DI INSERIMENTO
                                    String patternString="^([0-2][0-9]|3[0-1])/(0[1-9]|1[0-2])/([0-9]{4})$";
                                    Pattern pattern = Pattern.compile(patternString);
                                    Matcher matcher = pattern.matcher(sceltaData);

                                    //se l'input è nel formato corretto
                                    if (matcher.matches())
                                    {
                                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                                        LocalDate nuovadata_temp = LocalDate.parse(sceltaData, formatter); 
                                        if(nuovadata_temp.isAfter(LocalDate.now())) 
                                        {
                                            date.add(nuovadata_temp);
                                            inputData = true;
                                        }
                                    }
                                        else
                                            System.out.println(" Data inserita non esistente oppure formato errato. Riprovare...");

                                }while(!inputData);



                                //INPUT ORA
                                do
                                {
                                    System.out.println(" Inserisci l'ORA (HH:mm)");
                                    String orarioTemp = scan.nextLine();
                                    
                                    if(orarioTemp.equals("17:00") ||orarioTemp.equals("18:00") ||orarioTemp.equals("19:00") ||orarioTemp.equals("20:00"))
                                    {
                                        ore.add(LocalTime.parse(orarioTemp));
                                        inputOra=true;
                                    }
                                    else
                                        System.out.println("Non ci sono Lezioni per l'orario scelto. Le Lezioni iniziano solo alle 17:00, alle 18:00, alle 19:00 oppure alle 20:00. Riprovare...");
                                }while(!inputOra);
                    
                                
                                
                                
                                //INPUT LIVELLO
                                do
                                {
                                    System.out.println(" Inserisci il LIVELLO della lezione :"); 
                                    String sceltaLivello=scan.nextLine().toUpperCase();
                                    if(sceltaLivello.equals("BASE") || sceltaLivello.equals("INTERMEDIO"))
                                    {
                                        livelli.add(sceltaLivello); //verificare che il database funzioni con le lettere maiuscole
                                        inputLivello=true;
                                    }
                                    else
                                        System.out.println(" Le lezioni possono essere solo di 2 livelli:  BASE (costo di 15.00 euro) oppure INTERMEDIO (costo di 30.00 euro)");

                                }while(!inputLivello);
                                
                                inputValido = true;

                            
                            }
                            catch (IllegalArgumentException iE) 
                            {
                                System.out.println("Errore nell'acquisizione di nomeMaestro, cognomeMaestro, data e ora riprovare..");
                                System.out.println();
                            }
                        }

                        System.out.println(" Continuare a prenotare ?");
                        System.out.println(" 1.  SI");
                        System.out.println(" 2.  NO");
                
                        String sceltaOP=scan.nextLine();
    
                        if(sceltaOP.equals("2"))
                            stop=false;
                        
                    } while (stop);
                    

                
                        System.out.println(" Riepilogo Prenotazione: ");
                        for (int j = 0; j < nomiMaestro.size(); j++)
                        {
                            System.out.println();
                            System.out.println(nomiMaestro.get(j));
                            System.out.println(cognomiMaestro.get(j)); 
                            System.out.println(strumenti.get(j));
                            System.out.println(date.get(j));
                            System.out.println(ore.get(j));
                            System.out.println(livelli.get(j));
                            System.out.println();
                        }
                        
                        System.out.println(" CONFERMARE ?");
                        System.out.println(" 1.  SI");
                        System.out.println(" 2.  NO");
                        String conferma=scan.nextLine();
                        
                        if(conferma.equals("1"))
                            gestioneLezioni.prenotaLezione(username, password, nomiMaestro, cognomiMaestro, date, ore, strumenti, livelli);
                        else
                            System.out.println(" Operazione Annullata");
                    
                    
                }
                else
                    System.out.println("Studente non registrato");
    }




    //UC4 - DISDICILEZIONE
    public static void disdiciLezione() 
    {
        String username = null;
        String password = null;

        String cognomeMaestro= null;
        String nomeMaestro=null;
        String strumento=null;
        boolean inputStrumento=false;
        boolean inputData=false;
        boolean inputOra=false;
        String conferma;
        LocalDate data=null;
        LocalTime ora=null;
        boolean inputValido = false;

        GestioneLezioni gestioneLezioni = GestioneLezioni.getInstance();


        while(!inputValido) 
        {
			System.out.println("Inserisci username ");
			username = scan.nextLine();

			if(username.length() <= 50) 
				inputValido = true;
		}			
		
		inputValido = false;
		
		while(!inputValido) 
        {
			System.out.println("Inserisci la password ");
			password = scan.nextLine();

			if(password.length() <= 50 && password.length() >= 8) 
				inputValido = true;
		}
		
		inputValido = false;
		
                
                if(GestioneLezioni.verificaStudente(username, password)) 
                {
                        
                        while (!inputValido) 
                        {
                            try 
                            {  
                                //INPUT STRUMENTO
                                do
                                {
                                    System.out.println(" Inserisci lo STRUMENTO della lezione :"); 
                                    String sceltaStrumento=scan.nextLine().toUpperCase();
                                    if(sceltaStrumento.equals("PIANOFORTE") || sceltaStrumento.equals("CHITARRA") || sceltaStrumento.equals("VIOLINO") || sceltaStrumento.equals("BATTERIA") || sceltaStrumento.equals("BASSO") || sceltaStrumento.equals("SASSOFONO"))
                                    {
                                        strumento=sceltaStrumento; //verificare che il database funzioni con le lettere maiuscole
                                        inputStrumento=true;
                                    }
                                    else
                                        System.out.println(" Non vengono erogate lezioni per lo Strumento scelto. Riprovare scegliendo un altro strumento...");

                                }while(!inputStrumento);

                                boolean inputNome=false;

                                
                                while(!inputNome) 
                                {
                                    //INPUT NOME MAESTRO
                                    System.out.println("Inserisci il NOME del Maestro");
                                    nomeMaestro=scan.nextLine();
                        
                                    if(nomeMaestro.length() <= 50) 
                                        inputNome = true;
                                    else
                                        System.out.println("nome Maestro non valido");
                                }		
                                
                                
                                boolean inputCognome=false;
                                
                                
                                while(!inputCognome) 
                                {
                                    //INPUT COGNOME MAESTRO
                                    System.out.println(" Inserisci il COGNOME del Maestro ");
                                    cognomeMaestro=scan.nextLine();

                                    if(cognomeMaestro.length() <= 50 ) 
                                    inputCognome = true;
                                    else
                                        System.out.println("cognome Maestro non valido");
                                }
                                


                                //INPUT DATA
                                do
                                {
                                    System.out.println("Inserisci la DATA (dd/MM/yyyy) :");
                                    String sceltaData=scan.nextLine();

                                    //CONTROLLO FRORMATO DI INSERIMENTO
                                    String patternString="^([0-2][0-9]|3[0-1])/(0[1-9]|1[0-2])/([0-9]{4})$";
                                    Pattern pattern = Pattern.compile(patternString);
                                    Matcher matcher = pattern.matcher(sceltaData);

                                    //se l'input è nel formato corretto
                                    if (matcher.matches())
                                    {
                                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                                        data=LocalDate.parse(sceltaData, formatter);  

                                        inputData=true;
                                    }
                                    else
                                        System.out.println(" Data inserita non esistente oppure formato errato. Riprovare...");

                                }while(!inputData);



                                //INPUT ORA
                                do
                                {
                                    System.out.println(" Inserisci l'ORA (HH:mm)");
                                    String orarioTemp = scan.nextLine();
                                    
                                    if(orarioTemp.equals("17:00") ||orarioTemp.equals("18:00") ||orarioTemp.equals("19:00") ||orarioTemp.equals("20:00"))
                                    {
                                        ora=LocalTime.parse(orarioTemp);
                                        inputOra=true;
                                    }
                                    else
                                        System.out.println("Non ci sono Lezioni per l'orario scelto. Le Lezioni iniziano solo alle 17:00, alle 18:00, alle 19:00 oppure alle 20:00. Riprovare...");
                                }while(!inputOra);
                    
                                  
                                inputValido = true;
                            }
                            catch (IllegalArgumentException iE) 
                            {
                                System.out.println("Errore nell'acquisizione di nomeMaestro, cognomeMaestro, data, ora estrumento...riprovare..");
                                System.out.println();
                            }
                        }

                        System.out.println(" Riepilogo Disdetta: ");
                        System.out.println();
                        System.out.println(data);
                        System.out.println(ora);
                        System.out.println(nomeMaestro);
                        System.out.println(cognomeMaestro);
                        System.out.println(strumento);


                        System.out.println();
                        System.out.println(" CONFERMARE ?");
                        System.out.println(" 1.  SI");
                        System.out.println(" 2.  NO");
                        conferma=scan.nextLine();

                        if(conferma.equals("1"))
                        {
                            gestioneLezioni.disdiciLezione(username, password, data, ora, nomeMaestro, cognomeMaestro,strumento); 
                            System.out.println("");
                        }
                        else
                            System.out.println(" Operazione Annullata");


                        //Invio Notifica alla segreteria via email
                }
                else
                    System.out.println("Studente non registrato");
    }





    public static void CercaMaestro()
    {
        boolean inputStrumento=false;
                    String strumento=null;

                        //INPUT STRUMENTO
                        do
                        {
                            System.out.println(" Inserisci lo STRUMENTO della lezione :"); 
                            String sceltaStrumento=scan.nextLine().toUpperCase();
                            if(sceltaStrumento.equals("PIANOFORTE") || sceltaStrumento.equals("CHITARRA") || sceltaStrumento.equals("VIOLINO") || sceltaStrumento.equals("BATTERIA") || sceltaStrumento.equals("BASSO") || sceltaStrumento.equals("SASSOFONO"))
                            {
                                strumento = sceltaStrumento; //verificare che il database funzioni con le lettere maiuscole
                                inputStrumento=true;
                            }
                            else
                                System.out.println(" Non vengono erogate lezioni per lo Strumento scelto. Riprovare scegliendo un altro strumento...");

                        }while(!inputStrumento);

                        try
                        {
                            ArrayList<Maestro> maestri=MaestroDAO.CercaMaestro(strumento);
                            System.out.println();
                            System.out.println(" Dati Maestro trovato: ");
                            System.out.println(maestri.get(0).getNome());
                            System.out.println(maestri.get(0).getCognome());
                            System.out.println(maestri.get(0).getEmail());
                            System.out.println(maestri.get(0).getNumeroDiTelefono());

                        }
                        catch (DAOException | DBConnectionException e) 
                        {
                            System.out.println("Qualcosa e' andato storto durante il salvataggio della prenotazione in prenotaLezione: "+e.getMessage());
                        }
    }





    public static void visualizzaDisponibilità()
    {
        String cognomeMaestro= null;
        String nomeMaestro;

        boolean inputNome=false;

                                
        while(!inputNome) 
        {
            //INPUT NOME MAESTRO
            System.out.println("Inserisci il NOME del Maestro");
            nomeMaestro=scan.nextLine();

            if(nomeMaestro.length() <= 50) 
                inputNome = true;
            else
                System.out.println("nome Maestro non valido");
        }		
        
        
        boolean inputCognome=false;
        
        
        while(!inputCognome) 
        {
            //INPUT COGNOME MAESTRO
            System.out.println(" Inserisci il COGNOME del Maestro ");
            cognomeMaestro=scan.nextLine();

            if(cognomeMaestro.length() <= 50 && cognomeMaestro.length() >= 8) 
                inputCognome = true;
            else
                System.out.println("cognome Maestro non valido");
        }

        try
        {
            Maestro eM=MaestroDAO.readMaestrobySurname(cognomeMaestro);

            try
            {
                ArrayList<GiornoDisponibilità> giorni=GiornoDisponibilitàDAO.readGiorniDisponibilità(eM.getId());

                for (int i = 0; i <giorni.size(); i++) 
                {
                    System.out.println();
                    System.out.println(giorni.get(i).getData());
                    System.out.println();

                    for (int j = 0 ; j < LezioneDAO.readLezioniDisponibili(giorni.get(i), eM.getId()).size(); j++)
                    {
                        System.out.println(LezioneDAO.readLezioniDisponibili(giorni.get(i), eM.getId()).get(j).getOra());
                    }
                }
            
            }
            catch (DAOException | DBConnectionException e) 
            {
                System.out.println("Qualcosa e' andato storto durante la ricerca dei Giorni in readGiorniDisponibilità: "+e.getMessage());
            }
            
        }
        catch (DAOException | DBConnectionException e1) 
        {
            System.out.println("Qualcosa e' andato storto durante la ricerca del maestro" );
        }
    }
}


