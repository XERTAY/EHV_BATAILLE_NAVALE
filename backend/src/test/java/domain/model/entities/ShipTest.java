package domain.model.entities;

import domain.model.core.Coordinate;
import domain.model.core.ShipOrientation;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ShipTest {
    
    @Test
    void testShipCreation() {
        Ship ship = new Ship("Porte-avions", 5);
        assertEquals("Porte-avions", ship.getName());
        assertEquals(5, ship.getSize());
        assertFalse(ship.isPlaced());
    }
    
    @Test
    void testShipInvalidSize() {
        assertThrows(IllegalArgumentException.class, () -> new Ship("Test", 1));
        assertThrows(IllegalArgumentException.class, () -> new Ship("Test", 6));
    }
    
    @Test
    void testPlaceShip() {
        Ship ship = new Ship("Croiseur", 4);
        Coordinate start = new Coordinate(0, 0);
        ship.place(start, ShipOrientation.HORIZONTAL);
        
        assertTrue(ship.isPlaced());
        assertEquals(start, ship.getStartPosition());
        assertEquals(ShipOrientation.HORIZONTAL, ship.getOrientation());
    }
    
    @Test
    void testGetOccupiedCellsHorizontal() {
        Ship ship = new Ship("Destroyer", 3);
        ship.place(new Coordinate(2, 2), ShipOrientation.HORIZONTAL);
        
        var cells = ship.getOccupiedCells();
        assertEquals(3, cells.size());
        assertTrue(cells.contains(new Coordinate(2, 2)));
        assertTrue(cells.contains(new Coordinate(3, 2)));
        assertTrue(cells.contains(new Coordinate(4, 2)));
    }
    
    @Test
    void testGetOccupiedCellsVertical() {
        Ship ship = new Ship("Destroyer", 3);
        ship.place(new Coordinate(2, 2), ShipOrientation.VERTICAL);
        
        var cells = ship.getOccupiedCells();
        assertEquals(3, cells.size());
        assertTrue(cells.contains(new Coordinate(2, 2)));
        assertTrue(cells.contains(new Coordinate(2, 3)));
        assertTrue(cells.contains(new Coordinate(2, 4)));
    }
    
    @Test
    void testHit() {
        Ship ship = new Ship("Sous-marin", 2);
        ship.place(new Coordinate(0, 0), ShipOrientation.HORIZONTAL);
        
        assertFalse(ship.isSunk());
        boolean sunk = ship.hit(new Coordinate(0, 0));
        assertFalse(sunk);
        assertFalse(ship.isSunk());
        
        sunk = ship.hit(new Coordinate(1, 0));
        assertTrue(sunk);
        assertTrue(ship.isSunk());
    }
    
    @Test
    void testContains() {
        Ship ship = new Ship("Croiseur", 4);
        ship.place(new Coordinate(0, 0), ShipOrientation.HORIZONTAL);
        
        assertTrue(ship.contains(new Coordinate(0, 0)));
        assertTrue(ship.contains(new Coordinate(3, 0)));
        assertFalse(ship.contains(new Coordinate(4, 0)));
        assertFalse(ship.contains(new Coordinate(0, 1)));
    }
}
