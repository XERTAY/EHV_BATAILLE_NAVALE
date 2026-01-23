package domain.model.entities;

import domain.model.core.CellStatus;
import domain.model.core.Coordinate;
import domain.model.core.ShipOrientation;

import java.util.Map;

/**
 * Joueur avec sa flotte et sa grille.
 */
public class Player {
    private final String name;
    private final Fleet fleet;
    private final Grid grid;
    
    public Player(String name, int gridSize, Map<String, Integer> shipConfig) {
        this.name = name;
        this.grid = new Grid(gridSize);
        this.fleet = new Fleet(shipConfig);
        
        // Créer les navires selon la configuration
        initializeFleet(shipConfig);
    }
    
    private void initializeFleet(Map<String, Integer> shipConfig) {
        // Mapping des noms de navires vers leur taille
        Map<String, Integer> shipSizes = Map.of(
            "Porte-avions", 5,
            "Croiseur", 4,
            "Destroyer", 3,
            "Sous-marin", 2
        );
        
        for (Map.Entry<String, Integer> entry : shipConfig.entrySet()) {
            String shipName = entry.getKey();
            int quantity = entry.getValue();
            int size = shipSizes.getOrDefault(shipName, 2);
            
            for (int i = 0; i < quantity; i++) {
                Ship ship = new Ship(shipName, size);
                fleet.addShip(ship);
            }
        }
    }
    
    public String getName() {
        return name;
    }
    
    public Fleet getFleet() {
        return fleet;
    }
    
    public Grid getGrid() {
        return grid;
    }
    
    /**
     * Place un navire sur la grille du joueur.
     * @return true si le placement a réussi, false sinon
     */
    public boolean placeShip(String shipName, Coordinate start, ShipOrientation orientation) {
        Ship ship = fleet.getUnplacedShipByName(shipName);
        if (ship == null) {
            return false;
        }
        
        if (grid.isValidPlacement(ship, start, orientation)) {
            return grid.placeShip(ship, start, orientation);
        }
        
        return false;
    }
    
    /**
     * Effectue un tir sur la grille du joueur (appelé par l'adversaire).
     * @return Le statut de la cellule après le tir
     */
    public CellStatus shootAt(Coordinate coord) {
        return grid.shoot(coord);
    }
    
    /**
     * Vérifie si tous les navires sont placés.
     */
    public boolean hasPlacedAllShips() {
        return fleet.allShipsPlaced();
    }
    
    /**
     * Vérifie si le joueur a perdu (tous ses navires sont coulés).
     */
    public boolean hasLost() {
        return grid.allShipsSunk();
    }
}
