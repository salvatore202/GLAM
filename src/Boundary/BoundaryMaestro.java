package Boundary;

import Control.GestioneLezioni;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BoundaryMaestro 
{
    static Scanner scan= new Scanner(System.in);

    public static void main(String[] args) 
    {
        boolean exit=false;

        while(!exit)
        {
            System.out.println(" Maestro ");
            System.out.println(" 1. Inserisci Giorno di Disponibilità");
            System.out.println(" 2. Modifica Giorno di Disponibilità");
            System.out.println(" 3. Consulta Lezioni Svolte");
            System.out.println(" 4. ESCI");

            String sceltaOP = scan.nextLine();

            switch (sceltaOP) 
            {
                case "1" -> inserisciGiorno();

                case "2" -> modificaGiorno();
                
                case "3" -> System.out.println(" Operazione non ancora disponibilie. Ci scusiamo per il disagio");

                case "4" -> exit=true;

                default -> throw new AssertionError(" scelta non consentita ");
            }
        }
    }



    //UC6 - INSERISCIGIORNO
    public static void inserisciGiorno()
    {
        String email=null;
        String password=null;

        
        boolean inputValido = false;
        boolean stop=true;

        LocalDate data=null;

        GestioneLezioni gestioneLezioni = GestioneLezioni.getInstance();


        while(!inputValido) 
        {
			System.out.println("Inserisci email ");
			email = scan.nextLine();

			if(email.length() <= 50) 
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

                if(GestioneLezioni.verificaMaestro(email, password))  
                {
                    do
                    {
                        
                        
                        while (!inputValido) 
                        {
                            try 
                            {
                                System.out.println("Inserisci la DATA (dd/MM/yyyy):");
                                String sceltaData=scan.nextLine();
                               
                                //CONTROLLO FRORMATO DI INSERIMENTO
                                String patternString="^([0-2][0-9]|3[0-1])/(0[1-9]|1[0-2])/([0-9]{4})$";
                                Pattern pattern = Pattern.compile(patternString);
                                Matcher matcher = pattern.matcher(sceltaData);

                                //se l'input è nel formato corretto
                                if (matcher.matches())
                                {
                                    inputValido = true;

                                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                                    data = LocalDate.parse(sceltaData, formatter);  
                                }
                                else
                                    System.out.println(" Data inserita non esistente oppure formato errato. Riprovare...");
                                                              
                            }
                            catch (IllegalArgumentException iE) 
                            {
                                System.out.println("Errore nell'acquisizione di data, riprovare..");
                                System.out.println();
                            }
                        }

                        System.out.println(" Continuare a inserire Giorni di Disponibilità ?");
                        System.out.println(" 1.  SI");
                        System.out.println(" 2.  NO");
                
                        String sceltaOP=scan.nextLine();
    
                        if(sceltaOP.equals("2"))
                            stop=false;
                        
                    } while (stop);
                    
                    System.out.println(" Riepilogo Giorno da Inserire: ");
                    System.out.println(data);
                    System.out.println();
                    
                    System.out.println(" CONFERMARE ?");
                    System.out.println(" 1.  SI");
                    System.out.println(" 2.  NO");
                    String conferma=scan.nextLine();
                    
                    if(conferma.equals("1"))
                    {
                        gestioneLezioni.inserisciGiorno(data, email, password);
                        System.out.println("");
                        System.out.println("Un nuovo giorno di disponibilità è stato inserito con successo.");
                    }
                    else
                        System.out.println(" Operazione Annullata");
                    
                }
                else
                    System.out.println("Maestro non registrato");
		
    }
 

    public static void modificaGiorno()
    {
        String email=null;
        String password=null;
        
        boolean inputValido = false;
        boolean stop=true;

        LocalDate data=null;
        LocalDate nuovadata=null;
        LocalDate nuovadata_temp;


        GestioneLezioni gestioneLezioni = GestioneLezioni.getInstance();


        while(!inputValido) 
        {
			System.out.println("Inserisci email ");
			email = scan.nextLine();

			if(email.length() <= 50) 
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

                if(GestioneLezioni.verificaMaestro(email, password))  
                {
                    do
                    {
                        
                        
                        while (!inputValido) 
                        {
                            try 
                            {
                                System.out.println("Inserisci la DATA DA MODIFICARE:");
                                String sceltaData=scan.nextLine();

                                //CONTROLLO FRORMATO DI INSERIMENTO
                                String patternString="^([0-2][0-9]|3[0-1])/(0[1-9]|1[0-2])/([0-9]{4})$";
                                Pattern pattern = Pattern.compile(patternString);
                                Matcher matcher = pattern.matcher(sceltaData);

                                //se l'input è nel formato corretto
                                if (matcher.matches())
                                {
                                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                                    LocalDate data_temp = LocalDate.parse(sceltaData, formatter); 
                                    if(data_temp.isAfter(LocalDate.now())) 
                                    {
                                        data=data_temp;
                                        inputValido = true;
                                    }
                                    else
                                        System.out.println(" Data inserita non esistente oppure formato errato. Riprovare...");
                                        
                                }
                            }
                            catch (IllegalArgumentException iE) 
                            {
                                System.out.println("Errore nell'acquisizione di data riprovare..");
                                System.out.println();
                            }
                        }

                        inputValido=false;

                        while (!inputValido) 
                        {
                            try 
                            {
                                System.out.println("Inserisci la NUOVA DATA :");
                                String sceltaData=scan.nextLine();

                                //CONTROLLO FRORMATO DI INSERIMENTO
                                String patternString="^([0-2][0-9]|3[0-1])/(0[1-9]|1[0-2])/([0-9]{4})$";
                                Pattern pattern = Pattern.compile(patternString);
                                Matcher matcher = pattern.matcher(sceltaData);

                                //se l'input è nel formato corretto
                                if (matcher.matches())
                                {
                                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                                    nuovadata_temp = LocalDate.parse(sceltaData, formatter); 
                                    if(nuovadata_temp.isAfter(LocalDate.now())) 
                                    {
                                        nuovadata=nuovadata_temp;
                                        inputValido = true;
                                    }
                                    else
                                        System.out.println(" Data inserita non esistente oppure formato errato. Riprovare...");
                                        
                                }
                            }
                            catch (IllegalArgumentException iE) 
                            {
                                System.out.println("Errore nell'acquisizione di data riprovare..");
                                System.out.println();
                            }
                        }

                        System.out.println(" Riepilogo Giorno modificato: ");
                        System.out.println(" Aggiornare "+data+" con "+nuovadata);
                        System.out.println();
                        
                        System.out.println(" CONFERMARE ?");
                        System.out.println(" 1.  SI");
                        System.out.println(" 2.  NO");
                        String conferma=scan.nextLine();
                        
                        if(conferma.equals("1"))
                        {
                            gestioneLezioni.modificaGiorno(data, nuovadata, email, password);
                            System.out.println("");
                            System.out.println("Giorno di disponibilità modificato con successo!");
                        }
                        else
                            System.out.println(" Operazione Annullata");


                        System.out.println(" Continuare a modificare Giorni di Disponibilità ?");
                        System.out.println(" 1.  SI");
                        System.out.println(" 2.  NO");
                
                        String sceltaOP=scan.nextLine();
    
                        if(sceltaOP.equals("2"))
                            stop=false;
                        
                    } while (stop);
                    
                }
                else
                    System.out.println("Maestro non registrato");
		
    }

}
