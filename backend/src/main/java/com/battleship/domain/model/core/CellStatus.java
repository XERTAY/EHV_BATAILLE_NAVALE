package com.battleship.domain.model.core;

/**
 * Énumération des états possibles d'une cellule.
 */
public enum CellStatus {
    EMPTY,    // Case vide
    SHIP,     // Case occupée par un navire (non touchée)
    HIT,      // Case touchée (navire)
    MISS,     // Case touchée (vide)
    SUNK      // Case d'un navire coulé
}

