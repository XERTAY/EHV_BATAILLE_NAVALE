import { Coordinate, CellStatus, ShipOrientation, Ship, ShotResult } from '../types/game';

// Service mock pour simuler le backend Java

const GRID_SIZE = 10;

// Configuration des navires standards
const SHIP_CONFIGS = [
  { name: 'Porte-avions', size: 5 },
  { name: 'Cuirassé', size: 4 },
  { name: 'Croiseur', size: 3 },
  { name: 'Sous-marin', size: 3 },
  { name: 'Destroyer', size: 2 }
];

/**
 * Génère une grille vide
 */
export function createEmptyGrid(): CellStatus[][] {
  return Array(GRID_SIZE).fill(null).map(() => 
    Array(GRID_SIZE).fill(CellStatus.EMPTY)
  );
}

/**
 * Vérifie si une coordonnée est valide
 */
export function isValidCoordinate(coord: Coordinate): boolean {
  return coord.x >= 0 && coord.x < GRID_SIZE && coord.y >= 0 && coord.y < GRID_SIZE;
}

/**
 * Vérifie si un navire peut être placé à une position donnée
 */
export function canPlaceShip(
  grid: CellStatus[][],
  startCoord: Coordinate,
  size: number,
  orientation: ShipOrientation
): boolean {
  const coords: Coordinate[] = [];
  
  for (let i = 0; i < size; i++) {
    const coord = orientation === ShipOrientation.HORIZONTAL
      ? { x: startCoord.x + i, y: startCoord.y }
      : { x: startCoord.x, y: startCoord.y + i };
    
    if (!isValidCoordinate(coord)) return false;
    if (grid[coord.y][coord.x] !== CellStatus.EMPTY) return false;
    
    // Vérifier les cases adjacentes
    const adjacent = [
      { x: coord.x - 1, y: coord.y },
      { x: coord.x + 1, y: coord.y },
      { x: coord.x, y: coord.y - 1 },
      { x: coord.x, y: coord.y + 1 }
    ];
    
    for (const adj of adjacent) {
      if (isValidCoordinate(adj) && grid[adj.y][adj.x] === CellStatus.SHIP) {
        return false;
      }
    }
    
    coords.push(coord);
  }
  
  return true;
}

/**
 * Place un navire sur la grille
 */
export function placeShip(
  grid: CellStatus[][],
  startCoord: Coordinate,
  size: number,
  orientation: ShipOrientation,
  shipId: string
): { grid: CellStatus[][]; ship: Ship } {
  const newGrid = grid.map(row => [...row]);
  const coords: Coordinate[] = [];
  
  for (let i = 0; i < size; i++) {
    const coord = orientation === ShipOrientation.HORIZONTAL
      ? { x: startCoord.x + i, y: startCoord.y }
      : { x: startCoord.x, y: startCoord.y + i };
    
    newGrid[coord.y][coord.x] = CellStatus.SHIP;
    coords.push(coord);
  }
  
  const ship: Ship = {
    id: shipId,
    name: SHIP_CONFIGS.find(s => s.size === size)?.name || `Navire ${size}`,
    size,
    coordinates: coords,
    orientation,
    sunk: false
  };
  
  return { grid: newGrid, ship };
}

/**
 * Simule un tir sur la grille adverse
 */
export function shootAt(
  grid: CellStatus[][],
  ships: Ship[],
  coordinate: Coordinate
): ShotResult {
  const cellStatus = grid[coordinate.y][coordinate.x];
  
  // Si déjà tiré
  if (cellStatus === CellStatus.HIT || cellStatus === CellStatus.MISS || cellStatus === CellStatus.SUNK) {
    return {
      hit: false,
      sunk: false,
      coordinate
    };
  }
  
  // Si touché
  if (cellStatus === CellStatus.SHIP) {
    grid[coordinate.y][coordinate.x] = CellStatus.HIT;
    
    // Trouver le navire touché
    const hitShip = ships.find(ship => 
      ship.coordinates.some(coord => coord.x === coordinate.x && coord.y === coordinate.y)
    );
    
    if (hitShip) {
      // Vérifier si le navire est coulé
      const allHit = hitShip.coordinates.every(coord => 
        grid[coord.y][coord.x] === CellStatus.HIT || grid[coord.y][coord.x] === CellStatus.SUNK
      );
      
      if (allHit) {
        // Marquer toutes les cases du navire comme coulées
        hitShip.coordinates.forEach(coord => {
          grid[coord.y][coord.x] = CellStatus.SUNK;
        });
        hitShip.sunk = true;
        
        return {
          hit: true,
          sunk: true,
          shipId: hitShip.id,
          coordinate
        };
      }
    }
    
    return {
      hit: true,
      sunk: false,
      coordinate
    };
  }
  
  // Si manqué
  grid[coordinate.y][coordinate.x] = CellStatus.MISS;
  return {
    hit: false,
    sunk: false,
    coordinate
  };
}

/**
 * Place automatiquement les navires de l'adversaire (IA)
 */
export function placeOpponentShips(): { grid: CellStatus[][]; ships: Ship[] } {
  const grid = createEmptyGrid();
  const ships: Ship[] = [];
  
  for (let i = 0; i < SHIP_CONFIGS.length; i++) {
    const config = SHIP_CONFIGS[i];
    let placed = false;
    let attempts = 0;
    
    while (!placed && attempts < 100) {
      const orientation = Math.random() > 0.5 
        ? ShipOrientation.HORIZONTAL 
        : ShipOrientation.VERTICAL;
      
      const maxX = orientation === ShipOrientation.HORIZONTAL 
        ? GRID_SIZE - config.size 
        : GRID_SIZE - 1;
      const maxY = orientation === ShipOrientation.VERTICAL 
        ? GRID_SIZE - config.size 
        : GRID_SIZE - 1;
      
      const startCoord: Coordinate = {
        x: Math.floor(Math.random() * (maxX + 1)),
        y: Math.floor(Math.random() * (maxY + 1))
      };
      
      if (canPlaceShip(grid, startCoord, config.size, orientation)) {
        const result = placeShip(grid, startCoord, config.size, orientation, `opponent-ship-${i}`);
        ships.push(result.ship);
        placed = true;
      }
      
      attempts++;
    }
  }
  
  return { grid, ships };
}

/**
 * Vérifie si tous les navires sont coulés
 */
export function areAllShipsSunk(ships: Ship[]): boolean {
  return ships.length > 0 && ships.every(ship => ship.sunk);
}

export { GRID_SIZE, SHIP_CONFIGS };
