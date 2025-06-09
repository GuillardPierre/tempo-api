#!/bin/sh
#   Use this script to test if a given TCP host/port are available

set -e

# Fonction pour afficher l'utilisation
usage() {
    echo "Usage: $0 host:port [-t timeout] [-- command args]"
    exit 1
}

# Initialisation des variables
TIMEOUT=15
QUIET=0
HOST=""
PORT=""

# Traitement des arguments
if echo "$1" | grep -q ":"; then
    HOST=$(echo "$1" | cut -d: -f1)
    PORT=$(echo "$1" | cut -d: -f2)
    shift
else
    echo "Error: Invalid host:port format"
    usage
fi

# Attente de la disponibilité du service
wait_for() {
    echo "Waiting for $HOST:$PORT..."
    
    for i in $(seq 1 $TIMEOUT); do
        if nc -z "$HOST" "$PORT" > /dev/null 2>&1; then
            if [ $? -eq 0 ]; then
                echo "$HOST:$PORT is available"
                return 0
            fi
        fi
        sleep 1
    done
    echo "Timeout occurred after waiting $TIMEOUT seconds for $HOST:$PORT"
    return 1
}

# Exécution de la commande
execute_cmd() {
    # Supprime le premier argument (--) s'il existe
    if [ "$1" = "--" ]; then
        shift
    fi
    
    if [ ! -z "$1" ]; then
        echo "Executing command: $@"
        exec "$@"
    fi
}

# Programme principal
wait_for
RESULT=$?
if [ $RESULT -eq 0 ]; then
    # Supprime host:port des arguments et exécute la commande
    shift
    execute_cmd "$@"
else
    exit $RESULT
fi
