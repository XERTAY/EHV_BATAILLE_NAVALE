# 🚢 Bataille Navale - Monorepo Moderne

Projet de Bataille Navale respectant les règles classiques : placement de navires, tirs tour par tour, multijoueur temps réel.

**Architecture MVC stricte** : Modèle Java pur, Backend Spring Boot, Frontend React, Client Desktop Tauri.

## 📋 Vue d'ensemble

### Architecture globale

```mermaid
graph TB
    subgraph Frontend["Frontend"]
        WEB["React Web<br/>VPS (Nginx)"]
        DESKTOP["Tauri Desktop<br/>Windows/Mac/Linux"]
    end
    
    subgraph Backend["Backend"]
        API["Spring Boot API<br/>REST + WebSocket"]
    end
    
    subgraph Domain["Domain Layer"]
        MODEL["Modèle Java Pur<br/>Grid, Ship, Game, Fleet"]
    end
    
    WEB -->|HTTP/WebSocket| API
    DESKTOP -->|HTTP/WebSocket| API
    API --> MODEL
    
    style MODEL fill:#e1f5ff
    style API fill:#fff4e1
    style WEB fill:#e8f5e9
    style DESKTOP fill:#e8f5e9
```

### Flux de données

```mermaid
sequenceDiagram
    participant U as Utilisateur
    participant F as Frontend (React/Tauri)
    participant B as Backend (Spring Boot)
    participant D as Domain (Java Pur)
    
    U->>F: Placement navire / Tir
    F->>B: HTTP POST /api/games/{id}/action
    B->>D: Appel logique métier
    D-->>B: Résultat (HIT/MISS)
    B-->>F: JSON Response
    F-->>U: Mise à jour UI
    
    Note over F,B: Multijoueur temps réel
    B->>F: WebSocket STOMP /ws
    F->>U: Notification temps réel
```

## 🎯 Fonctionnalités

### Principales (requises)

- ✅ Respect des règles du jeu classique
- ✅ Interface contrôlable intégralement à la souris
- ✅ Écran de sélection des options :
  - Choix du nombre et type de navires
  - Choix de la taille de la grille
  - Mode solo (IA) / multijoueur tour par tour
- ✅ Sauvegarde/chargement de partie

### Avancées (bonus)

- 🌐 Multijoueur temps réel (WebSocket)
- ☁️ Hébergement VPS sécurisé (Docker, reverse proxy, SSL)
- 💻 Client desktop natif (Tauri)
- 🎮 Modes alternatifs : Radar, Artillerie

## 🗂️ Structure du projet

```
battleship-monorepo/
├── README.md
├── backend/                    # Java Spring Boot (MVC)
│   ├── domain/                 # MODÈLE JAVA PUR
│   │   ├── model/
│   │   │   ├── Grid.java
│   │   │   ├── Ship.java
│   │   │   ├── Fleet.java
│   │   │   └── Game.java
│   │   └── ports/              # Interfaces hexagonal
│   ├── application/            # Services métier
│   ├── infrastructure/         # WebSocket, persistence
│   ├── pom.xml
│   └── target/battleship.jar
├── frontend/                   # React (web + Tauri)
│   ├── src/
│   │   ├── components/
│   │   │   └── Grid.tsx
│   │   ├── hooks/
│   │   │   └── useWebSocket.ts
│   │   └── App.tsx
│   ├── package.json
│   └── vite.config.ts
├── tauri/                      # Client desktop
│   ├── src-tauri/
│   │   ├── Cargo.toml
│   │   ├── src/lib.rs         # Lance JAR Java
│   │   └── tauri.conf.json
│   └── src/                   # → Symlink frontend/
├── docker-compose.yml         # Dev/prod
├── Dockerfile                 # Image backend
├── Dockerfile.frontend        # Image frontend (nginx)
├── nginx.conf                 # Configuration reverse proxy
└── deploy/                    # Scripts déploiement VPS
```

## 🏗️ Architecture technique

### Séparation des couches

```mermaid
graph TD
    subgraph Presentation["Présentation"]
        UI["React Components"]
    end
    
    subgraph Application["Application"]
        CTRL["Spring Controllers"]
        WS["WebSocket Handler"]
    end
    
    subgraph Domain["Domain (Java Pur)"]
        G["Grid"]
        S["Ship"]
        F["Fleet"]
        GM["Game Logic"]
    end
    
    subgraph Infrastructure["Infrastructure"]
        DB[("Persistence")]
        WS_IMPL["WebSocket Impl"]
    end
    
    UI --> CTRL
    UI --> WS
    CTRL --> G
    CTRL --> S
    CTRL --> F
    WS --> GM
    GM --> G
    GM --> S
    GM --> F
    CTRL --> DB
    WS --> WS_IMPL
    
    style G fill:#e1f5ff
    style S fill:#e1f5ff
    style F fill:#e1f5ff
    style GM fill:#e1f5ff
```

### Règles du jeu

**Configuration standard :**
- Grille : 10x10 (configurable)
- Navires :
  - 1 Porte-avions (5 cases)
  - 2 Croiseurs (4 cases)
  - 3 Destroyers (3 cases)
  - 4 Sous-marins (2 cases)

**Flux de jeu :**

```mermaid
stateDiagram-v2
    [*] --> Configuration: Démarrer
    Configuration --> Placement: Valider options
    Placement --> Placement: Placer navires
    Placement --> Attente: Tous navires placés
    Attente --> TourJoueur1: Partie démarrée
    TourJoueur1 --> Vérification: Tir effectué
    Vérification --> TourJoueur2: Tour suivant
    Vérification --> Fin: Tous navires coulés
    TourJoueur2 --> Vérification: Tir effectué
    Fin --> [*]
```

## 🚀 Installation & Lancement

### Prérequis

- Java 17+ (Maven)
- Node.js 18+
- Rust (pour Tauri) : `curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh`
- Docker & Docker Compose (pour déploiement VPS)

### Développement local

```bash
# Backend Java
cd backend && mvn clean install
java -jar target/battleship.jar  # Port 8080

# Frontend web (nouvel terminal)
cd frontend && npm i && npm run dev  # http://localhost:3000

# Client Tauri (desktop)
cd tauri && npm i && npm run tauri dev
```

### Build production

```bash
# Backend JAR
mvn clean package -Pprod

# Frontend web
npm run build:web  # dist/ pour hébergement

# Desktop (tous OS)
npm run tauri build  # Bundle avec JAR embarqué
```

## ☁️ Déploiement VPS

### Architecture sécurisée avec Docker

Le projet est déployé sur un VPS avec une architecture Docker sécurisée :

- **Backend** : Container Spring Boot isolé
- **Frontend** : Container Nginx servant les fichiers statiques React
- **Reverse Proxy** : Nginx avec SSL/TLS (Let's Encrypt)
- **Sécurité** : Firewall, isolation réseau, variables d'environnement

### Architecture de déploiement

```mermaid
graph TB
    subgraph Internet["Internet"]
        U["Utilisateurs"]
    end
    
    subgraph VPS["VPS"]
        subgraph ReverseProxy["Reverse Proxy"]
            NGINX["Nginx<br/>SSL/TLS<br/>Port 443/80"]
        end
        
        subgraph DockerNetwork["Docker Network"]
            FE["Container Frontend<br/>Nginx + React Build"]
            BE["Container Backend<br/>Spring Boot JAR"]
        end
        
        subgraph Volumes["Volumes"]
            SSL["SSL Certificates"]
            DATA["App Data"]
        end
    end
    
    U -->|HTTPS| NGINX
    NGINX -->|HTTP| FE
    NGINX -->|HTTP| BE
    NGINX -.->|Certificats| SSL
    BE -.->|Sauvegardes| DATA
    
    style NGINX fill:#ff6b6b
    style FE fill:#e8f5e9
    style BE fill:#fff4e1
    style SSL fill:#ffe66d
    style DATA fill:#ffe66d
```

### Préparation du déploiement

#### 1. Build des images

```bash
# Backend
cd backend
mvn clean package -DskipTests
docker build -t battleship-backend:latest -f ../Dockerfile .

# Frontend
cd frontend
npm run build
docker build -t battleship-frontend:latest -f ../Dockerfile.frontend .
```

#### 2. Configuration VPS

```bash
# Sur le VPS
git clone <repo>
cd battleship-monorepo

# Configuration des variables d'environnement
cp .env.example .env
# Éditer .env avec vos configurations
```

#### 3. Déploiement avec Docker Compose

```bash
# Démarrage des services
docker-compose up -d

# Vérification des logs
docker-compose logs -f

# Arrêt
docker-compose down
```

### Sécurisation

#### Firewall (UFW)

```bash
# Sur le VPS
sudo ufw allow 22/tcp    # SSH
sudo ufw allow 80/tcp    # HTTP (redirection vers HTTPS)
sudo ufw allow 443/tcp   # HTTPS
sudo ufw enable
```

#### SSL/TLS avec Let's Encrypt

```bash
# Installation Certbot
sudo apt install certbot python3-certbot-nginx

# Génération certificat
sudo certbot --nginx -d votre-domaine.com

# Renouvellement automatique
sudo certbot renew --dry-run
```

#### Configuration Nginx (reverse proxy)

Le fichier `nginx.conf` configure :
- Redirection HTTP → HTTPS
- Proxy vers backend (port 8080)
- Servir frontend statique
- Headers de sécurité (CORS, CSP, etc.)

### Variables d'environnement

Créer un fichier `.env` à la racine :

```bash
# Backend
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080
DATABASE_URL=jdbc:h2:file:/app/data/battleship
JWT_SECRET=votre-secret-jwt

# Frontend
VITE_API_URL=https://votre-domaine.com/api

# Docker
COMPOSE_PROJECT_NAME=battleship
```

### Monitoring et maintenance

```bash
# Vérifier l'état des containers
docker-compose ps

# Redémarrer un service
docker-compose restart backend

# Mettre à jour l'application
git pull
docker-compose up -d --build

# Sauvegardes
docker-compose exec backend tar -czf /app/data/backup-$(date +%Y%m%d).tar.gz /app/data
```

## 📚 Implémentation progressive

1. **Phase 1** : Modèle Java pur (tests unitaires JUnit)
2. **Phase 2** : Backend Spring Boot (REST + WebSocket)
3. **Phase 3** : Frontend React (grille cliquable)
4. **Phase 4** : Tauri Desktop (JAR embarqué)
5. **Phase 5** : Polish (sauvegarde, modes alternatifs)

## 🤝 Contribution

1. Fork → Branch `feature/nom`
2. `mvn test && npm test`
3. PR avec tests

## 📄 Licence

MIT

---

**Projet réalisé dans le cadre d'études à l'Université Paris-Saclay**
