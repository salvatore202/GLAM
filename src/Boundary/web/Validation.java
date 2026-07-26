package Boundary.web;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Set;

/**
 * Replica delle regole di validazione gia' presenti nelle classi Boundary
 * originali (BoundaryMaestro, BoundaryStudenteRegistrato), spostate qui
 * perche' il frontend web invia dati via JSON invece che tramite Scanner.
 * Le regole di business (lunghezza password, strumenti ammessi, orari
 * ammessi, livelli ammessi, vincolo "data futura" per la modifica di un
 * giorno) sono le stesse: cambia solo il formato di trasporto (JSON invece
 * di testo da terminale, date ISO "yyyy-MM-dd" invece di "dd/MM/yyyy").
 */
public final class Validation
{
    private Validation() {}

    private static final Set<String> STRUMENTI_VALIDI =
        Set.of("PIANOFORTE", "CHITARRA", "VIOLINO", "BATTERIA", "BASSO", "SASSOFONO");

    private static final Set<String> ORARI_VALIDI = Set.of("17:00", "18:00", "19:00", "20:00");

    private static final Set<String> LIVELLI_VALIDI = Set.of("BASE", "INTERMEDIO");

    public static Set<String> strumentiValidi() { return STRUMENTI_VALIDI; }

    public static String email(String value)
    {
        if (value == null || value.isBlank() || value.length() > 50)
            throw ApiException.badRequest("Email non valida (richiesta, max 50 caratteri)");
        return value;
    }

    public static String password(String value)
    {
        if (value == null || value.length() < 8 || value.length() > 50)
            throw ApiException.badRequest("Password non valida (richiesti 8-50 caratteri)");
        return value;
    }

    public static String username(String value)
    {
        if (value == null || value.isBlank() || value.length() > 50)
            throw ApiException.badRequest("Username non valido (richiesto, max 50 caratteri)");
        return value;
    }

    public static String nomeOCognome(String value, String campo)
    {
        if (value == null || value.isBlank() || value.length() > 50)
            throw ApiException.badRequest(campo + " non valido (richiesto, max 50 caratteri)");
        return value;
    }

    public static String strumento(String value)
    {
        if (value == null) throw ApiException.badRequest("Strumento obbligatorio");
        String v = value.toUpperCase();
        if (!STRUMENTI_VALIDI.contains(v))
            throw ApiException.badRequest("Strumento non valido. Ammessi: " + String.join(", ", STRUMENTI_VALIDI));
        return v;
    }

    public static String livello(String value)
    {
        if (value == null) throw ApiException.badRequest("Livello obbligatorio");
        String v = value.toUpperCase();
        if (!LIVELLI_VALIDI.contains(v))
            throw ApiException.badRequest("Livello non valido. Ammessi: BASE (15.00 EUR), INTERMEDIO (30.00 EUR)");
        return v;
    }

    public static LocalTime ora(String value)
    {
        if (value == null || !ORARI_VALIDI.contains(value))
            throw ApiException.badRequest("Orario non valido. Le lezioni iniziano solo alle 17:00, 18:00, 19:00 o 20:00");
        return LocalTime.parse(value);
    }

    public static LocalDate data(String value, String campo)
    {
        if (value == null || value.isBlank())
            throw ApiException.badRequest(campo + " obbligatoria (formato yyyy-MM-dd)");
        try
        {
            return LocalDate.parse(value);
        }
        catch (DateTimeParseException e)
        {
            throw ApiException.badRequest(campo + " non valida: usare il formato yyyy-MM-dd");
        }
    }

    public static LocalDate dataFutura(String value, String campo)
    {
        LocalDate d = data(value, campo);
        if (!d.isAfter(LocalDate.now()))
            throw ApiException.badRequest(campo + " deve essere una data futura");
        return d;
    }
}
