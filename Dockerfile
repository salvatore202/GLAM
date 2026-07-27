FROM ubuntu:22.04

ENV DEBIAN_FRONTEND=noninteractive

# Installa MySQL, OpenJDK 17, Node.js 20, Maven, Git, locale UTF-8 e dipendenze
RUN apt-get update && apt-get install -y \
    mysql-server \
    openjdk-17-jdk \
    maven \
    curl \
    git \
    build-essential \
    locales \
    && locale-gen en_US.UTF-8 \
    && curl -fsSL https://deb.nodesource.com/setup_20.x | bash - \
    && apt-get install -y nodejs \
    && rm -rf /var/lib/apt/lists/*

# IMPORTANTE: deve valere per build (javac, npm, ecc.) e per il container a runtime,
# altrimenti nomi file con caratteri accentati (es. GiornoDisponibilitàDAO.java)
# vengono corrotti e la compilazione fallisce.
ENV LANG=en_US.UTF-8
ENV LC_ALL=en_US.UTF-8
ENV LANGUAGE=en_US.UTF-8

# IMPOSTAZIONE CRITICA PER LINUX: Configura lower_case_table_names = 1 prima di inizializzare MySQL
RUN service mysql stop || true \
    && rm -rf /var/lib/mysql/* \
    && echo "[mysqld]" >> /etc/mysql/mysql.conf.d/mysqld.cnf \
    && echo "lower_case_table_names = 1" >> /etc/mysql/mysql.conf.d/mysqld.cnf \
    && mysqld --initialize-insecure --user=mysql --lower-case-table-names=1

WORKDIR /app

# Copia l'intero repository nel container
COPY . /app

# Assegna i permessi agli script
RUN chmod +x scripts/*.sh 2>/dev/null || true

# Pre-installa le dipendenze npm del frontend durante la build
RUN if [ -d "frontend" ]; then \
    cd frontend && npm install; \
    fi

# Script d'ingresso per avviare DB, Backend Java e Frontend React
RUN printf '%s\n' \
'#!/bin/bash' \
'' \
'echo "==> [1/3] Avvio del Database (MySQL)..."' \
'service mysql start' \
'' \
'until mysqladmin ping --silent; do' \
'    echo "In attesa che il database sia pronto..."' \
'    sleep 1' \
'done' \
'' \
'# Garantisce i permessi root senza password' \
'mysql -u root -e "ALTER USER '"'"'root'"'"'@'"'"'localhost'"'"' IDENTIFIED WITH mysql_native_password BY '"'"''"'"'; FLUSH PRIVILEGES;" 2>/dev/null || true' \
'' \
'echo "==> [2/3] Avvio Backend Java e popolamento DB (scripts/dev-up.sh)..."' \
'cd /app' \
'bash scripts/dev-up.sh &' \
'BACKEND_PID=$!' \
'' \
'echo "==> [3/3] Avvio Frontend React (Vite)..."' \
'cd /app/frontend' \
'npm run dev -- --host 0.0.0.0 &' \
'FRONTEND_PID=$!' \
'' \
'echo "========================================================="' \
'echo "  GLAM AVVIATO CON SUCCESSO!"' \
'echo "  Frontend: http://localhost:5173"' \
'echo "  Backend:  http://localhost:8080"' \
'echo "========================================================="' \
'' \
'# Se un processo termina, logga il motivo ma NON fare morire il container:' \
'# vogliamo che almeno il frontend resti raggiungibile per il debug.' \
'trap "echo \"Ricevuto segnale di stop, chiudo i processi...\"; kill $BACKEND_PID $FRONTEND_PID 2>/dev/null" SIGTERM SIGINT' \
'' \
'wait $BACKEND_PID' \
'BACKEND_EXIT=$?' \
'if [ $BACKEND_EXIT -ne 0 ]; then' \
'    echo "!!! ATTENZIONE: il backend Java è terminato con errore (exit $BACKEND_EXIT)."' \
'    echo "!!! Controlla i log sopra (es. errori di compilazione javac)."' \
'fi' \
'' \
'wait $FRONTEND_PID' \
> /entrypoint.sh && chmod +x /entrypoint.sh

EXPOSE 5173 8080 3306

ENTRYPOINT ["/entrypoint.sh"]