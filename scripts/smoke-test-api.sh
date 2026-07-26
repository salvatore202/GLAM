#!/bin/bash
# Smoke test per Boundary.web.ApiServer (nuovo layer REST).
#
# Presuppone che:
#   1. Il database sia gia' stato caricato con Database/MySql/databaseglam10.sql
#      seguito da Database/MySql/test_fixtures.sql
#   2. Boundary.web.ApiServer sia gia' in esecuzione e raggiungibile
#
# Uso:
#   API_BASE=http://localhost:8080/api bash scripts/smoke-test-api.sh
# (API_BASE e' opzionale, di default punta a localhost:8080/api)

set -uo pipefail
BASE="${API_BASE:-http://localhost:8080/api}"
PASS=0
FAIL=0

check() {
  local desc="$1"; local expected="$2"; local actual="$3"; local body="$4"
  if [ "$actual" == "$expected" ]; then
    PASS=$((PASS+1))
    echo "OK   [$actual] $desc"
  else
    FAIL=$((FAIL+1))
    echo "FAIL [atteso $expected, ricevuto $actual] $desc"
    echo "     body: $body"
  fi
}

req() {
  local method="$1"; local path="$2"; local data="${3:-}"
  shift 3 2>/dev/null || shift $#
  if [ -n "$data" ]; then
    curl -s -o /tmp/glam_resp.json -w "%{http_code}" -X "$method" "$BASE$path" \
      -H "Content-Type: application/json" -d "$data" "$@"
  else
    curl -s -o /tmp/glam_resp.json -w "%{http_code}" -X "$method" "$BASE$path" "$@"
  fi
}

echo "=================== Base URL: $BASE ==================="

echo "--- 1. Health & Catalogo (pubblici) ---"
code=$(req GET /health); check "GET /health" 200 "$code" "$(cat /tmp/glam_resp.json)"
code=$(req GET /strumenti); check "GET /strumenti" 200 "$code" "$(cat /tmp/glam_resp.json)"
grep -q "PIANOFORTE" /tmp/glam_resp.json && echo "     contiene PIANOFORTE: OK"
grep -q "BASSO" /tmp/glam_resp.json && echo "     contiene BASSO (0 maestri, niente crash): OK"
code=$(req GET /maestri); check "GET /maestri" 200 "$code" "$(cat /tmp/glam_resp.json)"
code=$(req GET "/maestri/ricerca?strumento=PIANOFORTE"); check "GET /maestri/ricerca?strumento=PIANOFORTE" 200 "$code" "$(cat /tmp/glam_resp.json)"
code=$(req GET "/maestri/ricerca?strumento=BASSO"); check "GET /maestri/ricerca?strumento=BASSO (nessun docente, no crash)" 200 "$code" "$(cat /tmp/glam_resp.json)"
code=$(req GET "/maestri/999/disponibilita"); check "GET /maestri/999/disponibilita (id inesistente)" 404 "$code" "$(cat /tmp/glam_resp.json)"
code=$(req GET "/maestri/1/disponibilita"); check "GET /maestri/1/disponibilita" 200 "$code" "$(cat /tmp/glam_resp.json)"

echo "--- 2. Autenticazione ---"
code=$(req POST /auth/maestro '{"email":"paolo.ventresca@example.com","password":"password123"}')
check "POST /auth/maestro (credenziali corrette)" 200 "$code" "$(cat /tmp/glam_resp.json)"
code=$(req POST /auth/maestro '{"email":"paolo.ventresca@example.com","password":"sbagliata"}')
check "POST /auth/maestro (password sbagliata)" 401 "$code" "$(cat /tmp/glam_resp.json)"
code=$(req POST /auth/studente '{"username":"vleinaudi","password":"password333"}')
check "POST /auth/studente (credenziali corrette)" 200 "$code" "$(cat /tmp/glam_resp.json)"

echo "--- 3. Maestro: giorni di disponibilita ---"
code=$(req POST /maestro/giorni '{"email":"paolo.ventresca@example.com","password":"password123","data":"2099-08-15"}')
check "POST /maestro/giorni 2099-08-15 (nuovo)" 201 "$code" "$(cat /tmp/glam_resp.json)"
code=$(req POST /maestro/giorni '{"email":"paolo.ventresca@example.com","password":"password123","data":"2099-08-15"}')
check "POST /maestro/giorni 2099-08-15 (duplicato)" 409 "$code" "$(cat /tmp/glam_resp.json)"
code=$(req PUT /maestro/giorni '{"email":"paolo.ventresca@example.com","password":"password123","data":"2099-08-15","nuovaData":"2099-08-16"}')
check "PUT /maestro/giorni sposta 2099-08-15 -> 2099-08-16" 200 "$code" "$(cat /tmp/glam_resp.json)"

echo "--- 4. Studente: prenotazioni ---"
code=$(req POST /studente/prenotazioni '{"username":"vleinaudi","password":"password333","richieste":[
  {"strumento":"PIANOFORTE","nomeMaestro":"Paolo","cognomeMaestro":"Ventresca","data":"2099-08-16","ora":"17:00","livello":"BASE"}
]}')
check "POST /studente/prenotazioni (valida)" 201 "$code" "$(cat /tmp/glam_resp.json)"
grep -q '"esito":"prenotata"' /tmp/glam_resp.json && echo "     esito=prenotata: OK"

code=$(req POST /studente/prenotazioni '{"username":"adelpiero","password":"password444","richieste":[
  {"strumento":"PIANOFORTE","nomeMaestro":"Paolo","cognomeMaestro":"Ventresca","data":"2099-08-16","ora":"17:00","livello":"BASE"}
]}')
check "POST /studente/prenotazioni stesso slot, altro studente (non disponibile)" 207 "$code" "$(cat /tmp/glam_resp.json)"

code=$(req GET /studente/prenotazioni "" -H "X-Username: vleinaudi" -H "X-Password: password333")
check "GET /studente/prenotazioni (vleinaudi)" 200 "$code" "$(cat /tmp/glam_resp.json)"

echo "--- 5. Maestro: lezioni prenotate ---"
code=$(req GET /maestro/lezioni-svolte "" -H "X-Email: paolo.ventresca@example.com" -H "X-Password: password123")
check "GET /maestro/lezioni-svolte (Ventresca)" 200 "$code" "$(cat /tmp/glam_resp.json)"

echo "--- 6. Disdici lezione (incluso il guard anti-NullPointerException) ---"
code=$(req DELETE /studente/prenotazioni '{"username":"vleinaudi","password":"password333","strumento":"PIANOFORTE","nomeMaestro":"Paolo","cognomeMaestro":"Ventresca","data":"2099-08-16","ora":"17:00"}')
check "DELETE prenotazione (>=3gg, deve rimborsare)" 200 "$code" "$(cat /tmp/glam_resp.json)"
grep -q '"rimborsata":true' /tmp/glam_resp.json && echo "     rimborsata=true come atteso: OK"

# Critico: disdire una lezione MAI prenotata da questo studente, a >=3 giorni di
# distanza. Nel Control originale (non modificato) questo scatena una
# NullPointerException non gestita. La rotta deve rispondere 409 pulito PRIMA
# di richiamare il metodo originale in quello stato.
code=$(req DELETE /studente/prenotazioni '{"username":"adelpiero","password":"password444","strumento":"PIANOFORTE","nomeMaestro":"Paolo","cognomeMaestro":"Ventresca","data":"2099-08-16","ora":"18:00"}')
check "DELETE lezione mai prenotata da questo studente, >=3gg (guard anti-NPE)" 409 "$code" "$(cat /tmp/glam_resp.json)"

echo "--- 7. Validazione input ---"
code=$(req POST /auth/maestro '{"email":"paolo.ventresca@example.com","password":"corta"}')
check "POST /auth/maestro password troppo corta" 400 "$code" "$(cat /tmp/glam_resp.json)"
code=$(req POST /studente/prenotazioni '{"username":"vleinaudi","password":"password333","richieste":[
  {"strumento":"UKULELE","nomeMaestro":"Paolo","cognomeMaestro":"Ventresca","data":"2099-08-16","ora":"17:00","livello":"BASE"}
]}')
check "POST /studente/prenotazioni strumento non valido" 400 "$code" "$(cat /tmp/glam_resp.json)"

echo ""
echo "=================== RISULTATO: $PASS OK, $FAIL FALLITI ==================="
if [ "$FAIL" -ne 0 ]; then
  echo "ALCUNI TEST SONO FALLITI"
  exit 1
fi
echo "TUTTI I TEST SONO PASSATI"
