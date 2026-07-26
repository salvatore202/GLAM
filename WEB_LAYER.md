# GLAM — Layer web (REST API + frontend)

Questo documento spiega cosa e' stato aggiunto al progetto originale per dargli
un frontend moderno, cosa NON e' stato toccato, i bug trovati nel codice
esistente durante il lavoro, e come far girare tutto (in locale e su GitHub
Actions).

## Cosa e' stato aggiunto (tutto additivo — zero file originali modificati)

```
src/Entity/Strumento.java              nuova Entity per la tabella STRUMENTO
                                        (esisteva gia' nello schema SQL ma non
                                        aveva mai avuto una classe dedicata)
src/DAO/StrumentoDAO.java              DAO di sola lettura per Strumento
src/DAO/CatalogoDAO.java               query di aggregazione per il frontend
                                        (tutti i Maestri, prenotazioni di uno
                                        Studente, lezioni svolte da un Maestro)
src/Boundary/web/                      nuovo layer REST (9 file), vedi sotto
Database/MySql/test_fixtures.sql       fixture per la suite di test originale
                                        (vedi "Bug trovati", punto 4)
frontend/                              interfaccia web React
scripts/                               script di comodo per sviluppo/CI
.github/workflows/                     CI + deploy GitHub Pages
```

Nessun file dentro `src/Boundary`, `src/Control`, `src/DAO`, `src/Entity`,
`src/Exception`, `src/Test` che esisteva gia' e' stato modificato. Il nuovo
codice o aggiunge file nuovi, o affianca l'esistente in una sottocartella
(`Boundary/web`).

### Perche' `Boundary/web` e non un altro package

Il backend segue gia' un'architettura BCE (Boundary-Control-Entity): la CLI
(`BoundaryMaestro`, `BoundaryStudenteRegistrato`) e' un Boundary che parla con
`Control.GestioneLezioni`. Il server REST e' concettualmente un **secondo
Boundary**, questa volta HTTP invece che CLI, che riusa esattamente la stessa
Control — coerente con l'architettura che il progetto gia' dichiara.

### Perche' nessuna libreria esterna nuova

Il progetto compila con `javac` puro, senza Maven/Gradle, con i jar in
`src/lib`. Aggiungere un framework web (Spring, Javalin...) avrebbe richiesto
Maven Central, non raggiungibile in questo ambiente ne', potenzialmente, in
modo affidabile da ogni runner CI. Il server REST usa solo
`com.sun.net.httpserver` (incluso nel JDK) e un encoder/decoder JSON scritto a
mano (`Boundary/web/Json.java`) — zero dipendenze nuove, stessa filosofia
"solo javac" del resto del progetto.

## Come i metodi "void" della Control diventano risposte HTTP

`GestioneLezioni.prenotaLezione`, `disdiciLezione`, `inserisciGiorno`,
`modificaGiorno` sono `void` e comunicano l'esito solo stampando su console.
Per una risposta HTTP questo non basta, quindi ogni rotta:

1. fa un controllo preventivo di sola lettura (es. la lezione esiste? e'
   libera?) per poter dare un errore preciso *prima* di chiamare il metodo
   originale;
2. chiama il metodo originale, catturando temporaneamente `System.out`/`err`
   (`ConsoleCapture.java`) cosi' il testo esatto che il backend avrebbe
   stampato in console arriva anche nella risposta JSON (campo `"log"` —
   visibile nell'interfaccia in un pannello "Console del backend originale");
3. rilegge lo stato dal database subito dopo, per confermare cosa e'
   realmente successo.

Il server usa un executor **single-thread** (`ApiServer.java`), di proposito:
`DAO.DBManager` tiene una singola `Connection` statica condivisa e la chiude
in ogni `finally` di ogni chiamata DAO — sotto richieste concorrenti su thread
diversi questo produrrebbe `Connection is closed` in modo intermittente. La
CLI originale e' comunque single-threaded, quindi processare le richieste in
sequenza rispetta le stesse assunzioni di concorrenza del codice esistente
senza doverlo modificare. Per un uso con piu' utenti reali in contemporanea,
`DBManager` andrebbe riscritto con un connection pool.

## Riferimento API REST

Base URL: `http://localhost:8080/api` (porta configurabile con `PORT`).

| Metodo | Path                              | Autenticazione        | Descrizione |
|--------|-----------------------------------|------------------------|-------------|
| GET    | `/health`                         | —                      | Stato server + connessione DB |
| GET    | `/strumenti`                      | —                      | I 6 strumenti ammessi, con i Maestri che li insegnano |
| GET    | `/maestri`                        | —                      | Tutti i Maestri |
| GET    | `/maestri/ricerca?strumento=X`    | —                      | Maestri che insegnano `X` |
| GET    | `/maestri/{id}/disponibilita`     | —                      | Giorni + lezioni libere di un Maestro |
| POST   | `/auth/maestro`                   | body `{email,password}`| Login Maestro |
| POST   | `/auth/studente`                  | body `{username,password}` | Login Studente |
| POST   | `/maestro/giorni`                 | body                   | Apre un giorno di disponibilita' (crea 4 lezioni) |
| PUT    | `/maestro/giorni`                 | body                   | Sposta un giorno gia' esistente |
| GET    | `/maestro/lezioni-svolte`         | header `X-Email`/`X-Password` | Lezioni prenotate presso quel Maestro |
| POST   | `/studente/prenotazioni`          | body                   | Prenota una o piu' lezioni |
| DELETE | `/studente/prenotazioni`          | body                   | Disdice una prenotazione |
| GET    | `/studente/prenotazioni`          | header `X-Username`/`X-Password` | Le prenotazioni dello Studente |

Non esiste un concetto di sessione/token: come nella CLI originale, ogni
operazione richiede le credenziali ad ogni chiamata. Le GET che richiedono
credenziali le accettano da header invece che da query string, per non
lasciarle in URL/log.

## Bug trovati nel codice originale

Segnalati con la stessa franchezza tecnica che di solito apprezzi — nessuno
di questi e' stato "sistemato" nei file originali, solo aggirato in modo
sicuro nel nuovo layer, cosi' resta la tua scelta se e come intervenire.

1. **`GestioneLezioni.disdiciLezione` puo' andare in `NullPointerException`
   non gestita.** Se uno Studente disdice una lezione che non ha mai
   prenotato, e la data e' a 3 o piu' giorni di distanza,
   `PrenotazioneDAO.readPrenotazione(...)` ritorna correttamente `null`, ma il
   codice chiama comunque `prenotazioneEffettata.getIdPrenotazione()` senza
   controllo — `NullPointerException` non catturata (il blocco `catch`
   intorno intercetta solo `DAOException | DBConnectionException`), che nella
   CLI manda in crash l'intero programma. La rotta `DELETE
   /studente/prenotazioni` verifica che la prenotazione esista *prima* di
   chiamare il metodo originale, cosi' la condizione non si verifica mai
   passando dal web — ma il bug resta nel metodo originale se richiamato
   direttamente dalla CLI.

2. **`BoundaryStudenteRegistrato.CercaMaestro` puo' andare in
   `IndexOutOfBoundsException`.** Fa `maestri.get(0)` sul risultato di
   `MaestroDAO.CercaMaestro(strumento)` senza controllare se la lista e'
   vuota. Con i dati seed, cercare "BASSO" (strumento valido per la
   validazione ma senza nessun Maestro assegnato) manda in crash la CLI. La
   rotta `GET /maestri/ricerca` restituisce semplicemente una lista vuota.

3. **Un test esistente non verifica nulla.**
   `BoundaryStudenteRegistratoTest.prenotaLezione_inputValidiTest` popola
   `cognomiMaestro` ma non `nomiMaestro`: il `for` e' delimitato da
   `nomiMaestro.size()`, quindi il corpo del ciclo (dove stanno tutti gli
   assert) non viene mai eseguito. Il test "passa" senza controllare niente.

4. **10 test su 13 falliscono su un database pulito.** Dipendono da uno
   Studente (`vdeluca`/`password111`) e da alcune righe di
   `GiornoDisponibilita`/`Lezione`/`Prenotazione` che non fanno parte del
   file SQL committato — quasi certamente dati inseriti a mano durante i tuoi
   test interattivi da CLI e mai salvati nel seed. Ho ricostruito quei dati
   in `Database/MySql/test_fixtures.sql` (file nuovo, separato — non ho
   toccato `databaseglam10.sql` ne' i file di test) cosi' la suite ORIGINALE,
   non modificata, passa 13/13 su un database pulito, sia in locale che in
   CI.

5. **Su Linux, molte query non trovano le tabelle.** Le query mischiano
   maiuscole/minuscole nei nomi tabella (es. `SELECT * FROM LEZIONE` in un
   metodo, `JOIN Strumento s` in un altro, nella stessa classe/query). Su
   Windows/macOS MySQL confronta i nomi tabella senza distinguere
   maiuscole/minuscole di default (`lower_case_table_names=1`); su Linux il
   default e' `0` (case-sensitive), quindi quelle query falliscono a trovare
   le tabelle finche' non imposti esplicitamente
   `lower_case_table_names=1` (si puo' fare solo *prima* di creare le
   tabelle — vedi `scripts/dev-up.sh` e il workflow CI). Se hai sviluppato su
   Windows o macOS non l'avresti mai notato.

6. Osservazioni minori, a scopo informativo:
   - Il **livello** di una lezione (`BASE`/`INTERMEDIO`, quindi il prezzo) e'
     deciso dallo Studente al momento della prenotazione
     (`eL.setLivello(livelli.get(i))` sovrascrive il valore corrente prima di
     salvare), non e' un attributo fisso deciso dal Maestro quando apre lo
     slot. Puo' essere intenzionale, ma vale la pena verificarlo.
   - La tabella `LEZIONE` non registra per quale strumento e' stata
     prenotata una lezione (lo strumento e' usato solo come verifica "questo
     Maestro lo insegna davvero?" al momento della prenotazione/disdetta, poi
     non viene persistito). Per un Maestro che insegna un solo strumento non
     cambia nulla; per uno che ne insegna piu' di uno (es. Loredana:
     violino+batteria) non c'e' modo di risalire, dopo, a quale dei due fosse
     una lezione gia' svolta. Il frontend non inventa questo dato.
   - `GiornoDisponibilitàDAO.checkData` interroga una tabella scritta
     `GIORNIDISPONIBILITA` (plurale, refuso — la tabella si chiama
     `GiornoDisponibilita`, singolare) e legge una seconda colonna da un
     `COUNT(*)` che ne restituisce una sola: se richiamato fallirebbe.
     Sembra pero' inutilizzato altrove nel codice.
   - Le password sono in chiaro nel database e non c'e' hashing da nessuna
     parte (coerente con un esame che verte su architettura/test, non su
     sicurezza) — segnalato solo perche' ora passano anche per HTTP; il
     nuovo layer non le logga mai ne' le restituisce nelle risposte.

## Come far girare tutto in locale

```bash
# terminale 1 — backend (richiede MySQL in ascolto su localhost:3306, utente
# root senza password — le stesse credenziali hardcoded in DBManager.java)
bash scripts/dev-up.sh

# terminale 2 — frontend
cd frontend
npm install
npm run dev
```

Poi apri `http://localhost:5173`. Credenziali di test nei dati seed: Maestro
`paolo.ventresca@example.com` / `password123`, Studente `vleinaudi` /
`password333`.

Su Linux, se e' la prima volta che crei il database su questa macchina,
ricorda di impostare `lower_case_table_names=1` nella configurazione di
MySQL *prima* di caricare lo schema (vedi punto 5 sopra) — se hai gia' un
`databaseglam10` funzionante da CLI su Windows/macOS non serve.

## GitHub Actions

- **`.github/workflows/ci.yml`** — ad ogni push/PR: installa e configura
  MySQL da zero, carica schema + fixture, compila tutto, esegue la suite
  JUnit originale (13/13 atteso), avvia il server REST e ci lancia contro
  `scripts/smoke-test-api.sh` (21 controlli, inclusi i due bug del punto 1 e
  2 sopra), poi separatamente lint + build del frontend.
- **`.github/workflows/deploy-frontend.yml`** — pubblica `frontend/` su
  GitHub Pages ad ogni push su `main` che tocca quella cartella (richiede
  Settings → Pages → Source → "GitHub Actions", una tantum). Pages serve solo
  file statici: per funzionare davvero (non solo per guardare l'interfaccia)
  serve un backend raggiungibile pubblicamente, configurabile con la
  variabile di repository `VITE_API_BASE_URL`.

Entrambi i workflow sono stati validati eseguendo localmente, passo per
passo, esattamente i comandi che contengono.
