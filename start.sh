#!/bin/bash
set -e

IMAGE_NAME="glam-app"
CONTAINER_NAME="glam_container"
URL="http://localhost:5173"
BRANCH="web-frontend"
LOG_FILE="/tmp/glam_start.log"

# ---------------------------------------------------------------------------
# Barra di progresso semplice: gira in background finché il PID passato vive
# ---------------------------------------------------------------------------
progress_bar() {
    local pid=$1
    local label=$2
    local width=30
    local i=0
    local spin='⠋⠙⠹⠸⠼⠴⠦⠧⠇⠏'

    tput civis 2>/dev/null || true
    while kill -0 "$pid" 2>/dev/null; do
        local filled=$(( (i * width) / 100 % width ))
        local bar
        bar=$(printf '%*s' "$filled" '' | tr ' ' '#')
        local empty
        empty=$(printf '%*s' "$((width - filled))" '')
        local spinner_char=${spin:$((i % ${#spin})):1}
        printf "\r%s [%s%s] %s" "$spinner_char" "$bar" "$empty" "$label"
        i=$((i + 3))
        sleep 0.15
    done
    printf "\r%*s\r" "$((width + ${#label} + 10))" ""
    tput cnorm 2>/dev/null || true
}

run_with_progress() {
    local label=$1
    shift
    ("$@" >> "$LOG_FILE" 2>&1) &
    local cmd_pid=$!
    progress_bar "$cmd_pid" "$label"
    wait "$cmd_pid"
    return $?
}

fail() {
    echo "❌ Errore durante: $1"
    echo "Ultime righe del log ($LOG_FILE):"
    tail -n 30 "$LOG_FILE"
    exit 1
}

> "$LOG_FILE"

echo "==> [1/5] Checkout branch '$BRANCH'..."
run_with_progress "Checkout $BRANCH" git fetch origin "$BRANCH" \
    && run_with_progress "Checkout $BRANCH" git checkout "$BRANCH" \
    && run_with_progress "Pull $BRANCH" git pull origin "$BRANCH" \
    || fail "checkout del branch $BRANCH"

echo "==> [2/5] Pulizia eventuali container precedenti..."
docker stop "$CONTAINER_NAME" >> "$LOG_FILE" 2>&1 || true
docker rm "$CONTAINER_NAME" >> "$LOG_FILE" 2>&1 || true

echo "==> [3/5] Build dell'immagine Docker (senza cache)..."
run_with_progress "Build immagine Docker" docker build --no-cache -t "$IMAGE_NAME" . \
    || fail "build dell'immagine Docker"

echo "==> [4/5] Avvio del container in background..."
docker run -d \
    -p 5173:5173 \
    -p 8080:8080 \
    -p 3306:3306 \
    --name "$CONTAINER_NAME" \
    "$IMAGE_NAME" >> "$LOG_FILE" 2>&1 || fail "avvio del container"

echo "==> [5/5] In attesa che Vite/React sia pronto su $URL..."
MAX_RETRIES=30
COUNT=0

(
    until curl -s -f "$URL" > /dev/null 2>&1 || [ $COUNT -eq $MAX_RETRIES ]; do
        sleep 2
        COUNT=$((COUNT + 1))
    done
) &
WAIT_PID=$!
progress_bar "$WAIT_PID" "Attesa avvio frontend"
wait "$WAIT_PID"

if curl -s -f "$URL" > /dev/null 2>&1; then
    echo "==> Frontend attivo! Apertura del browser..."
    if command -v xdg-open >/dev/null 2>&1; then
        (xdg-open "$URL" >/dev/null 2>&1 &) 
    elif command -v open >/dev/null 2>&1; then
        (open "$URL" >/dev/null 2>&1 &)
    elif command -v explorer.exe >/dev/null 2>&1; then
        (explorer.exe "$URL" >/dev/null 2>&1 &)
    else
        echo "Impossibile aprire il browser in automatico. Apri manualmente: $URL"
    fi
else
    echo "⚠️ ATTENZIONE: Il timeout è scaduto. Il container sta impiegando più del previsto ad avviarsi."
    echo "Controlla i log con: docker logs -f $CONTAINER_NAME"
fi

echo "========================================================="
echo " Container $CONTAINER_NAME avviato (branch: $BRANCH)."
echo " - Per vedere i log in tempo reale: docker logs -f $CONTAINER_NAME"
echo " - Per fermare il container:         docker stop $CONTAINER_NAME"
echo "========================================================="