// Types correspondant aux modèles Java du backend

export enum CellStatus {
  EMPTY = 'EMPTY',    // Case vide
  SHIP = 'SHIP',      // Case occupée par un navire (non touchée)
  HIT = 'HIT',        // Case touchée (navire)
  MISS = 'MISS',      // Case touchée (vide)
  SUNK = 'SUNK'       // Case d'un navire coulé
}

export enum ShipOrientation {
  HORIZONTAL = 'HORIZONTAL',
  VERTICAL = 'VERTICAL'
}

export interface Coordinate {
  x: number;
  y: number;
}

export interface Ship {
  id: string;
  name: string;
  size: number;
  coordinates: Coordinate[];
  orientation: ShipOrientation;
  sunk: boolean;
}

export interface GameState {
  playerGrid: CellStatus[][];
  opponentGrid: CellStatus[][];
  playerShips: Ship[];
  opponentShips: Ship[];
  currentPhase: 'PLACEMENT' | 'PLAYING' | 'GAME_OVER';
  currentPlayer: 'PLAYER' | 'OPPONENT';
  winner: 'PLAYER' | 'OPPONENT' | null;
}

export interface ShotResult {
  hit: boolean;
  sunk: boolean;
  shipId?: string;
  coordinate: Coordinate;
}
