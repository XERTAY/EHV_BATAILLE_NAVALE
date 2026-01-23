package domain.model.entities;

import domain.model.core.CellStatus;
import domain.model.core.Coordinate;
import domain.model.core.ShipOrientation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Grille de jeu d'un joueur (placement des navires et tirs adverses).
 */
public class Grid {
    private final int size;                    // Taille de la grille (10 par défaut)
    private final CellStatus[][] cells;        // État de chaque cellule
    private final Map<Coordinate, Ship> shipMap; // Carte navire par position
    
    public Grid(int size) {
        if (size < 5 || size > 20) {
            throw new IllegalArgumentException("La taille de la grille doit être entre 5 et 20");
        }
        this.size = size;
        this.cells = new CellStatus[size][size];
        this.shipMap = new HashMap<>();
        
        // Initialiser toutes les cellules à EMPTY
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                cells[i][j] = CellStatus.EMPTY;
            }
        }
    }
    
    public int getSize() {
        return size;
    }
    
    public CellStatus getCellStatus(Coordinate coord) {
        if (!coord.isValid(size)) {
            throw new IllegalArgumentException("Coordonnée invalide : " + coord);
        }
        return cells[coord.getX()][coord.getY()];
    }
    
    /**
     * Vérifie si le placement d'un navire est valide.
     */
    public boolean isValidPlacement(Ship ship, Coordinate start, ShipOrientation orientation) {
        // Vérifier que la position de départ est valide
        if (!start.isValid(size)) {
            return false;
        }
        
        // Calculer toutes les cellules que le navire occuperait
        int dx = orientation == ShipOrientation.HORIZONTAL ? 1 : 0;
        int dy = orientation == ShipOrientation.VERTICAL ? 1 : 0;
        
        for (int i = 0; i < ship.getSize(); i++) {
            Coordinate cell = start.add(i * dx, i * dy);
            
            // Vérifier que la cellule est dans la grille
            if (!cell.isValid(size)) {
                return false;
            }
            
            // Vérifier que la cellule n'est pas déjà occupée
            if (cells[cell.getX()][cell.getY()] != CellStatus.EMPTY) {
                return false;
            }
            
            // Vérifier qu'il n'y a pas de navire adjacent (règle : pas de navires adjacents)
            for (int adjX = -1; adjX <= 1; adjX++) {
                for (int adjY = -1; adjY <= 1; adjY++) {
                    if (adjX == 0 && adjY == 0) continue;
                    Coordinate adj = cell.add(adjX, adjY);
                    if (adj.isValid(size) && shipMap.containsKey(adj)) {
                        return false;
                    }
                }
            }
        }
        
        return true;
    }
    
    /**
     * Place un navire sur la grille.
     * @return true si le placement a réussi, false sinon
     */
    public boolean placeShip(Ship ship, Coordinate start, ShipOrientation orientation) {
        if (!isValidPlacement(ship, start, orientation)) {
            return false;
        }
        
        // Placer le navire
        ship.place(start, orientation);
        Set<Coordinate> occupiedCells = ship.getOccupiedCells();
        
        // Marquer les cellules comme SHIP et enregistrer dans shipMap
        for (Coordinate cell : occupiedCells) {
            cells[cell.getX()][cell.getY()] = CellStatus.SHIP;
            shipMap.put(cell, ship);
        }
        
        return true;
    }
    
    /**
     * Effectue un tir sur la grille.
     * @return Le statut de la cellule après le tir
     */
    public CellStatus shoot(Coordinate coord) {
        if (!coord.isValid(size)) {
            throw new IllegalArgumentException("Coordonnée invalide : " + coord);
        }
        
        CellStatus currentStatus = cells[coord.getX()][coord.getY()];
        
        // Si déjà touché, retourner le statut actuel
        if (currentStatus == CellStatus.HIT || currentStatus == CellStatus.MISS || currentStatus == CellStatus.SUNK) {
            return currentStatus;
        }
        
        // Vérifier si un navire est présent
        Ship ship = shipMap.get(coord);
        
        if (ship != null) {
            // Toucher le navire
            boolean sunk = ship.hit(coord);
            
            if (sunk) {
                // Marquer toutes les cellules du navire comme SUNK
                Set<Coordinate> occupiedCells = ship.getOccupiedCells();
                for (Coordinate cell : occupiedCells) {
                    cells[cell.getX()][cell.getY()] = CellStatus.SUNK;
                }
                return CellStatus.SUNK;
            } else {
                cells[coord.getX()][coord.getY()] = CellStatus.HIT;
                return CellStatus.HIT;
            }
        } else {
            cells[coord.getX()][coord.getY()] = CellStatus.MISS;
            return CellStatus.MISS;
        }
    }
    
    /**
     * Vérifie si tous les navires sont coulés.
     */
    public boolean allShipsSunk() {
        for (Ship ship : shipMap.values()) {
            // Utiliser un Set pour éviter les doublons
            if (!ship.isSunk()) {
                return false;
            }
        }
        return !shipMap.isEmpty();
    }
    
    /**
     * Retourne le navire à une position donnée, ou null si aucun navire.
     */
    public Ship getShipAt(Coordinate coord) {
        return shipMap.get(coord);
    }
}
