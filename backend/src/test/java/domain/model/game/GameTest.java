package domain.model.game;

import domain.model.core.Coordinate;
import domain.model.core.ShipOrientation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class GameTest {
    private Game game;
    private Map<String, Integer> shipConfig;
    
    @BeforeEach
    void setUp() {
        shipConfig = new HashMap<>();
        shipConfig.put("Porte-avions", 1);
        shipConfig.put("Croiseur", 1);
        shipConfig.put("Destroyer", 1);
        shipConfig.put("Sous-marin", 1);
        
        game = new Game("Joueur1", "Joueur2", 10, shipConfig);
    }
    
    @Test
    void testGameCreation() {
        assertEquals(GameState.CONFIG, game.getState());
        assertEquals(GameTurn.PLAYER1, game.getCurrentTurn());
        assertFalse(game.canStart());
        assertFalse(game.isFinished());
    }
    
    @Test
    void testPlaceShip() {
        GameResult result = game.placeShip(
            "Joueur1", 
            "Porte-avions", 
            new Coordinate(0, 0), 
            ShipOrientation.HORIZONTAL
        );
        
        assertTrue(result.isSuccess());
        assertEquals(GameState.PLACEMENT, game.getState());
    }
    
    @Test
    void testPlaceShipInvalid() {
        // Placer un navire hors limites
        GameResult result = game.placeShip(
            "Joueur1", 
            "Porte-avions", 
            new Coordinate(8, 0), 
            ShipOrientation.HORIZONTAL
        );
        
        assertFalse(result.isSuccess());
    }
    
    @Test
    void testCanStart() {
        // Placer tous les navires du joueur 1
        placeAllShipsForPlayer("Joueur1");
        assertFalse(game.canStart()); // Joueur 2 n'a pas encore placé
        assertTrue(game.getPlayer1().hasPlacedAllShips(), "Joueur1 devrait avoir placé tous ses navires");
        
        // Placer tous les navires du joueur 2
        placeAllShipsForPlayer("Joueur2");
        assertTrue(game.getPlayer2().hasPlacedAllShips(), "Joueur2 devrait avoir placé tous ses navires");
        assertTrue(game.canStart());
    }
    
    @Test
    void testStart() {
        placeAllShipsForPlayer("Joueur1");
        placeAllShipsForPlayer("Joueur2");
        
        game.start();
        assertEquals(GameState.PLAYING, game.getState());
    }
    
    @Test
    void testShoot() {
        placeAllShipsForPlayer("Joueur1");
        placeAllShipsForPlayer("Joueur2");
        game.start();
        
        // Tirer sur une position où un navire du joueur 2 est placé (Porte-avions à (5,5))
        GameResult result = game.shoot("Joueur1", new Coordinate(5, 5));
        assertTrue(result.isSuccess());
        assertNotNull(result.getCellStatus());
    }
    
    @Test
    void testShootWrongTurn() {
        placeAllShipsForPlayer("Joueur1");
        placeAllShipsForPlayer("Joueur2");
        game.start();
        
        // Joueur 2 essaie de tirer alors que c'est le tour du joueur 1
        GameResult result = game.shoot("Joueur2", new Coordinate(0, 0));
        assertFalse(result.isSuccess());
    }
    
    @Test
    void testGameFinished() {
        placeAllShipsForPlayer("Joueur1");
        placeAllShipsForPlayer("Joueur2");
        game.start();
        
        // Couler tous les navires du joueur 2
        sinkAllShips("Joueur2");
        
        assertTrue(game.isFinished());
        assertNotNull(game.getWinner());
        assertEquals("Joueur1", game.getWinner().getName());
    }
    
    // Méthodes helper
    private void placeAllShipsForPlayer(String playerName) {
        // Placer les navires sur des colonnes complètement différentes pour éviter les adjacences
        // Joueur 1 : colonnes 0, 2, 4, 6
        // Joueur 2 : colonnes 0, 2, 4, 6 (même disposition, mais grille séparée)
        int[] cols = playerName.equals("Joueur1") ? new int[]{0, 2, 4, 6} : new int[]{0, 2, 4, 6};
        
        // Porte-avions (5 cases) vertical à (cols[0], 0)
        GameResult result1 = game.placeShip(playerName, "Porte-avions", new Coordinate(cols[0], 0), ShipOrientation.VERTICAL);
        assertTrue(result1.isSuccess(), "Placement Porte-avions devrait réussir");
        
        // Croiseur (4 cases) vertical à (cols[1], 0)
        GameResult result2 = game.placeShip(playerName, "Croiseur", new Coordinate(cols[1], 0), ShipOrientation.VERTICAL);
        assertTrue(result2.isSuccess(), "Placement Croiseur devrait réussir");
        
        // Destroyer (3 cases) vertical à (cols[2], 0)
        GameResult result3 = game.placeShip(playerName, "Destroyer", new Coordinate(cols[2], 0), ShipOrientation.VERTICAL);
        assertTrue(result3.isSuccess(), "Placement Destroyer devrait réussir");
        
        // Sous-marin (2 cases) vertical à (cols[3], 0)
        GameResult result4 = game.placeShip(playerName, "Sous-marin", new Coordinate(cols[3], 0), ShipOrientation.VERTICAL);
        assertTrue(result4.isSuccess(), "Placement Sous-marin devrait réussir");
    }
    
    private void sinkAllShips(String targetPlayerName) {
        // Couler tous les navires du joueur cible en tirant sur toutes les positions
        // On connaît les positions : colonnes 0, 2, 4, 6, lignes 0-4 (selon la taille)
        int[] cols = new int[]{0, 2, 4, 6};
        int[] sizes = new int[]{5, 4, 3, 2}; // Porte-avions, Croiseur, Destroyer, Sous-marin
        
        // Tirer sur chaque navire
        for (int shipIdx = 0; shipIdx < cols.length; shipIdx++) {
            int col = cols[shipIdx];
            int size = sizes[shipIdx];
            
            // Tirer sur chaque case du navire
            for (int row = 0; row < size; row++) {
                // Attendre que ce soit le tour du joueur 1
                while (game.getState() == GameState.PLAYING && 
                       !game.isFinished() && 
                       game.getCurrentTurn() != GameTurn.PLAYER1) {
                    // Passer le tour en tirant à l'eau (position sûre)
                    game.shoot("Joueur2", new Coordinate(9, 9));
                }
                
                if (game.getState() == GameState.PLAYING && !game.isFinished()) {
                    game.shoot("Joueur1", new Coordinate(col, row));
                }
            }
        }
    }
}
