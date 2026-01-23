import { useState, useCallback, useEffect } from 'react';
import './App.css';
import Grid from './components/Grid';
import ShipSelector from './components/ShipSelector';
import {
  CellStatus,
  Coordinate,
  Ship,
  ShipOrientation,
  GameState
} from './types/game';
import {
  createEmptyGrid,
  canPlaceShip,
  placeShip,
  shootAt,
  placeOpponentShips,
  areAllShipsSunk,
  GRID_SIZE,
  SHIP_CONFIGS
} from './services/mockBackend';

type GamePhase = 'PLACEMENT' | 'PLAYING' | 'GAME_OVER';

function App() {
  const [gameState, setGameState] = useState<GameState>({
    playerGrid: createEmptyGrid(),
    opponentGrid: createEmptyGrid(),
    playerShips: [],
    opponentShips: [],
    currentPhase: 'PLACEMENT',
    currentPlayer: 'PLAYER',
    winner: null
  });

  const [selectedShip, setSelectedShip] = useState<{ name: string; size: number; id: string } | null>(null);
  const [orientation, setOrientation] = useState<ShipOrientation>(ShipOrientation.HORIZONTAL);
  const [hoveredCoord, setHoveredCoord] = useState<Coordinate | null>(null);
  const [shipsToPlace, setShipsToPlace] = useState(
    SHIP_CONFIGS.map((config, index) => ({
      ...config,
      id: `ship-${index}`
    }))
  );

  // Initialiser les navires de l'adversaire au démarrage
  useEffect(() => {
    const { grid, ships } = placeOpponentShips();
    setGameState(prev => ({
      ...prev,
      opponentGrid: grid,
      opponentShips: ships
    }));
  }, []);

  const handlePlaceShip = useCallback((coord: Coordinate) => {
    if (gameState.currentPhase !== 'PLACEMENT' || !selectedShip) return;

    if (canPlaceShip(gameState.playerGrid, coord, selectedShip.size, orientation)) {
      const { grid, ship } = placeShip(
        gameState.playerGrid,
        coord,
        selectedShip.size,
        orientation,
        selectedShip.id
      );

      setGameState(prev => ({
        ...prev,
        playerGrid: grid,
        playerShips: [...prev.playerShips, ship]
      }));

      // Retirer le navire de la liste
      setShipsToPlace(prev => prev.filter(s => s.id !== selectedShip.id));
      setSelectedShip(null);

      // Vérifier si tous les navires sont placés
      if (shipsToPlace.length === 1) {
        setTimeout(() => {
          setGameState(prev => ({
            ...prev,
            currentPhase: 'PLAYING'
          }));
        }, 500);
      }
    }
  }, [gameState.playerGrid, selectedShip, orientation, shipsToPlace.length]);

  const handleShoot = useCallback((coord: Coordinate) => {
    if (gameState.currentPhase !== 'PLAYING' || gameState.currentPlayer !== 'PLAYER') return;
    
    const cellStatus = gameState.opponentGrid[coord.y][coord.x];
    if (cellStatus === CellStatus.HIT || cellStatus === CellStatus.MISS || cellStatus === CellStatus.SUNK) {
      return; // Déjà tiré
    }

    const result = shootAt(
      [...gameState.opponentGrid.map(row => [...row])],
      gameState.opponentShips,
      coord
    );

    setGameState(prev => {
      const newOpponentGrid = [...prev.opponentGrid.map(row => [...row])];
      newOpponentGrid[coord.y][coord.x] = result.hit ? CellStatus.HIT : CellStatus.MISS;

      if (result.sunk) {
        const updatedShips = prev.opponentShips.map(ship =>
          ship.id === result.shipId ? { ...ship, sunk: true } : ship
        );
        
        // Marquer toutes les cases du navire comme coulées
        const sunkShip = updatedShips.find(s => s.id === result.shipId);
        if (sunkShip) {
          sunkShip.coordinates.forEach(c => {
            newOpponentGrid[c.y][c.x] = CellStatus.SUNK;
          });
        }

        // Vérifier si tous les navires adverses sont coulés
        if (areAllShipsSunk(updatedShips)) {
          return {
            ...prev,
            opponentGrid: newOpponentGrid,
            opponentShips: updatedShips,
            currentPhase: 'GAME_OVER',
            winner: 'PLAYER'
          };
        }

        return {
          ...prev,
          opponentGrid: newOpponentGrid,
          opponentShips: updatedShips
        };
      }

      // Si pas touché, c'est au tour de l'adversaire (IA simple)
      if (!result.hit) {
        setTimeout(() => {
          aiTurn();
        }, 1000);
      }

      return {
        ...prev,
        opponentGrid: newOpponentGrid
      };
    });
  }, [gameState]);

  const aiTurn = useCallback(() => {
    // IA simple : tire aléatoirement sur les cases non touchées
    const availableCells: Coordinate[] = [];
    
    for (let y = 0; y < GRID_SIZE; y++) {
      for (let x = 0; x < GRID_SIZE; x++) {
        const status = gameState.playerGrid[y][x];
        if (status !== CellStatus.HIT && status !== CellStatus.MISS && status !== CellStatus.SUNK) {
          availableCells.push({ x, y });
        }
      }
    }

    if (availableCells.length === 0) return;

    const randomCell = availableCells[Math.floor(Math.random() * availableCells.length)];
    const result = shootAt(
      [...gameState.playerGrid.map(row => [...row])],
      gameState.playerShips,
      randomCell
    );

    setGameState(prev => {
      const newPlayerGrid = [...prev.playerGrid.map(row => [...row])];
      newPlayerGrid[randomCell.y][randomCell.x] = result.hit ? CellStatus.HIT : CellStatus.MISS;

      if (result.sunk) {
        const updatedShips = prev.playerShips.map(ship =>
          ship.id === result.shipId ? { ...ship, sunk: true } : ship
        );
        
        const sunkShip = updatedShips.find(s => s.id === result.shipId);
        if (sunkShip) {
          sunkShip.coordinates.forEach(c => {
            newPlayerGrid[c.y][c.x] = CellStatus.SUNK;
          });
        }

        if (areAllShipsSunk(updatedShips)) {
          return {
            ...prev,
            playerGrid: newPlayerGrid,
            playerShips: updatedShips,
            currentPhase: 'GAME_OVER',
            winner: 'OPPONENT'
          };
        }

        return {
          ...prev,
          playerGrid: newPlayerGrid,
          playerShips: updatedShips
        };
      }

      return {
        ...prev,
        playerGrid: newPlayerGrid
      };
    });
  }, [gameState.playerGrid, gameState.playerShips]);

  const getPreviewCoordinates = (): Coordinate[] => {
    if (!hoveredCoord || !selectedShip || gameState.currentPhase !== 'PLACEMENT') {
      return [];
    }

    const coords: Coordinate[] = [];
    for (let i = 0; i < selectedShip.size; i++) {
      const coord = orientation === ShipOrientation.HORIZONTAL
        ? { x: hoveredCoord.x + i, y: hoveredCoord.y }
        : { x: hoveredCoord.x, y: hoveredCoord.y + i };
      
      if (coord.x >= 0 && coord.x < GRID_SIZE && coord.y >= 0 && coord.y < GRID_SIZE) {
        coords.push(coord);
      }
    }
    return coords;
  };

  const previewCoords = getPreviewCoordinates();
  const canPlace = selectedShip && hoveredCoord
    ? canPlaceShip(gameState.playerGrid, hoveredCoord, selectedShip.size, orientation)
    : false;

  const handleReset = () => {
    const { grid, ships } = placeOpponentShips();
    setGameState({
      playerGrid: createEmptyGrid(),
      opponentGrid: grid,
      playerShips: [],
      opponentShips: ships,
      currentPhase: 'PLACEMENT',
      currentPlayer: 'PLAYER',
      winner: null
    });
    setShipsToPlace(SHIP_CONFIGS.map((config, index) => ({
      ...config,
      id: `ship-${index}`
    })));
    setSelectedShip(null);
  };

  return (
    <div className="app">
      <header className="app-header">
        <h1>BATAILLE NAVALE</h1>
        {gameState.currentPhase === 'GAME_OVER' && (
          <div className="game-over-message">
            {gameState.winner === 'PLAYER' ? 'VICTOIRE !' : 'DEFAITE !'}
          </div>
        )}
        {gameState.currentPhase === 'PLAYING' && (
          <div className="game-status">
            {gameState.currentPlayer === 'PLAYER' ? 'VOTRE TOUR' : 'TOUR ADVERSAIRE...'}
          </div>
        )}
        {gameState.currentPhase === 'PLACEMENT' && (
          <div className="game-status">
            PLACEZ VOS NAVIRES ({shipsToPlace.length} RESTANT{shipsToPlace.length > 1 ? 'S' : ''})
          </div>
        )}
      </header>

      <div className="game-container">
        {gameState.currentPhase === 'PLACEMENT' && (
          <ShipSelector
            ships={shipsToPlace}
            selectedShip={selectedShip}
            onSelectShip={setSelectedShip}
            orientation={orientation}
            onToggleOrientation={() => 
              setOrientation(prev => 
                prev === ShipOrientation.HORIZONTAL 
                  ? ShipOrientation.VERTICAL 
                  : ShipOrientation.HORIZONTAL
              )
            }
          />
        )}

        <div className="grids-container">
          <Grid
            grid={gameState.playerGrid}
            onCellClick={handlePlaceShip}
            onCellHover={setHoveredCoord}
            showShips={true}
            disabled={gameState.currentPhase !== 'PLACEMENT'}
            title="VOTRE GRILLE"
            previewCoords={previewCoords}
            canPlace={canPlace}
          />

          <Grid
            grid={gameState.opponentGrid}
            onCellClick={handleShoot}
            showShips={false}
            disabled={gameState.currentPhase !== 'PLAYING' || gameState.currentPlayer !== 'PLAYER'}
            title="GRILLE ADVERSAIRE"
          />
        </div>
      </div>

      <div className="game-info">
        <div className="ships-status">
          <div className="ships-section">
            <h4>VOS NAVIRES</h4>
            <div className="ships-list">
              {gameState.playerShips.map(ship => (
                <div key={ship.id} className={`ship-status ${ship.sunk ? 'sunk' : ''}`}>
                  <span>{ship.name}</span>
                  <span>{ship.sunk ? '[COULE]' : '[OK]'}</span>
                </div>
              ))}
            </div>
          </div>
          <div className="ships-section">
            <h4>NAVIRES ADVERSES</h4>
            <div className="ships-list">
              {gameState.opponentShips.map(ship => (
                <div key={ship.id} className={`ship-status ${ship.sunk ? 'sunk' : ''}`}>
                  <span>{ship.name}</span>
                  <span>{ship.sunk ? '[COULE]' : '[?]'}</span>
                </div>
              ))}
            </div>
          </div>
        </div>
        <button className="reset-button" onClick={handleReset}>
          NOUVELLE PARTIE
        </button>
      </div>
    </div>
  );
}

export default App;
