# 🎯 Modèle Java Pur - Architecture Détaillée

Ce document décrit l'architecture complète du **modèle Java pur** de la Bataille Navale. Le modèle est **100% indépendant** de toute infrastructure (Spring, HTTP, WebSocket) et peut être utilisé directement via console pour le debugging.

## 📋 Table des matières

1. [Vue d'ensemble](#vue-densemble)
2. [Architecture du modèle](#architecture-du-modèle)
3. [Classes principales](#classes-principales)
4. [Diagrammes de classes](#diagrammes-de-classes)
5. [Flux de jeu](#flux-de-jeu)
6. [Utilisation console](#utilisation-console)
7. [Exemples de code](#exemples-de-code)

## 🎯 Vue d'ensemble

### Principe fondamental

Le modèle Java pur contient **toute la logique métier** du jeu de Bataille Navale. Il est conçu pour être :
- ✅ **Indépendant** : Aucune dépendance à Spring, HTTP, WebSocket
- ✅ **Testable** : Utilisable directement via console pour debugging
- ✅ **Réutilisable** : Peut être utilisé par Spring Boot, Tauri, ou tout autre framework
- ✅ **Complet** : Gère toutes les règles du jeu

### Architecture en couches

```mermaid
graph TB
    subgraph Model["Modèle Java Pur"]
        subgraph Core["Core Domain"]
            C["Coordinate"]
            CS["CellStatus"]
            SO["ShipOrientation"]
        end
        
        subgraph Entities["Entités"]
            S["Ship"]
            G["Grid"]
            F["Fleet"]
            P["Player"]
        end
        
        subgraph Game["Game Logic"]
            GM["Game"]
            GS["GameState"]
            GT["GameTurn"]
        end
        
        subgraph Services["Services Métier"]
            PS["PlacementService"]
            SS["ShootingService"]
            VS["ValidationService"]
        end
    end
    
    Core --> Entities
    Entities --> Game
    Game --> Services
    
    style Core fill:#e1f5ff
    style Entities fill:#e8f5e9
    style Game fill:#fff4e1
    style Services fill:#ffe66d
```

## 🏗️ Architecture du modèle

### Structure des packages

```
backend/domain/model/
├── core/
│   ├── Coordinate.java          # Position (x, y)
│   ├── CellStatus.java          # État d'une cellule (EMPTY, SHIP, HIT, MISS, SUNK)
│   └── ShipOrientation.java     # Orientation (HORIZONTAL, VERTICAL)
├── entities/
│   ├── Ship.java                # Navire avec taille, position, orientation
│   ├── Grid.java                # Grille de jeu (10x10 par défaut)
│   ├── Fleet.java               # Flotte de navires d'un joueur
│   └── Player.java              # Joueur (nom, flotte, grille)
└── game/
    ├── Game.java                # Partie complète (2 joueurs, état, tours)
    ├── GameState.java           # État de la partie (CONFIG, PLACEMENT, PLAYING, FINISHED)
    ├── GameTurn.java            # Tour de jeu (joueur actif)
    └── GameResult.java          # Résultat d'une action (HIT, MISS, SUNK, INVALID)
```

### Diagramme de dépendances

```mermaid
graph LR
    subgraph Core["Core"]
        C["Coordinate"]
        CS["CellStatus"]
        SO["ShipOrientation"]
    end
    
    subgraph Entities["Entities"]
        S["Ship"]
        G["Grid"]
        F["Fleet"]
        P["Player"]
    end
    
    subgraph Game["Game"]
        GM["Game"]
        GS["GameState"]
        GT["GameTurn"]
        GR["GameResult"]
    end
    
    C --> S
    CS --> G
    SO --> S
    S --> F
    G --> P
    F --> P
    P --> GM
    GS --> GM
    GT --> GM
    GR --> GM
    
    style Core fill:#e1f5ff
    style Entities fill:#e8f5e9
    style Game fill:#fff4e1
```

## 📦 Classes principales

### 1. Coordinate (Core)

Représente une position sur la grille.

```java
public class Coordinate {
    private final int x;  // 0-9 (ou selon taille grille)
    private final int y;  // 0-9 (ou selon taille grille)
    
    // Constructeurs, getters, equals, hashCode
    public boolean isValid(int gridSize);
    public Coordinate add(int dx, int dy);
}
```

**Diagramme de classe :**

```mermaid
classDiagram
    class Coordinate {
        -int x
        -int y
        +Coordinate(int x, int y)
        +getX() int
        +getY() int
        +isValid(int gridSize) boolean
        +add(int dx, int dy) Coordinate
        +equals(Object o) boolean
        +hashCode() int
        +toString() String
    }
```

### 2. CellStatus (Core)

Énumération des états possibles d'une cellule.

```java
public enum CellStatus {
    EMPTY,    // Case vide
    SHIP,     // Case occupée par un navire (non touchée)
    HIT,      // Case touchée (navire)
    MISS,     // Case touchée (vide)
    SUNK      // Case d'un navire coulé
}
```

### 3. ShipOrientation (Core)

Orientation d'un navire.

```java
public enum ShipOrientation {
    HORIZONTAL,  // Navire placé horizontalement
    VERTICAL     // Navire placé verticalement
}
```

### 4. Ship (Entity)

Représente un navire avec sa taille, position et orientation.

```java
public class Ship {
    private final String name;           // "Porte-avions", "Croiseur", etc.
    private final int size;              // Taille en cases (2-5)
    private Coordinate startPosition;    // Position de départ
    private ShipOrientation orientation; // Orientation
    private Set<Coordinate> hits;        // Cases touchées
    
    // Méthodes principales
    public boolean isSunk();
    public Set<Coordinate> getOccupiedCells();
    public boolean contains(Coordinate coord);
    public boolean hit(Coordinate coord);
}
```

**Diagramme de classe :**

```mermaid
classDiagram
    class Ship {
        -String name
        -int size
        -Coordinate startPosition
        -ShipOrientation orientation
        -Set hits
        +Ship(String name, int size)
        +getName() String
        +getSize() int
        +getStartPosition() Coordinate
        +getOrientation() ShipOrientation
        +getOccupiedCells() Set
        +contains(Coordinate coord) boolean
        +hit(Coordinate coord) boolean
        +isSunk() boolean
        +getHits() Set
    }
    
    Ship --> Coordinate
    Ship --> ShipOrientation
```

### 5. Grid (Entity)

Grille de jeu d'un joueur (placement des navires et tirs adverses).

```java
public class Grid {
    private final int size;                    // Taille de la grille (10 par défaut)
    private CellStatus[][] cells;              // État de chaque cellule
    private Map<Coordinate, Ship> shipMap;     // Carte navire par position
    
    // Méthodes principales
    public boolean placeShip(Ship ship, Coordinate start, ShipOrientation orientation);
    public CellStatus shoot(Coordinate coord);
    public boolean isValidPlacement(Ship ship, Coordinate start, ShipOrientation orientation);
    public boolean allShipsSunk();
    public GridView getView(boolean revealShips); // Pour affichage console
}
```

**Diagramme de classe :**

```mermaid
classDiagram
    class Grid {
        -int size
        -CellStatus[][] cells
        -Map shipMap
        +Grid(int size)
        +getSize() int
        +getCellStatus(Coordinate coord) CellStatus
        +placeShip(Ship ship, Coordinate start, ShipOrientation orientation) boolean
        +shoot(Coordinate coord) CellStatus
        +isValidPlacement(Ship ship, Coordinate start, ShipOrientation orientation) boolean
        +allShipsSunk() boolean
        +getView(boolean revealShips) GridView
    }
    
    Grid --> Coordinate
    Grid --> Ship
    Grid --> CellStatus
    Grid --> ShipOrientation
```

### 6. Fleet (Entity)

Flotte complète d'un joueur (ensemble de navires).

```java
public class Fleet {
    private final List<Ship> ships;           // Liste des navires
    private final Map<String, Integer> shipConfig; // Configuration (nom -> quantité)
    
    // Méthodes principales
    public boolean addShip(Ship ship);
    public boolean isComplete();
    public List<Ship> getShips();
    public List<Ship> getSunkShips();
    public List<Ship> getActiveShips();
}
```

**Diagramme de classe :**

```mermaid
classDiagram
    class Fleet {
        -List ships
        -Map shipConfig
        +Fleet(Map config)
        +addShip(Ship ship) boolean
        +isComplete() boolean
        +getShips() List
        +getSunkShips() List
        +getActiveShips() List
        +getShipConfig() Map
    }
    
    Fleet --> Ship
```

### 7. Player (Entity)

Joueur avec sa flotte et sa grille.

```java
public class Player {
    private final String name;
    private final Fleet fleet;
    private final Grid grid;
    
    // Méthodes principales
    public boolean placeShip(String shipName, Coordinate start, ShipOrientation orientation);
    public CellStatus shootAt(Coordinate coord);
    public boolean hasPlacedAllShips();
    public boolean hasLost();
}
```

**Diagramme de classe :**

```mermaid
classDiagram
    class Player {
        -String name
        -Fleet fleet
        -Grid grid
        +Player(String name, int gridSize, Map shipConfig)
        +getName() String
        +getFleet() Fleet
        +getGrid() Grid
        +placeShip(String shipName, Coordinate start, ShipOrientation orientation) boolean
        +shootAt(Coordinate coord) CellStatus
        +hasPlacedAllShips() boolean
        +hasLost() boolean
    }
    
    Player --> Fleet
    Player --> Grid
```

### 8. GameState (Game)

États possibles de la partie.

```java
public enum GameState {
    CONFIG,      // Configuration (taille grille, navires)
    PLACEMENT,   // Placement des navires par les joueurs
    PLAYING,     // Partie en cours (tirs)
    FINISHED     // Partie terminée
}
```

### 9. GameTurn (Game)

Gestion des tours de jeu.

```java
public enum GameTurn {
    PLAYER1,     // Tour du joueur 1
    PLAYER2      // Tour du joueur 2
}
```

### 10. GameResult (Game)

Résultat d'une action (tir, placement).

```java
public class GameResult {
    private final boolean success;
    private final String message;
    private final CellStatus cellStatus;  // Pour les tirs
    private final Ship sunkShip;         // Si un navire est coulé
    
    // Constructeurs statiques
    public static GameResult success(String message);
    public static GameResult error(String message);
    public static GameResult hit(Ship ship);
    public static GameResult miss();
    public static GameResult sunk(Ship ship);
}
```

### 11. Game (Game)

Classe principale gérant toute la logique de la partie.

```java
public class Game {
    private final Player player1;
    private final Player player2;
    private GameState state;
    private GameTurn currentTurn;
    private Player winner;
    
    // Méthodes principales
    public GameResult placeShip(String playerName, String shipName, 
                                Coordinate start, ShipOrientation orientation);
    public GameResult shoot(String playerName, Coordinate target);
    public boolean canStart();
    public void start();
    public boolean isFinished();
    public Player getWinner();
    public GameView getView(String playerName); // Pour affichage console
}
```

**Diagramme de classe complet :**

```mermaid
classDiagram
    class Game {
        -Player player1
        -Player player2
        -GameState state
        -GameTurn currentTurn
        -Player winner
        +Game(String player1Name, String player2Name, int gridSize, Map shipConfig)
        +placeShip(String playerName, String shipName, Coordinate start, ShipOrientation orientation) GameResult
        +shoot(String playerName, Coordinate target) GameResult
        +canStart() boolean
        +start() void
        +isFinished() boolean
        +getWinner() Player
        +getState() GameState
        +getCurrentTurn() GameTurn
        +getView(String playerName) GameView
    }
    
    class Player {
        -String name
        -Fleet fleet
        -Grid grid
        +placeShip(String shipName, Coordinate start, ShipOrientation orientation) boolean
        +shootAt(Coordinate coord) CellStatus
        +hasPlacedAllShips() boolean
        +hasLost() boolean
    }
    
    class Grid {
        -int size
        -CellStatus[][] cells
        -Map shipMap
        +placeShip(Ship ship, Coordinate start, ShipOrientation orientation) boolean
        +shoot(Coordinate coord) CellStatus
        +allShipsSunk() boolean
    }
    
    class Fleet {
        -List ships
        +addShip(Ship ship) boolean
        +isComplete() boolean
    }
    
    class Ship {
        -String name
        -int size
        -Coordinate startPosition
        -ShipOrientation orientation
        +getOccupiedCells() Set
        +isSunk() boolean
        +hit(Coordinate coord) boolean
    }
    
    class Coordinate {
        -int x
        -int y
        +isValid(int gridSize) boolean
    }
    
    class GameResult {
        -boolean success
        -String message
        -CellStatus cellStatus
        -Ship sunkShip
        +success(String message) GameResult
        +error(String message) GameResult
        +hit(Ship ship) GameResult
        +miss() GameResult
    }
    
    Game --> Player
    Game --> GameState
    Game --> GameTurn
    Game --> GameResult
    Player --> Fleet
    Player --> Grid
    Fleet --> Ship
    Grid --> Ship
    Grid --> Coordinate
    Grid --> CellStatus
    Ship --> Coordinate
    Ship --> ShipOrientation
    GameResult --> Ship
    GameResult --> CellStatus
```

## 🔄 Flux de jeu

### Diagramme de séquence - Placement d'un navire

```mermaid
sequenceDiagram
    participant C as "Console/Client"
    participant G as Game
    participant P as Player
    participant F as Fleet
    participant GR as Grid
    participant S as Ship
    
    C->>G: placeShip("Joueur1", "Porte-avions", (0,0), HORIZONTAL)
    G->>G: Vérifier état = PLACEMENT
    G->>P: getPlayer("Joueur1")
    P->>F: getShipByName("Porte-avions")
    F-->>P: Ship
    P->>GR: isValidPlacement(ship, (0,0), HORIZONTAL)
    GR-->>P: true
    P->>GR: placeShip(ship, (0,0), HORIZONTAL)
    GR-->>P: true
    P->>F: markShipPlaced(ship)
    P-->>G: true
    G->>G: Vérifier si tous navires placés
    G-->>C: GameResult.success("Navire placé")
```

### Diagramme de séquence - Tir

```mermaid
sequenceDiagram
    participant C as "Console/Client"
    participant G as Game
    participant P1 as "Player1 (Tireur)"
    participant P2 as "Player2 (Cible)"
    participant GR2 as "Grid Player2"
    participant S as Ship
    
    C->>G: shoot("Joueur1", (5,5))
    G->>G: Vérifier état = PLAYING
    G->>G: Vérifier tour = PLAYER1
    G->>P2: getGrid()
    P2-->>G: Grid
    G->>GR2: shoot((5,5))
    alt Case vide
        GR2-->>G: MISS
        G-->>C: GameResult.miss()
    else Case navire
        GR2->>S: hit((5,5))
        S-->>GR2: true
        alt Navire coulé
            GR2-->>G: SUNK + Ship
            G->>G: Vérifier si tous navires coulés
            G-->>C: GameResult.sunk(ship)
        else Navire touché
            GR2-->>G: HIT + Ship
            G-->>C: GameResult.hit(ship)
        end
    end
    G->>G: Changer de tour
```

### Machine à états du jeu

```mermaid
stateDiagram-v2
    [*] --> CONFIG: Créer Game
    CONFIG --> PLACEMENT: Config terminée
    
    state PLACEMENT {
        [*] --> PlacementJ1: Joueur 1 place
        PlacementJ1 --> PlacementJ2: Tous navires J1 placés
        PlacementJ2 --> PlacementJ1: Tous navires J2 placés
        PlacementJ1 --> [*]: Tous navires placés
    }
    
    PLACEMENT --> PLAYING: start()
    
    state PLAYING {
        [*] --> TourJ1: Tour Joueur 1
        TourJ1 --> TourJ2: Tir effectué
        TourJ2 --> TourJ1: Tir effectué
        TourJ1 --> [*]: Victoire J1
        TourJ2 --> [*]: Victoire J2
    }
    
    PLAYING --> FINISHED: Tous navires coulés
    FINISHED --> [*]
```

## 🖥️ Utilisation console

### Interface console pour debugging

Le modèle peut être utilisé directement via console pour tester la logique sans interface graphique.

### Exemple d'utilisation

```java
// Création d'une partie
Map<String, Integer> shipConfig = new HashMap<>();
shipConfig.put("Porte-avions", 1);  // 5 cases
shipConfig.put("Croiseur", 2);      // 4 cases
shipConfig.put("Destroyer", 3);     // 3 cases
shipConfig.put("Sous-marin", 4);    // 2 cases

Game game = new Game("Joueur1", "Joueur2", 10, shipConfig);

// Placement des navires - Joueur 1
game.placeShip("Joueur1", "Porte-avions", new Coordinate(0, 0), ShipOrientation.HORIZONTAL);
game.placeShip("Joueur1", "Croiseur", new Coordinate(0, 2), ShipOrientation.HORIZONTAL);
// ... autres navires

// Placement des navires - Joueur 2
game.placeShip("Joueur2", "Porte-avions", new Coordinate(5, 5), ShipOrientation.VERTICAL);
// ... autres navires

// Démarrer la partie
if (game.canStart()) {
    game.start();
}

// Jouer
GameResult result = game.shoot("Joueur1", new Coordinate(5, 5));
System.out.println(result.getMessage()); // "TOUCHÉ ! Porte-avions"

// Afficher l'état
GameView view = game.getView("Joueur1");
view.printToConsole();
```

### Classe ConsoleGame (Helper)

Pour faciliter l'utilisation en console, une classe helper peut être créée :

```java
public class ConsoleGame {
    private final Game game;
    private final Scanner scanner;
    
    public ConsoleGame(String player1Name, String player2Name) {
        // Initialisation avec configuration par défaut
        Map<String, Integer> config = getDefaultShipConfig();
        this.game = new Game(player1Name, player2Name, 10, config);
        this.scanner = new Scanner(System.in);
    }
    
    public void run() {
        // Phase de placement
        placementPhase();
        
        // Phase de jeu
        playingPhase();
    }
    
    private void placementPhase() {
        // Logique de placement interactif
    }
    
    private void playingPhase() {
        // Logique de jeu interactif
    }
    
    public void displayGrid(String playerName) {
        GameView view = game.getView(playerName);
        view.printToConsole();
    }
}
```

### Diagramme d'interaction console

```mermaid
sequenceDiagram
    participant U as Utilisateur
    participant CG as ConsoleGame
    participant G as Game
    participant V as GameView
    
    U->>CG: Créer partie
    CG->>G: new Game(...)
    
    loop "Placement navires"
        U->>CG: Commande placement
        CG->>G: placeShip(...)
        G-->>CG: GameResult
        CG->>V: getView(playerName)
        V-->>CG: GameView
        CG->>U: Afficher grille
    end
    
    U->>CG: Démarrer partie
    CG->>G: start()
    
    loop Tirs
        U->>CG: Coordonnée tir
        CG->>G: shoot(playerName, coord)
        G-->>CG: GameResult
        CG->>V: getView(playerName)
        V-->>CG: GameView
        CG->>U: Afficher résultat + grille
    end
    
    G->>CG: isFinished() = true
    CG->>U: Afficher vainqueur
```

## 💻 Exemples de code

### Exemple 1 : Création et configuration

```java
// Configuration des navires
Map<String, Integer> shipConfig = new HashMap<>();
shipConfig.put("Porte-avions", 1);   // 1 navire de 5 cases
shipConfig.put("Croiseur", 2);       // 2 navires de 4 cases
shipConfig.put("Destroyer", 3);      // 3 navires de 3 cases
shipConfig.put("Sous-marin", 4);     // 4 navires de 2 cases

// Création de la partie
Game game = new Game("Alice", "Bob", 10, shipConfig);
```

### Exemple 2 : Placement d'un navire

```java
// Placement horizontal d'un porte-avions à la position (0, 0)
GameResult result = game.placeShip(
    "Alice",
    "Porte-avions",
    new Coordinate(0, 0),
    ShipOrientation.HORIZONTAL
);

if (result.isSuccess()) {
    System.out.println("Navire placé avec succès !");
} else {
    System.out.println("Erreur : " + result.getMessage());
}
```

### Exemple 3 : Tir sur une case

```java
// Tir du joueur actif sur la case (5, 5)
GameResult result = game.shoot("Alice", new Coordinate(5, 5));

switch (result.getCellStatus()) {
    case HIT:
        System.out.println("TOUCHÉ ! " + result.getSunkShip().getName());
        break;
    case MISS:
        System.out.println("À l'eau !");
        break;
    case SUNK:
        System.out.println("COULÉ ! " + result.getSunkShip().getName());
        break;
}
```

### Exemple 4 : Affichage de la grille

```java
// Obtenir la vue de la grille pour un joueur
GameView view = game.getView("Alice");

// Afficher sa propre grille (avec navires)
view.printOwnGrid();

// Afficher la grille adverse (sans navires, seulement tirs)
view.printOpponentGrid();
```

### Exemple 5 : Vérification de l'état

```java
// Vérifier si la partie peut démarrer
if (game.canStart()) {
    game.start();
    System.out.println("Partie démarrée !");
}

// Vérifier si la partie est terminée
if (game.isFinished()) {
    Player winner = game.getWinner();
    System.out.println("Vainqueur : " + winner.getName());
}
```

## 🧪 Tests unitaires

Le modèle doit être testé avec JUnit pour garantir la validité de la logique métier.

### Exemple de test

```java
@Test
public void testPlaceShip() {
    Game game = new Game("J1", "J2", 10, getDefaultConfig());
    
    GameResult result = game.placeShip(
        "J1", "Porte-avions", 
        new Coordinate(0, 0), 
        ShipOrientation.HORIZONTAL
    );
    
    assertTrue(result.isSuccess());
    assertFalse(game.canStart()); // Pas encore tous placés
}

@Test
public void testShoot() {
    Game game = setupGameWithShips();
    game.start();
    
    GameResult result = game.shoot("J1", new Coordinate(0, 0));
    
    assertEquals(CellStatus.HIT, result.getCellStatus());
}
```

## 📝 Résumé

Le modèle Java pur est **autonome** et contient toute la logique métier :

- ✅ **Core** : Types de base (Coordinate, CellStatus, ShipOrientation)
- ✅ **Entities** : Entités métier (Ship, Grid, Fleet, Player)
- ✅ **Game** : Logique de partie (Game, GameState, GameTurn, GameResult)
- ✅ **Console** : Utilisable directement pour debugging
- ✅ **Tests** : Testable unitairement sans dépendances externes

Ce modèle peut ensuite être utilisé par :
- Spring Boot (via services/controllers)
- Tauri (via JAR embarqué)
- Tests unitaires
- Console de debugging

---

**Note** : Ce modèle est la **Phase 1** du projet. Il doit être complètement fonctionnel et testé avant d'ajouter Spring Boot (Phase 2).

