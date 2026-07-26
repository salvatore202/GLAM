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


## 📄 Documentazione del Progetto

La documentazione completa, che include l'analisi dei requisiti dettagliata, i diagrammi UML e altro ancora, è consultabile direttamente qui:

👉 **[Scarica/Visualizza la Documentazione Completa (PDF)](Documentation/Documentazione_GLAM.pdf)**