#!/usr/bin/env bash
#   Use this script to test if a given TCP host/port are available

set -e

HOST="$1"
shift
PORT="${HOST##*:}"
HOST="${HOST%%:*}"

TIMEOUT=60
QUIET=0

while [[ $# -gt 0 ]]
do
    case "$1" in
        --timeout=*)
        TIMEOUT="${1#*=}"
        shift
        ;;
        --quiet)
        QUIET=1
        shift
        ;;
        --)
        shift
        break
        ;;
        *)
        break
        ;;
    esac
done

if [[ $QUIET -ne 1 ]]; then
  echo "Attente de la disponibilité de $HOST:$PORT pendant $TIMEOUT secondes..."
fi

for ((i=0;i<TIMEOUT;i++)); do
  if nc -z "$HOST" "$PORT"; then
    if [[ $QUIET -ne 1 ]]; then
      echo "$HOST:$PORT est disponible !"
    fi
    exec "$@"
    exit 0
  fi
  sleep 1
done

echo "Timeout après $TIMEOUT secondes en attendant $HOST:$PORT"
exit 1
