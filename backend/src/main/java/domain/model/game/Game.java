package domain.model.game;

import domain.model.core.Coordinate;
import domain.model.core.ShipOrientation;
import domain.model.entities.Player;

import java.util.Map;
import java.util.Objects;

/**
 * Classe principale gérant toute la logique de la partie.
 */
public class Game {
    private final Player player1;
    private final Player player2;
    private GameState state;
    private GameTurn currentTurn;
    private Player winner;
    
    public Game(String player1Name, String player2Name, int gridSize, Map<String, Integer> shipConfig) {
        this.player1 = new Player(player1Name, gridSize, shipConfig);
        this.player2 = new Player(player2Name, gridSize, shipConfig);
        this.state = GameState.CONFIG;
        this.currentTurn = GameTurn.PLAYER1;
        this.winner = null;
    }
    
    /**
     * Place un navire pour un joueur.
     */
    public GameResult placeShip(String playerName, String shipName, 
                                Coordinate start, ShipOrientation orientation) {
        // Vérifier l'état
        if (state != GameState.CONFIG && state != GameState.PLACEMENT) {
            return GameResult.error("Impossible de placer un navire dans l'état actuel");
        }
        
        // Passer à l'état PLACEMENT si on est en CONFIG
        if (state == GameState.CONFIG) {
            state = GameState.PLACEMENT;
        }
        
        Player player = getPlayer(playerName);
        if (player == null) {
            return GameResult.error("Joueur introuvable : " + playerName);
        }
        
        // Vérifier que le joueur n'a pas déjà placé tous ses navires
        if (player.hasPlacedAllShips()) {
            return GameResult.error("Tous les navires sont déjà placés pour " + playerName);
        }
        
        // Placer le navire
        boolean success = player.placeShip(shipName, start, orientation);
        
        if (success) {
            return GameResult.success("Navire " + shipName + " placé avec succès");
        } else {
            return GameResult.error("Impossible de placer le navire " + shipName + " à cette position");
        }
    }
    
    /**
     * Effectue un tir.
     */
    public GameResult shoot(String playerName, Coordinate target) {
        // Vérifier l'état
        if (state != GameState.PLAYING) {
            return GameResult.error("La partie n'est pas en cours");
        }
        
        Player shooter = getPlayer(playerName);
        Player targetPlayer = getOpponent(playerName);
        
        if (shooter == null || targetPlayer == null) {
            return GameResult.error("Joueur introuvable");
        }
        
        // Vérifier que c'est le tour du bon joueur
        if (!isCurrentPlayer(playerName)) {
            return GameResult.error("Ce n'est pas votre tour");
        }
        
        // Effectuer le tir
        domain.model.core.CellStatus result = targetPlayer.shootAt(target);
        
        // Créer le GameResult approprié
        GameResult gameResult;
        switch (result) {
            case HIT:
                domain.model.entities.Ship hitShip = targetPlayer.getGrid().getShipAt(target);
                gameResult = GameResult.hit(hitShip);
                break;
            case MISS:
                gameResult = GameResult.miss();
                break;
            case SUNK:
                domain.model.entities.Ship sunkShip = targetPlayer.getGrid().getShipAt(target);
                gameResult = GameResult.sunk(sunkShip);
                // Vérifier si la partie est terminée
                if (targetPlayer.hasLost()) {
                    state = GameState.FINISHED;
                    winner = shooter;
                }
                break;
            default:
                return GameResult.error("Tir invalide");
        }
        
        // Changer de tour seulement si ce n'est pas un coup qui coule un navire
        // (dans certaines variantes, on peut rejouer après un coup réussi)
        // Ici, on change toujours de tour après un tir
        switchTurn();
        
        return gameResult;
    }
    
    /**
     * Vérifie si la partie peut démarrer (tous les navires placés).
     */
    public boolean canStart() {
        return state == GameState.PLACEMENT 
            && player1.hasPlacedAllShips() 
            && player2.hasPlacedAllShips();
    }
    
    /**
     * Démarre la partie.
     */
    public void start() {
        if (!canStart()) {
            throw new IllegalStateException("Impossible de démarrer la partie : tous les navires ne sont pas placés");
        }
        state = GameState.PLAYING;
        currentTurn = GameTurn.PLAYER1;
    }
    
    /**
     * Vérifie si la partie est terminée.
     */
    public boolean isFinished() {
        return state == GameState.FINISHED;
    }
    
    /**
     * Retourne le vainqueur, ou null si la partie n'est pas terminée.
     */
    public Player getWinner() {
        return winner;
    }
    
    public GameState getState() {
        return state;
    }
    
    public GameTurn getCurrentTurn() {
        return currentTurn;
    }
    
    public Player getPlayer1() {
        return player1;
    }
    
    public Player getPlayer2() {
        return player2;
    }
    
    /**
     * Retourne un joueur par son nom.
     */
    public Player getPlayer(String playerName) {
        if (Objects.equals(player1.getName(), playerName)) {
            return player1;
        } else if (Objects.equals(player2.getName(), playerName)) {
            return player2;
        }
        return null;
    }
    
    /**
     * Retourne l'adversaire d'un joueur.
     */
    public Player getOpponent(String playerName) {
        if (Objects.equals(player1.getName(), playerName)) {
            return player2;
        } else if (Objects.equals(player2.getName(), playerName)) {
            return player1;
        }
        return null;
    }
    
    /**
     * Vérifie si c'est le tour du joueur donné.
     */
    private boolean isCurrentPlayer(String playerName) {
        if (currentTurn == GameTurn.PLAYER1) {
            return Objects.equals(player1.getName(), playerName);
        } else {
            return Objects.equals(player2.getName(), playerName);
        }
    }
    
    /**
     * Change de tour.
     */
    private void switchTurn() {
        currentTurn = (currentTurn == GameTurn.PLAYER1) ? GameTurn.PLAYER2 : GameTurn.PLAYER1;
    }
    
    /**
     * Retourne une vue de la partie pour un joueur donné.
     */
    public GameView getView(String playerName) {
        Player player = getPlayer(playerName);
        Player opponent = getOpponent(playerName);
        
        if (player == null || opponent == null) {
            throw new IllegalArgumentException("Joueur introuvable : " + playerName);
        }
        
        return new GameView(player, opponent, this);
    }
}
