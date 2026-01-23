#!/bin/bash
# Script d'installation des prérequis pour Bataille Navale (Arch Linux)
# Usage: bash install-prerequisites.sh

echo "🚀 Installation des prérequis pour Bataille Navale (Arch Linux)"
echo ""

# Vérifier que nous sommes sur Arch Linux
if [ ! -f /etc/arch-release ]; then
    echo "⚠️  Ce script est conçu pour Arch Linux"
    read -p "Voulez-vous continuer quand même ? (O/N) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Oo]$ ]]; then
        exit 1
    fi
fi

# Fonction pour vérifier si une commande existe
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Fonction pour vérifier si un paquet est installé via pacman
package_installed() {
    pacman -Qi "$1" >/dev/null 2>&1
}

# Fonction pour trouver un AUR helper
find_aur_helper() {
    if command_exists yay; then
        echo "yay"
    elif command_exists paru; then
        echo "paru"
    else
        echo ""
    fi
}

# Fonction pour mettre à jour les miroirs
update_mirrors() {
    echo "🔄 Mise à jour des miroirs Arch Linux..."
    if command_exists pacman-mirrors; then
        echo "   Exécution de pacman-mirrors -g..."
        sudo pacman-mirrors -g
    else
        echo "   pacman-mirrors n'est pas disponible (Manjaro uniquement)"
        echo "   Pour Arch Linux standard, mettez à jour manuellement /etc/pacman.d/mirrorlist"
    fi
    echo "   Mise à jour de la base de données des paquets..."
    sudo pacman -Sy
    echo "✅ Miroirs mis à jour"
}

# Fonction pour installer un paquet avec gestion des erreurs 404
install_package_with_mirror_fix() {
    local package_name="$1"
    local packages="$2"
    local log_file="/tmp/pacman_install_${package_name}.log"
    
    echo "📦 Installation de $package_name depuis les dépôts officiels..."
    sudo pacman -S --noconfirm $packages 2>&1 | tee "$log_file"
    local pacman_exit_code=${PIPESTATUS[0]}
    
    if [ $pacman_exit_code -eq 0 ]; then
        rm -f "$log_file"
        return 0
    else
        # Vérifier si c'est une erreur 404 (miroirs obsolètes)
        if grep -q "404\|échec de récupération" "$log_file" 2>/dev/null; then
            echo "⚠️  Erreurs 404 détectées - vos miroirs semblent obsolètes."
            read -p "Voulez-vous mettre à jour les miroirs et réessayer ? (O/N) " -n 1 -r
            echo
            if [[ $REPLY =~ ^[Oo]$ ]]; then
                update_mirrors
                echo "📦 Nouvelle tentative d'installation de $package_name..."
                sudo pacman -S --noconfirm $packages
                if [ $? -eq 0 ]; then
                    rm -f "$log_file"
                    return 0
                fi
            fi
        fi
        rm -f "$log_file"
        return 1
    fi
}

# Fonction pour vérifier la version de Java
check_java() {
    if command_exists java; then
        version=$(java -version 2>&1 | head -n 1 | sed -E 's/.*version "([0-9]+).*/\1/')
        if [ "$version" -ge 17 ]; then
            echo "✅ Java $version détecté"
            return 0
        else
            echo "❌ Java version $version détectée, mais Java 17+ est requis"
            return 1
        fi
    fi
    return 1
}

# Fonction pour vérifier la version de Maven
check_maven() {
    if command_exists mvn; then
        version=$(mvn -version 2>&1 | head -n 1 | sed -E 's/.*Apache Maven ([0-9]+\.[0-9]+).*/\1/')
        major=$(echo "$version" | cut -d. -f1)
        minor=$(echo "$version" | cut -d. -f2)
        if [ "$major" -gt 3 ] || ([ "$major" -eq 3 ] && [ "$minor" -ge 9 ]); then
            echo "✅ Maven $version détecté"
            return 0
        else
            echo "❌ Maven version $version détectée, mais Maven 3.9+ est requis"
            return 1
        fi
    fi
    return 1
}

# Fonction pour vérifier la version de Node.js
check_node() {
    if command_exists node; then
        version=$(node -v | sed 's/v//' | cut -d. -f1)
        if [ "$version" -ge 18 ]; then
            echo "✅ Node.js $(node -v) détecté"
            return 0
        else
            echo "❌ Node.js version $(node -v) détectée, mais Node.js 18+ est requis"
            return 1
        fi
    fi
    return 1
}

# Fonction pour vérifier Rust
check_rust() {
    if command_exists rustc; then
        # Tester si rustc fonctionne vraiment
        version_output=$(rustc -V 2>&1)
        if echo "$version_output" | grep -q "error:"; then
            # rustc existe mais n'est pas fonctionnel (problème avec rustup)
            return 1
        fi
        version=$(echo "$version_output" | head -n 1)
        echo "✅ Rust détecté: $version"
        return 0
    fi
    return 1
}

# Fonction pour vérifier Docker
check_docker() {
    if command_exists docker; then
        version=$(docker -v 2>&1)
        echo "✅ Docker détecté: $version"
        return 0
    fi
    return 1
}

# Installation de Java 17
install_java() {
    echo "☕ Installation de Java 17..."
    if package_installed jdk17-openjdk || package_installed jdk17-temurin-bin; then
        echo "✅ Java 17 déjà installé via pacman"
        return 0
    fi
    
    # Essayer d'installer depuis les dépôts officiels avec gestion des miroirs
    if install_package_with_mirror_fix "Java" "jdk17-openjdk"; then
        echo "✅ Java 17 installé avec succès"
        return 0
    fi
    
    # Si échec, essayer avec un AUR helper
    aur_helper=$(find_aur_helper)
    if [ -n "$aur_helper" ]; then
        echo "📦 Installation de jdk17-temurin-bin depuis l'AUR avec $aur_helper..."
        $aur_helper -S --noconfirm jdk17-temurin-bin
        if [ $? -eq 0 ]; then
            echo "✅ Java 17 installé avec succès depuis l'AUR"
            return 0
        fi
    fi
    
    echo "❌ Échec de l'installation. Veuillez installer Java 17 manuellement :"
    echo "   sudo pacman -S jdk17-openjdk"
    echo "   ou avec yay/paru : yay -S jdk17-temurin-bin"
    return 1
}

# Installation de Maven
install_maven() {
    echo "🔨 Installation de Maven..."
    if package_installed maven; then
        echo "✅ Maven déjà installé"
        return 0
    fi
    
    if install_package_with_mirror_fix "Maven" "maven"; then
        echo "✅ Maven installé avec succès"
        return 0
    else
        echo "❌ Échec de l'installation. Veuillez installer Maven manuellement :"
        echo "   sudo pacman -S maven"
        return 1
    fi
}

# Installation de Node.js
install_node() {
    echo "📦 Installation de Node.js..."
    if package_installed nodejs; then
        echo "✅ Node.js déjà installé"
        return 0
    fi
    
    if install_package_with_mirror_fix "Node.js" "nodejs npm"; then
        echo "✅ Node.js installé avec succès"
        return 0
    else
        echo "❌ Échec de l'installation. Veuillez installer Node.js manuellement :"
        echo "   sudo pacman -S nodejs npm"
        return 1
    fi
}

# Installation de Rust
install_rust() {
    echo "🦀 Installation de Rust..."
    
    # Vérifier si rust est installé via pacman
    if package_installed rust; then
        echo "✅ Rust déjà installé via pacman"
        return 0
    fi
    
    # Proposer l'installation via pacman ou rustup
    echo "Choisissez la méthode d'installation :"
    echo "1) Via pacman (recommandé pour Arch Linux)"
    echo "2) Via rustup (méthode officielle)"
    read -p "Votre choix (1 ou 2) : " choice
    
    if [ "$choice" = "1" ]; then
        if install_package_with_mirror_fix "Rust" "rust"; then
            echo "✅ Rust installé avec succès"
            return 0
        else
            echo "❌ Échec de l'installation via pacman"
            return 1
        fi
    else
        echo "📦 Installation de Rust via rustup..."
        # Si rustup est déjà installé mais cassé, le réinstaller
        if [ -f "$HOME/.cargo/env" ]; then
            echo "   Réparation de l'installation rustup existante..."
            source "$HOME/.cargo/env"
            rustup self uninstall -y 2>/dev/null
        fi
        curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh -s -- -y
        if [ -f "$HOME/.cargo/env" ]; then
            source "$HOME/.cargo/env"
            # Installer le composant rustc
            rustup component add rustc 2>/dev/null
            if rustc -V >/dev/null 2>&1; then
                echo "✅ Rust installé avec succès via rustup"
                return 0
            else
                echo "❌ Rust installé mais rustc n'est pas fonctionnel"
                return 1
            fi
        else
            echo "❌ Échec de l'installation via rustup"
            return 1
        fi
    fi
}

# Installation de Docker (optionnel)
install_docker() {
    echo "🐳 Installation de Docker..."
    
    if package_installed docker && package_installed docker-compose; then
        echo "✅ Docker et Docker Compose déjà installés"
        return 0
    fi
    
    if install_package_with_mirror_fix "Docker" "docker docker-compose"; then
        echo "✅ Docker installé avec succès"
        echo "⚠️  N'oubliez pas d'activer et démarrer le service Docker :"
        echo "   sudo systemctl enable docker"
        echo "   sudo systemctl start docker"
        echo "   sudo usermod -aG docker \$USER"
        return 0
    else
        echo "❌ Échec de l'installation de Docker."
        echo "   Veuillez installer Docker manuellement :"
        echo "   sudo pacman -S docker docker-compose"
        return 1
    fi
}

# Vérifications et installations
echo "🔍 Vérification des prérequis..."
echo ""

needs_restart=false
installation_failed=false
docker_failed=false

# Java
if ! check_java; then
    read -p "Voulez-vous installer Java 17 ? (O/N) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Oo]$ ]]; then
        if install_java; then
            # Vérifier à nouveau après installation
            if check_java; then
                needs_restart=true
            else
                installation_failed=true
            fi
        else
            installation_failed=true
        fi
    else
        installation_failed=true
    fi
fi

# Maven
if ! check_maven; then
    read -p "Voulez-vous installer Maven ? (O/N) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Oo]$ ]]; then
        if install_maven; then
            # Vérifier à nouveau après installation
            if check_maven; then
                needs_restart=true
            else
                installation_failed=true
            fi
        else
            installation_failed=true
        fi
    else
        installation_failed=true
    fi
fi

# Node.js
if ! check_node; then
    read -p "Voulez-vous installer Node.js ? (O/N) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Oo]$ ]]; then
        if install_node; then
            # Vérifier à nouveau après installation
            if check_node; then
                needs_restart=true
            else
                installation_failed=true
            fi
        else
            installation_failed=true
        fi
    else
        installation_failed=true
    fi
fi

# Rust
if ! check_rust; then
    read -p "Voulez-vous installer Rust (requis pour Tauri) ? (O/N) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Oo]$ ]]; then
        if install_rust; then
            # Vérifier à nouveau après installation
            if check_rust; then
                needs_restart=true
            else
                installation_failed=true
            fi
        else
            installation_failed=true
        fi
    else
        installation_failed=true
    fi
fi

# Docker (optionnel)
if ! check_docker; then
    read -p "Voulez-vous installer Docker (optionnel) ? (O/N) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Oo]$ ]]; then
        if ! install_docker; then
            docker_failed=true
        fi
    fi
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Vérification finale de tous les prérequis essentiels
java_ok=false
maven_ok=false
node_ok=false
rust_ok=false

check_java >/dev/null 2>&1 && java_ok=true
check_maven >/dev/null 2>&1 && maven_ok=true
check_node >/dev/null 2>&1 && node_ok=true
check_rust >/dev/null 2>&1 && rust_ok=true

if [ "$needs_restart" = true ]; then
    echo "⚠️  Certains outils ont été installés. Veuillez redémarrer votre terminal"
    echo "   et relancer ce script pour vérifier que tout est correctement installé."
elif [ "$installation_failed" = true ] || [ "$java_ok" = false ] || [ "$maven_ok" = false ] || [ "$node_ok" = false ] || [ "$rust_ok" = false ]; then
    echo "❌ Certains prérequis ne sont pas installés ou l'installation a échoué :"
    [ "$java_ok" = false ] && echo "   - Java 17+"
    [ "$maven_ok" = false ] && echo "   - Maven 3.9+"
    [ "$node_ok" = false ] && echo "   - Node.js 18+"
    [ "$rust_ok" = false ] && echo "   - Rust"
    echo ""
    echo "💡 Essayez de les installer manuellement :"
    [ "$java_ok" = false ] && echo "   sudo pacman -S jdk17-openjdk"
    [ "$maven_ok" = false ] && echo "   sudo pacman -S maven"
    [ "$node_ok" = false ] && echo "   sudo pacman -S nodejs npm"
    [ "$rust_ok" = false ] && echo "   sudo pacman -S rust"
    echo ""
    if [ "$docker_failed" = true ]; then
        echo "⚠️  L'installation de Docker a échoué. Cela peut être dû à des miroirs obsolètes."
        echo "   Essayez de mettre à jour les miroirs :"
        echo "   sudo pacman-mirrors -g"
        echo "   sudo pacman -Syu"
        echo "   sudo pacman -S docker docker-compose"
    fi
else
    echo "✅ Tous les prérequis essentiels sont installés !"
    if [ "$docker_failed" = true ]; then
        echo "⚠️  Docker n'a pas pu être installé (optionnel)."
    fi
    echo ""
    echo "📝 Prochaines étapes:"
    echo "   1. cd backend && mvn clean install"
    echo "   2. cd frontend && npm install"
    echo "   3. cd tauri && npm install"
fi

