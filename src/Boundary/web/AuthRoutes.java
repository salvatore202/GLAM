package Boundary.web;

import Control.GestioneLezioni;
import DAO.MaestroDAO;
import DAO.StrumentoDAO;
import DAO.StudenteRegistratoDAO;
import Entity.Maestro;
import Entity.StudenteRegistrato;
import java.util.Map;

/**
 * Login per Maestro e Studente Registrato. Riusa esattamente
 * GestioneLezioni.verificaMaestro / verificaStudente (classe Control
 * originale, non modificata) per l'esito dell'autenticazione.
 *
 * Il backend originale non ha alcun concetto di sessione/token: ogni singola
 * operazione della CLI richiede di reinserire email/password (o
 * username/password). Il layer web mantiene lo stesso modello stateless: il
 * frontend conserva le credenziali lato client (in memoria, non in
 * localStorage) e le reinvia ad ogni chiamata, cosi' come farebbe l'utente
 * da terminale.
 */
public final class AuthRoutes
{
    private AuthRoutes() {}

    public static void register(Router router)
    {
        router.add("POST", "/api/auth/maestro", AuthRoutes::loginMaestro);
        router.add("POST", "/api/auth/studente", AuthRoutes::loginStudente);
    }

    private static void loginMaestro(Router.RequestContext ctx) throws Exception
    {
        Map<String, Object> body = ctx.jsonBody();
        String email = Validation.email(ctx.requireString(body, "email"));
        String password = Validation.password(ctx.requireString(body, "password"));

        if (!GestioneLezioni.verificaMaestro(email, password))
            throw ApiException.unauthorized("Maestro non registrato o credenziali errate");

        Maestro maestro = MaestroDAO.readMaestro(email, password);
        Map<String, Object> response = Dto.maestro(maestro, StrumentoDAO.readByMaestro(maestro.getId()));
        response.put("successo", true);
        ctx.sendJson(200, response);
    }

    private static void loginStudente(Router.RequestContext ctx) throws Exception
    {
        Map<String, Object> body = ctx.jsonBody();
        String username = Validation.username(ctx.requireString(body, "username"));
        String password = Validation.password(ctx.requireString(body, "password"));

        if (!GestioneLezioni.verificaStudente(username, password))
            throw ApiException.unauthorized("Studente non registrato o credenziali errate");

        StudenteRegistrato studente = StudenteRegistratoDAO.readStudenteRegistrato(username, password);
        Map<String, Object> response = Dto.studente(studente);
        response.put("successo", true);
        ctx.sendJson(200, response);
    }
}
