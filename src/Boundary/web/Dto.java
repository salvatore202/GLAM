package Boundary.web;

import Entity.GiornoDisponibilità;
import Entity.Lezione;
import Entity.Maestro;
import Entity.Prenotazione;
import Entity.Strumento;
import Entity.StudenteRegistrato;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Conversione delle Entity originali in Map JSON-friendly. Vive interamente
 * nel nuovo layer web: le classi Entity originali non vengono toccate. Le
 * password non vengono MAI incluse in nessuna risposta.
 */
public final class Dto
{
    private Dto() {}

    public static Map<String, Object> maestro(Maestro m)
    {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", m.getId());
        map.put("nome", m.getNome());
        map.put("cognome", m.getCognome());
        map.put("email", m.getEmail());
        map.put("numeroDiTelefono", m.getNumeroDiTelefono());
        return map;
    }

    public static Map<String, Object> maestro(Maestro m, List<Strumento> strumenti)
    {
        Map<String, Object> map = maestro(m);
        List<String> nomi = new ArrayList<>();
        for (Strumento s : strumenti) nomi.add(s.getNome());
        map.put("strumenti", nomi);
        return map;
    }

    public static Map<String, Object> studente(StudenteRegistrato s)
    {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", s.getIdStudente());
        map.put("nome", s.getNome());
        map.put("cognome", s.getCognome());
        map.put("username", s.getUsername());
        map.put("email", s.getEmail());
        map.put("numeroDiTelefono", s.getNumeroDiTelefono());
        return map;
    }

    public static Map<String, Object> giorno(GiornoDisponibilità g)
    {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("idData", g.getIdData());
        map.put("data", g.getData().toString());
        map.put("idMaestro", g.getIdMaestro());
        return map;
    }

    public static Map<String, Object> lezione(Lezione l)
    {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("idLezione", l.getIdLezione());
        map.put("ora", l.getOra().toString());
        map.put("livello", l.getLivello());
        map.put("disponibile", l.isDisponibile());
        map.put("idMaestro", l.getIdMaestro());
        map.put("idData", l.getIdData());
        return map;
    }

    public static Map<String, Object> prenotazione(Prenotazione p)
    {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("idPrenotazione", p.getIdPrenotazione());
        map.put("costo", p.getCosto());
        map.put("idLezione", p.getIdLezione());
        map.put("idStudente", p.getIdStudente());
        return map;
    }
}
