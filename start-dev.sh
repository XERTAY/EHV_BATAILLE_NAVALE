#!/bin/bash
# Script de lancement pour le développement local
# Usage: bash start-dev.sh [backend|frontend|all]

echo "🚀 Lancement du projet Bataille Navale"
echo ""

# Fonction pour lancer le backend
start_backend() {
    echo "☕ Démarrage du backend Spring Boot..."
    cd backend
    
    # Vérifier si Maven a déjà compilé
    if [ ! -f "target/battleship-backend-1.0.0.jar" ]; then
        echo "📦 Compilation du backend avec Maven..."
        mvn clean install -DskipTests
        if [ $? -ne 0 ]; then
            echo "❌ Erreur lors de la compilation du backend"
            return 1
        fi
    fi
    
    echo "🚀 Lancement du backend sur http://localhost:8080"
    echo "   (Appuyez sur Ctrl+C pour arrêter)"
    echo ""
    mvn spring-boot:run
}

# Fonction pour lancer le frontend
start_frontend() {
    echo "⚛️  Démarrage du frontend React..."
    cd frontend
    
    # Vérifier si node_modules existe
    if [ ! -d "node_modules" ]; then
        echo "📦 Installation des dépendances npm..."
        npm install
        if [ $? -ne 0 ]; then
            echo "❌ Erreur lors de l'installation des dépendances"
            return 1
        fi
    fi
    
    echo "🚀 Lancement du frontend sur http://localhost:5173"
    echo "   (Appuyez sur Ctrl+C pour arrêter)"
    echo ""
    npm run dev
}

# Fonction pour lancer les deux
start_all() {
    echo "🚀 Lancement du backend et du frontend..."
    echo ""
    
    # Lancer le backend en arrière-plan
    cd backend
    if [ ! -f "target/battleship-backend-1.0.0.jar" ]; then
        echo "📦 Compilation du backend..."
        mvn clean install -DskipTests
    fi
    echo "☕ Démarrage du backend..."
    mvn spring-boot:run > /tmp/battleship-backend.log 2>&1 &
    BACKEND_PID=$!
    echo "   Backend démarré (PID: $BACKEND_PID)"
    echo "   Logs: tail -f /tmp/battleship-backend.log"
    echo ""
    
    # Attendre un peu que le backend démarre
    sleep 5
    
    # Lancer le frontend
    cd ../frontend
    if [ ! -d "node_modules" ]; then
        echo "📦 Installation des dépendances frontend..."
        npm install
    fi
    echo "⚛️  Démarrage du frontend..."
    npm run dev
    
    # Nettoyer à la sortie
    trap "kill $BACKEND_PID 2>/dev/null" EXIT
}

# Gestion des arguments
case "${1:-all}" in
    backend)
        start_backend
        ;;
    frontend)
        start_frontend
        ;;
    all|*)
        start_all
        ;;
esac

