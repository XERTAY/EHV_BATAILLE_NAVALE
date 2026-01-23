package domain.model.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CoordinateTest {
    
    @Test
    void testCoordinateCreation() {
        Coordinate coord = new Coordinate(5, 3);
        assertEquals(5, coord.getX());
        assertEquals(3, coord.getY());
    }
    
    @Test
    void testIsValid() {
        Coordinate coord1 = new Coordinate(5, 3);
        assertTrue(coord1.isValid(10));
        assertFalse(coord1.isValid(5));
        
        Coordinate coord2 = new Coordinate(-1, 3);
        assertFalse(coord2.isValid(10));
        
        Coordinate coord3 = new Coordinate(10, 3);
        assertFalse(coord3.isValid(10));
    }
    
    @Test
    void testAdd() {
        Coordinate coord = new Coordinate(5, 3);
        Coordinate result = coord.add(2, 4);
        assertEquals(7, result.getX());
        assertEquals(7, result.getY());
    }
    
    @Test
    void testEquals() {
        Coordinate coord1 = new Coordinate(5, 3);
        Coordinate coord2 = new Coordinate(5, 3);
        Coordinate coord3 = new Coordinate(5, 4);
        
        assertEquals(coord1, coord2);
        assertNotEquals(coord1, coord3);
    }
    
    @Test
    void testHashCode() {
        Coordinate coord1 = new Coordinate(5, 3);
        Coordinate coord2 = new Coordinate(5, 3);
        
        assertEquals(coord1.hashCode(), coord2.hashCode());
    }
}
