#!/bin/bash
# Avvio rapido del backend per lo sviluppo locale: ricarica il database da
# zero (schema originale + fixture di test) e avvia Boundary.web.ApiServer.
#
# Presuppone MySQL gia' in esecuzione e raggiungibile come root senza
# password su localhost:3306 (le stesse credenziali hardcoded in
# DAO/DBManager.java). Su Linux ricorda di impostare
# lower_case_table_names=1 prima di creare le tabelle la prima volta (vedi
# WEB_LAYER.md) - su Windows/macOS e' gia' cosi' di default.
#
# Uso:
#   bash scripts/dev-up.sh            # ricarica il DB e avvia il server (porta 8080)
#   bash scripts/dev-up.sh --no-db    # avvia solo il server, senza toccare il DB

set -euo pipefail
cd "$(dirname "$0")/.."

if [[ "${1:-}" != "--no-db" ]]; then
  echo "==> Ricarico il database da zero (schema originale + fixture di test)"
  mysql -u root -e "DROP DATABASE IF EXISTS databaseglam10;"
  mysql -u root < Database/MySql/databaseglam10.sql
  mysql -u root < Database/MySql/test_fixtures.sql
else
  echo "==> --no-db: database non toccato"
fi

echo "==> Compilo il backend"
mkdir -p build/classes
javac -encoding UTF-8 -d build/classes -cp "src/lib/mysql-connector-j-8.3.0.jar" \
  $(find src/Boundary src/Control src/DAO src/Entity src/Exception -name "*.java")

echo "==> Avvio Boundary.web.ApiServer sulla porta ${PORT:-8080}"
exec java -cp "build/classes:src/lib/mysql-connector-j-8.3.0.jar" Boundary.web.ApiServer
