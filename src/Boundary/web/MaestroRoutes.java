package Boundary.web;

import Control.GestioneLezioni;
import DAO.CatalogoDAO;
import DAO.GiornoDisponibilitàDAO;
import DAO.LezioneDAO;
import DAO.MaestroDAO;
import Entity.GiornoDisponibilità;
import Entity.Lezione;
import Entity.Maestro;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Rotte lato Maestro: equivalenti web delle opzioni 1 e 2 di BoundaryMaestro
 * ("Inserisci Giorno di Disponibilità", "Modifica Giorno di Disponibilità"),
 * piu' una nuova rotta di sola lettura che implementa concretamente l'opzione
 * 3 ("Consulta Lezioni Svolte"), lasciata come placeholder non implementato
 * nella CLI originale.
 *
 * GestioneLezioni.inserisciGiorno/modificaGiorno (Control originale, non
 * modificata) sono metodi void che comunicano l'esito solo stampando su
 * console: qui il risultato viene dedotto rileggendo lo stato dal database
 * subito dopo la chiamata (stesso approccio usato in StudenteRoutes).
 */
public final class MaestroRoutes
{
    private MaestroRoutes() {}

    public static void register(Router router)
    {
        router.add("POST", "/api/maestro/giorni", MaestroRoutes::inserisciGiorno);
        router.add("PUT", "/api/maestro/giorni", MaestroRoutes::modificaGiorno);
        router.add("GET", "/api/maestro/lezioni-svolte", MaestroRoutes::lezioniSvolte);
    }

    private static void inserisciGiorno(Router.RequestContext ctx) throws Exception
    {
        Map<String, Object> body = ctx.jsonBody();
        String email = Validation.email(ctx.requireString(body, "email"));
        String password = Validation.password(ctx.requireString(body, "password"));
        LocalDate data = Validation.data(ctx.requireString(body, "data"), "Data");

        Maestro maestro = autenticaMaestro(email, password);

        if (GiornoDisponibilitàDAO.checkGiornoMaestro(data, maestro.getId()) != 0)
            throw ApiException.conflict("Il Maestro ha gia' inserito questo giorno di disponibilita'");

        GestioneLezioni gestioneLezioni = GestioneLezioni.getInstance();
        var risultato = ConsoleCapture.captureVoid(() -> gestioneLezioni.inserisciGiorno(data, email, password));

        GiornoDisponibilità giornoCreato = GiornoDisponibilitàDAO.readGiornoDisponibilità(data, maestro.getId());
        if (giornoCreato == null)
            throw ApiException.serverError("Inserimento non riuscito: " + risultato.output());

        List<Map<String, Object>> lezioni = new ArrayList<>();
        for (Lezione l : LezioneDAO.readLezioniDisponibili(giornoCreato, maestro.getId()))
            lezioni.add(Dto.lezione(l));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("successo", true);
        response.put("giorno", Dto.giorno(giornoCreato));
        response.put("lezioniCreate", lezioni);
        response.put("log", risultato.output());
        ctx.sendJson(201, response);
    }

    private static void modificaGiorno(Router.RequestContext ctx) throws Exception
    {
        Map<String, Object> body = ctx.jsonBody();
        String email = Validation.email(ctx.requireString(body, "email"));
        String password = Validation.password(ctx.requireString(body, "password"));
        LocalDate data = Validation.dataFutura(ctx.requireString(body, "data"), "Data");
        LocalDate nuovaData = Validation.dataFutura(ctx.requireString(body, "nuovaData"), "Nuova data");

        Maestro maestro = autenticaMaestro(email, password);

        if (GiornoDisponibilitàDAO.checkGiornoMaestro(data, maestro.getId()) == 0)
            throw ApiException.notFound("Il giorno indicato non e' presente nel sistema");
        if (GiornoDisponibilitàDAO.checkGiornoMaestro(nuovaData, maestro.getId()) != 0)
            throw ApiException.conflict("Il nuovo giorno indicato e' gia' presente nel sistema");

        GestioneLezioni gestioneLezioni = GestioneLezioni.getInstance();
        var risultato = ConsoleCapture.captureVoid(() -> gestioneLezioni.modificaGiorno(data, nuovaData, email, password));

        GiornoDisponibilità giornoModificato = GiornoDisponibilitàDAO.readGiornoDisponibilità(nuovaData, maestro.getId());
        if (giornoModificato == null)
            throw ApiException.serverError("Modifica non riuscita: " + risultato.output());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("successo", true);
        response.put("giorno", Dto.giorno(giornoModificato));
        response.put("log", risultato.output());
        ctx.sendJson(200, response);
    }

    private static void lezioniSvolte(Router.RequestContext ctx) throws Exception
    {
        String email = Validation.email(ctx.header("X-Email"));
        String password = Validation.password(ctx.header("X-Password"));
        Maestro maestro = autenticaMaestro(email, password);

        ctx.sendJson(200, CatalogoDAO.readLezioniSvolteByMaestro(maestro.getId()));
    }

    private static Maestro autenticaMaestro(String email, String password) throws Exception
    {
        if (!GestioneLezioni.verificaMaestro(email, password))
            throw ApiException.unauthorized("Maestro non registrato o credenziali errate");
        return MaestroDAO.readMaestro(email, password);
    }
}
