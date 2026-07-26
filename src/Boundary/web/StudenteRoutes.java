package Boundary.web;

import Control.GestioneLezioni;
import DAO.CatalogoDAO;
import DAO.LezioneDAO;
import DAO.PrenotazioneDAO;
import DAO.StudenteRegistratoDAO;
import Entity.Lezione;
import Entity.Prenotazione;
import Entity.StudenteRegistrato;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Rotte lato Studente Registrato: equivalenti web delle opzioni 3 e 5 di
 * BoundaryStudenteRegistrato ("Prenota Lezione", "Disdici Lezione"), piu' una
 * nuova rotta di sola lettura che implementa concretamente l'opzione 4
 * ("Visualizza elenco lezioni svolte"), lasciata come placeholder non
 * implementato nella CLI originale.
 *
 * Nota importante su disdiciLezione: Control.GestioneLezioni.disdiciLezione
 * (classe originale, non modificata) lancia una NullPointerException non
 * gestita se lo studente prova a disdire una lezione per cui NON risulta
 * nessuna prenotazione e la data e' a 3 o piu' giorni di distanza (chiama
 * prenotazioneEffettata.getIdPrenotazione() su un riferimento null). Nella
 * CLI questo manda in crash l'intero programma. Questa rotta verifica PRIMA
 * che la prenotazione esista davvero e in caso contrario risponde con un
 * errore 409 pulito senza mai richiamare il metodo originale in quello
 * stato, cosi' il bug non viene mai innescato passando dal frontend web.
 */
public final class StudenteRoutes
{
    private StudenteRoutes() {}

    public static void register(Router router)
    {
        router.add("POST", "/api/studente/prenotazioni", StudenteRoutes::prenota);
        router.add("DELETE", "/api/studente/prenotazioni", StudenteRoutes::disdici);
        router.add("GET", "/api/studente/prenotazioni", StudenteRoutes::listMiePrenotazioni);
    }

    @SuppressWarnings("unchecked")
    private static void prenota(Router.RequestContext ctx) throws Exception
    {
        Map<String, Object> body = ctx.jsonBody();
        String username = Validation.username(ctx.requireString(body, "username"));
        String password = Validation.password(ctx.requireString(body, "password"));

        StudenteRegistrato studente = autenticaStudente(username, password);

        Object richiesteRaw = body.get("richieste");
        if (!(richiesteRaw instanceof List<?> richiesteList) || richiesteList.isEmpty())
            throw ApiException.badRequest("Il campo 'richieste' deve essere una lista non vuota di lezioni da prenotare");

        ArrayList<String> nomiMaestro = new ArrayList<>();
        ArrayList<String> cognomiMaestro = new ArrayList<>();
        ArrayList<LocalDate> date = new ArrayList<>();
        ArrayList<LocalTime> ore = new ArrayList<>();
        ArrayList<String> strumenti = new ArrayList<>();
        ArrayList<String> livelli = new ArrayList<>();

        // Pre-verifica (sola lettura) di ogni riga, per poter dare un esito preciso
        // per singola lezione dopo la chiamata al metodo originale (che e' void
        // e processa l'intera lista in un colpo solo).
        List<Lezione> lezioniTrovate = new ArrayList<>();
        List<String> motiviIndisponibilita = new ArrayList<>();

        for (Object raw : richiesteList)
        {
            Map<String, Object> item = (Map<String, Object>) raw;
            String strumento = Validation.strumento(ctx.requireString(item, "strumento"));
            String nomeMaestro = Validation.nomeOCognome(ctx.requireString(item, "nomeMaestro"), "Nome Maestro");
            String cognomeMaestro = Validation.nomeOCognome(ctx.requireString(item, "cognomeMaestro"), "Cognome Maestro");
            LocalDate data = Validation.dataFutura(ctx.requireString(item, "data"), "Data lezione");
            LocalTime ora = Validation.ora(ctx.requireString(item, "ora"));
            String livello = Validation.livello(ctx.requireString(item, "livello"));

            nomiMaestro.add(nomeMaestro);
            cognomiMaestro.add(cognomeMaestro);
            date.add(data);
            ore.add(ora);
            strumenti.add(strumento);
            livelli.add(livello);

            Lezione eL = LezioneDAO.readLezione(data, ora, strumento, cognomeMaestro);
            if (eL == null)
            {
                lezioniTrovate.add(null);
                motiviIndisponibilita.add("Lezione non trovata (verifica Maestro, strumento, data e ora)");
            }
            else if (!LezioneDAO.checkDisponibilità(eL.getIdLezione()))
            {
                lezioniTrovate.add(eL);
                motiviIndisponibilita.add("Lezione non disponibile: gia' prenotata da un altro studente");
            }
            else
            {
                lezioniTrovate.add(eL);
                motiviIndisponibilita.add(null);
            }
        }

        GestioneLezioni gestioneLezioni = GestioneLezioni.getInstance();
        var risultato = ConsoleCapture.captureVoid(() ->
            gestioneLezioni.prenotaLezione(username, password, nomiMaestro, cognomiMaestro, date, ore, strumenti, livelli));

        List<Map<String, Object>> esiti = new ArrayList<>();
        boolean tutteRiuscite = true;
        for (int i = 0; i < richiesteList.size(); i++)
        {
            Map<String, Object> esito = new LinkedHashMap<>();
            esito.put("strumento", strumenti.get(i));
            esito.put("maestro", nomiMaestro.get(i) + " " + cognomiMaestro.get(i));
            esito.put("data", date.get(i).toString());
            esito.put("ora", ore.get(i).toString());
            esito.put("livello", livelli.get(i));

            Lezione eL = lezioniTrovate.get(i);
            Prenotazione confermata = eL == null ? null : PrenotazioneDAO.readPrenotazione(eL.getIdLezione(), studente.getIdStudente());

            if (confermata != null)
            {
                esito.put("esito", "prenotata");
                esito.put("idPrenotazione", confermata.getIdPrenotazione());
                esito.put("costo", confermata.getCosto());
            }
            else
            {
                tutteRiuscite = false;
                esito.put("esito", "non_riuscita");
                esito.put("motivo", motiviIndisponibilita.get(i) != null ? motiviIndisponibilita.get(i) : "Prenotazione non riuscita");
            }
            esiti.add(esito);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("successo", tutteRiuscite);
        response.put("risultati", esiti);
        response.put("log", risultato.output());
        ctx.sendJson(tutteRiuscite ? 201 : 207, response);
    }

    private static void disdici(Router.RequestContext ctx) throws Exception
    {
        Map<String, Object> body = ctx.jsonBody();
        String username = Validation.username(ctx.requireString(body, "username"));
        String password = Validation.password(ctx.requireString(body, "password"));
        String strumento = Validation.strumento(ctx.requireString(body, "strumento"));
        String nomeMaestro = Validation.nomeOCognome(ctx.requireString(body, "nomeMaestro"), "Nome Maestro");
        String cognomeMaestro = Validation.nomeOCognome(ctx.requireString(body, "cognomeMaestro"), "Cognome Maestro");
        LocalDate data = Validation.data(ctx.requireString(body, "data"), "Data lezione");
        LocalTime ora = Validation.ora(ctx.requireString(body, "ora"));

        StudenteRegistrato studente = autenticaStudente(username, password);

        Lezione eL = LezioneDAO.readLezione(data, ora, strumento, cognomeMaestro);
        if (eL == null)
            throw ApiException.notFound("Lezione non trovata: disdetta non disponibile");

        Prenotazione prenotazioneEsistente = PrenotazioneDAO.readPrenotazione(eL.getIdLezione(), studente.getIdStudente());
        if (prenotazioneEsistente == null)
            throw ApiException.conflict("Non risulta nessuna prenotazione per questa lezione da parte di questo studente");

        GestioneLezioni gestioneLezioni = GestioneLezioni.getInstance();
        var risultato = ConsoleCapture.captureVoid(() ->
            gestioneLezioni.disdiciLezione(username, password, data, ora, nomeMaestro, cognomeMaestro, strumento));

        boolean slotLiberato = LezioneDAO.checkDisponibilità(eL.getIdLezione());
        boolean rimborsata = PrenotazioneDAO.readPrenotazione(eL.getIdLezione(), studente.getIdStudente()) == null;

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("successo", true);
        response.put("slotLiberato", slotLiberato);
        response.put("rimborsata", rimborsata);
        response.put("messaggio", rimborsata
            ? "Disdetta con almeno 3 giorni di anticipo: prenotazione eliminata."
            : "Disdetta con meno di 3 giorni di anticipo: lo slot e' stato liberato ma la prenotazione (e il relativo costo) restano registrati, come da regola di cancellazione del sistema.");
        response.put("log", risultato.output());
        ctx.sendJson(200, response);
    }

    private static void listMiePrenotazioni(Router.RequestContext ctx) throws Exception
    {
        String username = Validation.username(ctx.header("X-Username"));
        String password = Validation.password(ctx.header("X-Password"));
        StudenteRegistrato studente = autenticaStudente(username, password);

        ctx.sendJson(200, CatalogoDAO.readPrenotazioniByStudente(studente.getIdStudente()));
    }

    private static StudenteRegistrato autenticaStudente(String username, String password) throws Exception
    {
        if (!GestioneLezioni.verificaStudente(username, password))
            throw ApiException.unauthorized("Studente non registrato o credenziali errate");
        return StudenteRegistratoDAO.readStudenteRegistrato(username, password);
    }
}
