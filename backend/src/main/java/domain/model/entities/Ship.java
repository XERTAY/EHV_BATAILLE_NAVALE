package domain.model.entities;

import domain.model.core.Coordinate;
import domain.model.core.ShipOrientation;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Représente un navire avec sa taille, position et orientation.
 */
public class Ship {
    private final String name;           // "Porte-avions", "Croiseur", etc.
    private final int size;              // Taille en cases (2-5)
    private Coordinate startPosition;    // Position de départ
    private ShipOrientation orientation; // Orientation
    private Set<Coordinate> hits;        // Cases touchées
    private boolean placed;              // Indique si le navire est placé
    
    public Ship(String name, int size) {
        this.name = Objects.requireNonNull(name);
        if (size < 2 || size > 5) {
            throw new IllegalArgumentException("La taille du navire doit être entre 2 et 5");
        }
        this.size = size;
        this.hits = new HashSet<>();
        this.placed = false;
    }
    
    public String getName() {
        return name;
    }
    
    public int getSize() {
        return size;
    }
    
    public Coordinate getStartPosition() {
        return startPosition;
    }
    
    public ShipOrientation getOrientation() {
        return orientation;
    }
    
    public Set<Coordinate> getHits() {
        return new HashSet<>(hits);
    }
    
    public boolean isPlaced() {
        return placed;
    }
    
    /**
     * Place le navire à une position donnée avec une orientation.
     */
    public void place(Coordinate startPosition, ShipOrientation orientation) {
        this.startPosition = startPosition;
        this.orientation = orientation;
        this.placed = true;
    }
    
    /**
     * Retourne l'ensemble des cellules occupées par ce navire.
     */
    public Set<Coordinate> getOccupiedCells() {
        Set<Coordinate> cells = new HashSet<>();
        if (startPosition == null || orientation == null) {
            return cells;
        }
        
        int dx = orientation == ShipOrientation.HORIZONTAL ? 1 : 0;
        int dy = orientation == ShipOrientation.VERTICAL ? 1 : 0;
        
        for (int i = 0; i < size; i++) {
            cells.add(startPosition.add(i * dx, i * dy));
        }
        
        return cells;
    }
    
    /**
     * Vérifie si le navire contient la coordonnée donnée.
     */
    public boolean contains(Coordinate coord) {
        return getOccupiedCells().contains(coord);
    }
    
    /**
     * Enregistre un tir sur ce navire.
     * @return true si le navire est maintenant coulé, false sinon
     */
    public boolean hit(Coordinate coord) {
        if (contains(coord)) {
            hits.add(coord);
            return isSunk();
        }
        return false;
    }
    
    /**
     * Vérifie si le navire est coulé (toutes les cases touchées).
     */
    public boolean isSunk() {
        return placed && hits.size() == size;
    }
}
