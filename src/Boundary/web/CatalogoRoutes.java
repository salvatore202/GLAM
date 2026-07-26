package Boundary.web;

import DAO.CatalogoDAO;
import DAO.GiornoDisponibilitàDAO;
import DAO.LezioneDAO;
import DAO.MaestroDAO;
import DAO.StrumentoDAO;
import Entity.GiornoDisponibilità;
import Entity.Lezione;
import Entity.Maestro;
import Entity.Strumento;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Rotte pubbliche di catalogo: non richiedono autenticazione, cosi' come
 * "Visualizza Disponibilità" e "Cerca Maestro" non la richiedono nella CLI
 * originale (BoundaryStudenteRegistrato, opzioni 1 e 2 del menu).
 */
public final class CatalogoRoutes
{
    private CatalogoRoutes() {}

    public static void register(Router router)
    {
        router.add("GET", "/api/strumenti", CatalogoRoutes::listStrumenti);
        router.add("GET", "/api/maestri", CatalogoRoutes::listMaestri);
        router.add("GET", "/api/maestri/ricerca", CatalogoRoutes::cercaMaestro);
        router.add("GET", "/api/maestri/{id}/disponibilita", CatalogoRoutes::disponibilita);
    }

    // GET /api/strumenti -> elenco dei 6 strumenti ammessi, con i Maestri che li insegnano oggi
    private static void listStrumenti(Router.RequestContext ctx) throws Exception
    {
        Map<String, List<Map<String, Object>>> perStrumento = new LinkedHashMap<>();
        for (String nome : Validation.strumentiValidi()) perStrumento.put(nome, new ArrayList<>());

        for (Strumento s : StrumentoDAO.readAll())
        {
            Maestro m = CatalogoDAO.readMaestroById(s.getIdMaestro());
            if (m == null) continue;
            perStrumento.computeIfAbsent(s.getNome(), k -> new ArrayList<>()).add(Dto.maestro(m));
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (var entry : perStrumento.entrySet())
        {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("nome", entry.getKey());
            row.put("maestri", entry.getValue());
            result.add(row);
        }
        ctx.sendJson(200, result);
    }

    // GET /api/maestri -> tutti i Maestri con i rispettivi strumenti
    private static void listMaestri(Router.RequestContext ctx) throws Exception
    {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Maestro m : CatalogoDAO.readAllMaestri())
        {
            List<Strumento> strumenti = StrumentoDAO.readByMaestro(m.getId());
            result.add(Dto.maestro(m, strumenti));
        }
        ctx.sendJson(200, result);
    }

    // GET /api/maestri/ricerca?strumento=PIANOFORTE -> Maestri che insegnano quello strumento
    // (equivalente web di BoundaryStudenteRegistrato.CercaMaestro, ma senza il bug del
    // "maestri.get(0)" che nella CLI lancia IndexOutOfBoundsException se nessuno insegna
    // lo strumento scelto, es. BASSO nei dati seed)
    private static void cercaMaestro(Router.RequestContext ctx) throws Exception
    {
        String strumento = Validation.strumento(ctx.queryParam("strumento"));
        List<Map<String, Object>> result = new ArrayList<>();
        for (Maestro m : MaestroDAO.CercaMaestro(strumento))
        {
            result.add(Dto.maestro(m, StrumentoDAO.readByMaestro(m.getId())));
        }
        ctx.sendJson(200, result);
    }

    // GET /api/maestri/{id}/disponibilita -> giorni disponibili + lezioni libere per quel Maestro
    private static void disponibilita(Router.RequestContext ctx) throws Exception
    {
        int idMaestro = parseId(ctx.pathParam("id"));
        Maestro maestro = CatalogoDAO.readMaestroById(idMaestro);
        if (maestro == null) throw ApiException.notFound("Maestro non trovato");

        List<Map<String, Object>> giorni = new ArrayList<>();
        for (GiornoDisponibilità giorno : GiornoDisponibilitàDAO.readGiorniDisponibilità(idMaestro))
        {
            List<Map<String, Object>> lezioni = new ArrayList<>();
            for (Lezione l : LezioneDAO.readLezioniDisponibili(giorno, idMaestro))
            {
                lezioni.add(Dto.lezione(l));
            }
            Map<String, Object> row = Dto.giorno(giorno);
            row.put("lezioniDisponibili", lezioni);
            giorni.add(row);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("maestro", Dto.maestro(maestro, StrumentoDAO.readByMaestro(idMaestro)));
        response.put("giorni", giorni);
        ctx.sendJson(200, response);
    }

    private static int parseId(String raw)
    {
        try { return Integer.parseInt(raw); }
        catch (NumberFormatException e) { throw ApiException.badRequest("Id Maestro non valido"); }
    }
}
