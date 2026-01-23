package domain.model.game;

import domain.model.core.CellStatus;
import domain.model.entities.Ship;

/**
 * Résultat d'une action (tir, placement).
 */
public class GameResult {
    private final boolean success;
    private final String message;
    private final CellStatus cellStatus;  // Pour les tirs
    private final Ship sunkShip;         // Si un navire est coulé
    
    private GameResult(boolean success, String message, CellStatus cellStatus, Ship sunkShip) {
        this.success = success;
        this.message = message;
        this.cellStatus = cellStatus;
        this.sunkShip = sunkShip;
    }
    
    public static GameResult success(String message) {
        return new GameResult(true, message, null, null);
    }
    
    public static GameResult error(String message) {
        return new GameResult(false, message, null, null);
    }
    
    public static GameResult hit(Ship ship) {
        return new GameResult(true, "TOUCHÉ ! " + ship.getName(), CellStatus.HIT, null);
    }
    
    public static GameResult miss() {
        return new GameResult(true, "À l'eau !", CellStatus.MISS, null);
    }
    
    public static GameResult sunk(Ship ship) {
        return new GameResult(true, "COULÉ ! " + ship.getName(), CellStatus.SUNK, ship);
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public CellStatus getCellStatus() {
        return cellStatus;
    }
    
    public Ship getSunkShip() {
        return sunkShip;
    }
}
