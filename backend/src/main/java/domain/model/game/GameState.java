package domain.model.game;

/**
 * États possibles de la partie.
 */
public enum GameState {
    CONFIG,      // Configuration (taille grille, navires)
    PLACEMENT,   // Placement des navires par les joueurs
    PLAYING,     // Partie en cours (tirs)
    FINISHED     // Partie terminée
}
