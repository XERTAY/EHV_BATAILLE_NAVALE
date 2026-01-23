package com.battleship.domain.model.core;

import java.util.Objects;

/**
 * Représente une position sur la grille.
 */
public class Coordinate {
    private final int x;
    private final int y;

    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    /**
     * Vérifie si la coordonnée est valide pour une grille de taille donnée.
     */
    public boolean isValid(int gridSize) {
        return x >= 0 && x < gridSize && y >= 0 && y < gridSize;
    }

    /**
     * Crée une nouvelle coordonnée en ajoutant des décalages.
     */
    public Coordinate add(int dx, int dy) {
        return new Coordinate(x + dx, y + dy);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinate that = (Coordinate) o;
        return x == that.x && y == that.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}

