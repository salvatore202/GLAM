# GLAM

[![Java](https://img.shields.io/badge/Language-Java%2017%20%2F%2021-orange.svg?style=flat&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![MySQL](https://img.shields.io/badge/Database-MySQL-blue.svg?style=flat&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![JUnit 5](https://img.shields.io/badge/Testing-JUnit%205-red.svg?style=flat&logo=junit5&logoColor=white)](https://junit.org/junit5/)
[![Visual Paradigm](https://img.shields.io/badge/UML-Visual%20Paradigm-blueviolet.svg?style=flat)](https://www.visual-paradigm.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](https://opensource.org/licenses/MIT)
[![CI](https://github.com/salvatore202/GLAM/actions/workflows/ci.yml/badge.svg)](https://github.com/salvatore202/GLAM/actions/workflows/ci.yml)

Questo repository contiene il progetto d'esame sviluppato per il corso di **Ingegneria del Software**, tenuto dal Prof. Roberto Pietrantuono presso l'Università degli Studi di Napoli Federico II.

Il progetto consiste nell'ingegnerizzazione completa di un sistema software orientato agli oggetti, coprendo l'intero ciclo di vita dello sviluppo secondo una metodologia strutturata:

* **Analisi e Modellazione UML:** Traduzione dei requisiti nei diagrammi dei casi d'uso, delle classi e di sequenza sviluppati tramite Visual Paradigm.
* **Architettura BCE (Boundary-Control-Entity):** Disaccoppiamento netto del sistema software, separando rigorosamente le interfacce utente (**Boundary**) dalla logica di business (**Control**) e dalle classi di dominio (**Entity**).
* **Persistenza dei Dati e Pattern DAO:** Progettazione del database relazionale in MySQL, isolando i dettagli di persistenza ed eseguendo le operazioni CRUD tramite il pattern **Data Access Object (DAO)**.
* **Verifica e Testing Automatizzato:** Validazione della robustezza della business logic e del software tramite la scrittura di suite di test d'unità con il framework **JUnit**, eseguibili anche in modalità headless tramite console standalone per garantire l'affidabilità del sistema a fronte di edge cases ed eccezioni a runtime.

> Il progetto originale espone i casi d'uso solo tramite CLI. `WEB_LAYER.md` documenta un layer REST aggiuntivo (`src/Boundary/web`, zero modifiche ai file esistenti) e un frontend React in `frontend/`, con pipeline CI/CD in `.github/workflows/`.



## Struttura della Repository

La repository è organizzata in 4 directory principali. Indicazioni su struttura e contenuto delle directory di seguito:

```text
├── .github/
│   └── workflows/                  # CI (test backend + smoke test API + build frontend) e deploy su GitHub Pages
│
├── Database/                       # Ddatabase usato per testare  
│   └── MySql/
│
├── Documentation/                  # Documentazione del progetto
│   └── Documentazione_GLAM
│
├── frontend/                       # Interfaccia web (React + TypeScript + Vite), vedi WEB_LAYER.md
│
├── scripts/                        # Script di comodo per sviluppo locale e CI
│
├── src/                            # Codice sorgente dell'applicazione Java
│   ├── Boundary/                   # Interfacce utente (GUI o CLI) per l'interazione con gli attori del sistema
│   │   └── web/                    # Nuovo layer REST (Boundary.web.ApiServer), affianca la CLI esistente
│   ├── Control/                    # Controller della Business Logic (coordinamento tra viste ed entità)
│   ├── DAO/                        # Data Access Object: classi per il mapping e l'interazione CRUD con il database MySQL
│   ├── Entity/                     # Classi di dominio del sistema (modelli dei dati persistenti)
│   ├── Exception/                  # Eccezioni customizzate per la gestione robusta degli errori a runtime
│   ├── lib/                        # Librerie e driver esterni (es. Connector/J per la connessione JDBC a MySQL)
│   ├── Test/                       # Suite di Unit Testing sviluppata con JUnit 5 per la verifica dei requisiti
│   └── junit-platform-console-standalone-1.9.3
│
├── README.md                       # Questo file
└── WEB_LAYER.md                    # Documentazione del layer REST + frontend
```

## Tutorial per testare la repo

Per avviare e testare l'intero sistema (backend Java, database MySQL e frontend React) non è più necessario installare XAMPP o avviare manualmente backend e frontend: tutto il sistema gira in un container Docker gestito automaticamente dallo script `start.sh`.

### 1. Prerequisiti

Assicurati di avere **Docker** installato e funzionante sulla tua macchina.

### 2. Avviare il sistema

Apri il terminale nella root del progetto ed esegui:

```bash
chmod +x start.sh
./start.sh
```

> **Nota:** Lo script si occupa automaticamente di:
> - effettuare il checkout del branch `web-frontend`
> - buildare l'immagine Docker (backend Java + MySQL + frontend React)
> - avviare il container
> - popolare il database con i dati di test predefiniti
> - aprire il browser sul frontend una volta pronto (`http://localhost:5173`)

Nello specifico, il database viene popolato con i seguenti record:

#### Maestri
| idMaestro | nome | cognome | numeroditelefono | email | password |
| :--- | :--- | :--- | :--- | :--- | :--- |
| 1 | Paolo | Ventresca | 1234567890 | paolo.ventresca@example.com | password123 |
| 2 | Angelo | Trapanese | 0987654321 | angelo.trapanese@example.com | password456 |
| 3 | Loredana | Circuito | 3334445550 | loredana.circuito@example.com | password222 |

#### Strumenti
| idStrumento | nome | idMaestro |
| :--- | :--- | :--- |
| 1 | VIOLINO | 3 |
| 2 | CHITARRA | 2 |
| 3 | PIANOFORTE | 1 |
| 4 | BATTERIA | 3 |
| 5 | SASSOFONO | 2 |

#### Studenti
| idStudente | nome | cognome | datadinascita | numeroditelefono | username | email | password |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| 1 | Ludovico | Einaudi | 1955-12-23 | 3390078564 | vleinaudi | ludovico.einaudi@example.com | password333 |
| 2 | Alex | delPiero | 1974-11-09 | 3393378565 | adelpiero | alex.delpiero@example.com | password444 |
| 3 | User | FromGitHub | 1974-12-07 | 3393778565 | user | user.fromgithub@example.com | password555 |

### 3. Comandi utili

```bash
# Vedere i log in tempo reale
docker logs -f glam_container
```


```bash
# Fermare il container
docker stop glam_container
```
---

### 4. Aprire il browser

Una volta avviati tutti i servizi, apri il tuo browser preferito e collegati all'indirizzo:

👉 **[http://localhost:5173/](http://localhost:5173/)**

Da qui potrai interagire con l'interfaccia web del sistema GLAM.

## 📄 Documentazione del Progetto

La documentazione completa, che include l'analisi dei requisiti dettagliata, i diagrammi UML e altro ancora, è consultabile direttamente qui:

👉 **[Scarica/Visualizza la Documentazione Completa (PDF)](Documentation/Documentazione_GLAM.pdf)**