package domain.model.entities;

import java.util.*;
import java.util.stream.Collectors;
import java.util.Objects;

/**
 * Flotte complète d'un joueur (ensemble de navires).
 */
public class Fleet {
    private final List<Ship> ships;           // Liste des navires
    private final Map<String, Integer> shipConfig; // Configuration (nom -> quantité)
    private final Map<String, Integer> addedCount; // Compteur de navires ajoutés par type
    
    public Fleet(Map<String, Integer> shipConfig) {
        this.shipConfig = new HashMap<>(Objects.requireNonNull(shipConfig));
        this.ships = new ArrayList<>();
        this.addedCount = new HashMap<>();
        
        // Initialiser les compteurs
        for (String shipName : shipConfig.keySet()) {
            addedCount.put(shipName, 0);
        }
    }
    
    /**
     * Ajoute un navire à la flotte.
     * @return true si le navire a été ajouté, false si la limite est atteinte
     */
    public boolean addShip(Ship ship) {
        String shipName = ship.getName();
        int currentCount = addedCount.getOrDefault(shipName, 0);
        int maxCount = shipConfig.getOrDefault(shipName, 0);
        
        if (currentCount >= maxCount) {
            return false;
        }
        
        ships.add(ship);
        addedCount.put(shipName, currentCount + 1);
        return true;
    }
    
    /**
     * Vérifie si la flotte est complète (tous les navires requis sont présents).
     */
    public boolean isComplete() {
        for (Map.Entry<String, Integer> entry : shipConfig.entrySet()) {
            String shipName = entry.getKey();
            int required = entry.getValue();
            int current = addedCount.getOrDefault(shipName, 0);
            if (current < required) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Vérifie si tous les navires sont placés sur la grille.
     */
    public boolean allShipsPlaced() {
        return ships.stream().allMatch(Ship::isPlaced);
    }
    
    /**
     * Retourne la liste de tous les navires.
     */
    public List<Ship> getShips() {
        return new ArrayList<>(ships);
    }
    
    /**
     * Retourne la liste des navires coulés.
     */
    public List<Ship> getSunkShips() {
        return ships.stream()
                .filter(Ship::isSunk)
                .collect(Collectors.toList());
    }
    
    /**
     * Retourne la liste des navires actifs (non coulés).
     */
    public List<Ship> getActiveShips() {
        return ships.stream()
                .filter(ship -> !ship.isSunk())
                .collect(Collectors.toList());
    }
    
    /**
     * Retourne la configuration des navires.
     */
    public Map<String, Integer> getShipConfig() {
        return new HashMap<>(shipConfig);
    }
    
    /**
     * Retourne un navire non placé par son nom, ou null si tous sont placés.
     */
    public Ship getUnplacedShipByName(String shipName) {
        return ships.stream()
                .filter(ship -> ship.getName().equals(shipName) && !ship.isPlaced())
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Retourne tous les navires non placés.
     */
    public List<Ship> getUnplacedShips() {
        return ships.stream()
                .filter(ship -> !ship.isPlaced())
                .collect(Collectors.toList());
    }
}
