package domain.model.game;

import domain.model.core.CellStatus;
import domain.model.core.Coordinate;
import domain.model.entities.Grid;
import domain.model.entities.Player;

/**
 * Vue de la partie pour un joueur (affichage console).
 */
public class GameView {
    private final Player player;
    private final Player opponent;
    private final Game game;
    
    public GameView(Player player, Player opponent, Game game) {
        this.player = player;
        this.opponent = opponent;
        this.game = game;
    }
    
    /**
     * Affiche la grille du joueur (avec navires).
     */
    public void printOwnGrid() {
        System.out.println("\n=== Votre grille ===");
        printGrid(player.getGrid(), true);
    }
    
    /**
     * Affiche la grille adverse (sans navires, seulement tirs).
     */
    public void printOpponentGrid() {
        System.out.println("\n=== Grille adverse ===");
        printGrid(opponent.getGrid(), false);
    }
    
    /**
     * Affiche les deux grilles côte à côte.
     */
    public void printToConsole() {
        printOwnGrid();
        printOpponentGrid();
        System.out.println("\nÉtat : " + game.getState());
        System.out.println("Tour actuel : " + game.getCurrentTurn());
        if (game.isFinished() && game.getWinner() != null) {
            System.out.println("Vainqueur : " + game.getWinner().getName());
        }
    }
    
    /**
     * Affiche une grille.
     */
    private void printGrid(Grid grid, boolean revealShips) {
        int size = grid.getSize();
        
        // En-tête avec numéros de colonnes
        System.out.print("   ");
        for (int j = 0; j < size; j++) {
            System.out.print(String.format("%2d ", j));
        }
        System.out.println();
        
        // Lignes avec numéros de lignes
        for (int i = 0; i < size; i++) {
            System.out.print(String.format("%2d ", i));
            for (int j = 0; j < size; j++) {
                Coordinate coord = new Coordinate(i, j);
                CellStatus status = grid.getCellStatus(coord);
                
                char symbol = getSymbol(status, revealShips);
                System.out.print(" " + symbol + " ");
            }
            System.out.println();
        }
    }
    
    /**
     * Retourne le symbole à afficher pour un statut de cellule.
     */
    private char getSymbol(CellStatus status, boolean revealShips) {
        switch (status) {
            case EMPTY:
                return revealShips ? '.' : '?';
            case SHIP:
                return revealShips ? 'S' : '?';
            case HIT:
                return 'X';
            case MISS:
                return 'O';
            case SUNK:
                return '#';
            default:
                return '?';
        }
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public Player getOpponent() {
        return opponent;
    }
}
