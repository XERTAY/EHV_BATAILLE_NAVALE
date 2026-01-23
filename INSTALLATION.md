# 🚀 Guide d'Installation

Ce guide vous explique comment initialiser et lancer le projet Bataille Navale.

## 📋 Prérequis

- **Java 17+** : [Télécharger](https://adoptium.net/)
- **Maven 3.9+** : [Télécharger](https://maven.apache.org/download.cgi)
- **Node.js 18+** : [Télécharger](https://nodejs.org/)
- **Rust** (pour Tauri) : 
  ```bash
  curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh
  ```
- **Docker & Docker Compose** (optionnel, pour déploiement) : [Télécharger](https://www.docker.com/)

### 🚀 Installation Automatique des Prérequis (Arch Linux)

Pour installer automatiquement tous les prérequis sur Arch Linux, utilisez le script suivant :

```bash
bash install-prerequisites.sh
```

Ce script vérifie automatiquement quels outils sont déjà installés et vous propose d'installer ceux qui manquent. Il utilise :
- **pacman** pour les paquets officiels d'Arch Linux
- **yay** ou **paru** (si disponible) pour les paquets AUR si nécessaire

> ⚠️ **Note** : Après l'installation de certains outils (notamment Rust via rustup), vous devrez peut-être redémarrer votre terminal pour que les changements prennent effet.

## 🔧 Installation

### 1. Backend Java

```bash
cd backend
mvn clean install
```

Le backend sera disponible sur `http://localhost:8080`

### 2. Frontend React

```bash
cd frontend
npm install
npm run dev
```

Le frontend sera disponible sur `http://localhost:5173`

### 3. Tauri Desktop (optionnel)

```bash
cd tauri
npm install
npm run tauri:dev
```

## 🎯 Prochaines étapes

Selon le plan d'implémentation :

1. **Phase 1** : Compléter le modèle Java pur (classes Ship, Grid, Fleet, Player, Game)
2. **Phase 2** : Implémenter les controllers Spring Boot et WebSocket
3. **Phase 3** : Développer l'interface React (grille cliquable)
4. **Phase 4** : Finaliser l'intégration Tauri
5. **Phase 5** : Ajouter les fonctionnalités avancées (sauvegarde, modes alternatifs)

## 📝 Notes

- Le backend utilise H2 Database en mode fichier pour le développement
- Le frontend est configuré avec un proxy vers le backend sur le port 8080
- Tauri partage le code source du frontend React


