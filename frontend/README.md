# GLAM — Frontend

Interfaccia web per GLAM, scritta in React 19 + TypeScript + Vite 8 + Tailwind CSS v4.
Parla con il backend tramite il nuovo layer REST (`Boundary.web.ApiServer`, vedi
`../WEB_LAYER.md` nella root della repo per i dettagli del backend).

## Requisiti

- Node.js 22+ e npm
- Il backend GLAM (`Boundary.web.ApiServer`) in esecuzione — di default su `http://localhost:8080`

## Avvio in locale

```bash
# 1. dal backend (root della repo): avvia MySQL + API
bash scripts/dev-up.sh

# 2. in un altro terminale, dal frontend:
cd frontend
npm install
npm run dev
```

Apri `http://localhost:5173`. Le credenziali di test sono quelle nei dati seed
(`Database/MySql/databaseglam10.sql`): Maestro `paolo.ventresca@example.com` /
`password123`, Studente `vleinaudi` / `password333`.

## Comandi

| Comando           | Cosa fa                                              |
| ------------------ | ----------------------------------------------------- |
| `npm run dev`      | Server di sviluppo con hot reload (porta 5173)        |
| `npm run build`    | Typecheck + build di produzione in `dist/`             |
| `npm run lint`     | ESLint su tutto il progetto                            |
| `npm run preview`  | Serve la build di produzione in locale per verifica     |

## Configurazione

L'URL del backend si configura con la variabile d'ambiente `VITE_API_BASE_URL`
(vedi `.env.example`). Se non impostata, il default e' `http://localhost:8080/api`.

```bash
cp .env.example .env
# modifica .env se il backend gira altrove
```

## Note di design

- **Palette**: fondo scuro "palco" (`stage`) per l'hero, superfici chiare
  "parchment" per le card, accenti ottone (`brass`) e verde velluto
  (`velvet`).
- **Tipografia**: Fraunces per i titoli, IBM Plex Sans per il testo/interfaccia,
  IBM Plex Mono per orari/prezzi/id — caricati come font self-hosted via
  `@fontsource` (nessuna dipendenza da CDN esterni a runtime), solo subset
  `latin` (il sito e' in italiano).
- **Elemento firma**: gli slot prenotabili (`TicketSlot.tsx`) sono disegnati
  come lo stacco di un biglietto da concerto, incisura compresa. I separatori
  di sezione (`StaffDivider.tsx`) sono un rigo musicale a 5 linee.
- Le credenziali di Maestro/Studente restano solo in `sessionStorage` (mai
  `localStorage`): coerente col fatto che il backend originale non ha alcun
  concetto di sessione/token e richiede email+password (o username+password)
  ad ogni singola operazione, esattamente come la CLI.

## Build e TypeScript

`typescript` e' fissato alla `6.0.3` (non alla 7.0, uscita il 8 luglio 2026)
perche' al momento della stesura `typescript-eslint` dichiara esplicitamente
`typescript: ">=4.8.4 <6.1.0"` nei suoi `peerDependencies` — la 7.0 e' fuori
range. Quando l'ecosistema di lint si aggiornera', si potra' rivalutare.

## Deploy

`../.github/workflows/deploy-frontend.yml` pubblica automaticamente questa
cartella su GitHub Pages ad ogni push su `main` che tocca `frontend/`
(richiede di impostare una volta Settings → Pages → Source → "GitHub
Actions"). Ricorda: Pages serve solo file statici, quindi la pagina
pubblicata potra' effettivamente prenotare lezioni solo se
`VITE_API_BASE_URL` punta a un backend raggiungibile pubblicamente (puoi
impostarlo come variabile di repository `VITE_API_BASE_URL` in Settings →
Secrets and variables → Actions → Variables). Senza un backend pubblico resta
comunque utile come anteprima dell'interfaccia.
