package domain.model.entities;

import domain.model.core.CellStatus;
import domain.model.core.Coordinate;
import domain.model.core.ShipOrientation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GridTest {
    private Grid grid;
    
    @BeforeEach
    void setUp() {
        grid = new Grid(10);
    }
    
    @Test
    void testGridCreation() {
        assertEquals(10, grid.getSize());
        assertEquals(CellStatus.EMPTY, grid.getCellStatus(new Coordinate(0, 0)));
    }
    
    @Test
    void testInvalidGridSize() {
        assertThrows(IllegalArgumentException.class, () -> new Grid(4));
        assertThrows(IllegalArgumentException.class, () -> new Grid(21));
    }
    
    @Test
    void testPlaceShip() {
        Ship ship = new Ship("Porte-avions", 5);
        Coordinate start = new Coordinate(0, 0);
        
        assertTrue(grid.isValidPlacement(ship, start, ShipOrientation.HORIZONTAL));
        assertTrue(grid.placeShip(ship, start, ShipOrientation.HORIZONTAL));
        assertEquals(CellStatus.SHIP, grid.getCellStatus(new Coordinate(0, 0)));
        assertEquals(CellStatus.SHIP, grid.getCellStatus(new Coordinate(4, 0)));
    }
    
    @Test
    void testPlaceShipOutOfBounds() {
        Ship ship = new Ship("Porte-avions", 5);
        Coordinate start = new Coordinate(8, 0);
        
        assertFalse(grid.isValidPlacement(ship, start, ShipOrientation.HORIZONTAL));
        assertFalse(grid.placeShip(ship, start, ShipOrientation.HORIZONTAL));
    }
    
    @Test
    void testPlaceShipOverlapping() {
        Ship ship1 = new Ship("Porte-avions", 5);
        Ship ship2 = new Ship("Croiseur", 4);
        
        grid.placeShip(ship1, new Coordinate(0, 0), ShipOrientation.HORIZONTAL);
        assertFalse(grid.isValidPlacement(ship2, new Coordinate(2, 0), ShipOrientation.HORIZONTAL));
    }
    
    @Test
    void testShootMiss() {
        CellStatus result = grid.shoot(new Coordinate(5, 5));
        assertEquals(CellStatus.MISS, result);
        assertEquals(CellStatus.MISS, grid.getCellStatus(new Coordinate(5, 5)));
    }
    
    @Test
    void testShootHit() {
        Ship ship = new Ship("Sous-marin", 2);
        grid.placeShip(ship, new Coordinate(0, 0), ShipOrientation.HORIZONTAL);
        
        CellStatus result = grid.shoot(new Coordinate(0, 0));
        assertEquals(CellStatus.HIT, result);
        assertEquals(CellStatus.HIT, grid.getCellStatus(new Coordinate(0, 0)));
    }
    
    @Test
    void testShootSunk() {
        Ship ship = new Ship("Sous-marin", 2);
        grid.placeShip(ship, new Coordinate(0, 0), ShipOrientation.HORIZONTAL);
        
        grid.shoot(new Coordinate(0, 0));
        CellStatus result = grid.shoot(new Coordinate(1, 0));
        assertEquals(CellStatus.SUNK, result);
        assertEquals(CellStatus.SUNK, grid.getCellStatus(new Coordinate(0, 0)));
        assertEquals(CellStatus.SUNK, grid.getCellStatus(new Coordinate(1, 0)));
    }
    
    @Test
    void testAllShipsSunk() {
        assertFalse(grid.allShipsSunk()); // Pas de navires
        
        Ship ship = new Ship("Sous-marin", 2);
        grid.placeShip(ship, new Coordinate(0, 0), ShipOrientation.HORIZONTAL);
        assertFalse(grid.allShipsSunk()); // Navire pas encore coulé
        
        grid.shoot(new Coordinate(0, 0));
        grid.shoot(new Coordinate(1, 0));
        assertTrue(grid.allShipsSunk()); // Navire coulé
    }
}
