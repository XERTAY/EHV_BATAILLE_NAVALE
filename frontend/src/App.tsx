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

  const aiTurn = useCallback(() => {
    const executeAITurn = () => {
      setGameState(prev => {
        // Vérifier que c'est bien le tour de l'IA
        if (prev.currentPhase !== 'PLAYING' || prev.currentPlayer !== 'OPPONENT') {
          return prev;
        }

        // IA simple : tire aléatoirement sur les cases non touchées
        const availableCells: Coordinate[] = [];
        
        for (let y = 0; y < GRID_SIZE; y++) {
          for (let x = 0; x < GRID_SIZE; x++) {
            const status = prev.playerGrid[y][x];
            if (status !== CellStatus.HIT && status !== CellStatus.MISS && status !== CellStatus.SUNK) {
              availableCells.push({ x, y });
            }
          }
        }

        if (availableCells.length === 0) {
          return prev;
        }

        const randomCell = availableCells[Math.floor(Math.random() * availableCells.length)];

        // Créer une copie de la grille et des navires pour shootAt
        const gridCopy = prev.playerGrid.map(row => [...row]);
        const shipsCopy = prev.playerShips.map(ship => ({
          ...ship,
          coordinates: [...ship.coordinates]
        }));

        const result = shootAt(
          gridCopy,
          shipsCopy,
          randomCell
        );

        // Utiliser la grille modifiée par shootAt (qui a déjà mis à jour HIT/MISS/SUNK)
        // shootAt modifie directement gridCopy et shipsCopy, donc on les utilise tels quels
        const newPlayerGrid = gridCopy;
        const updatedShips = shipsCopy;

        // Vérifier si tous les navires du joueur sont coulés
        if (result.sunk && result.shipId && areAllShipsSunk(updatedShips)) {
          return {
            ...prev,
            playerGrid: newPlayerGrid,
            playerShips: updatedShips,
            currentPhase: 'GAME_OVER',
            winner: 'OPPONENT',
            currentPlayer: 'OPPONENT'
          };
        }

        // Si touché, l'IA rejoue, sinon c'est au tour du joueur
        const nextPlayer = result.hit ? 'OPPONENT' : 'PLAYER';

        const newState = {
          ...prev,
          playerGrid: newPlayerGrid,
          playerShips: updatedShips,
          currentPlayer: nextPlayer
        };

        // Si l'IA a touché, elle rejoue immédiatement
        if (result.hit) {
          setTimeout(() => {
            executeAITurn();
          }, 1000);
        }

        return newState;
      });
    };

    executeAITurn();
  }, []);

  const handleShoot = useCallback((coord: Coordinate) => {
    if (gameState.currentPhase !== 'PLAYING' || gameState.currentPlayer !== 'PLAYER') return;
    
    const cellStatus = gameState.opponentGrid[coord.y][coord.x];
    if (cellStatus === CellStatus.HIT || cellStatus === CellStatus.MISS || cellStatus === CellStatus.SUNK) {
      return; // Déjà tiré
    }

    // Créer une copie de la grille et des navires pour shootAt
    const gridCopy = gameState.opponentGrid.map(row => [...row]);
    const shipsCopy = gameState.opponentShips.map(ship => ({
      ...ship,
      coordinates: [...ship.coordinates]
    }));

    const result = shootAt(
      gridCopy,
      shipsCopy,
      coord
    );

    setGameState(prev => {
      // Utiliser la grille modifiée par shootAt (qui a déjà mis à jour HIT/MISS/SUNK)
      // shootAt modifie directement gridCopy et shipsCopy, donc on les utilise tels quels
      const newOpponentGrid = gridCopy;
      const updatedShips = shipsCopy;

      // Vérifier si tous les navires adverses sont coulés
      if (result.sunk && result.shipId && areAllShipsSunk(updatedShips)) {
        return {
          ...prev,
          opponentGrid: newOpponentGrid,
          opponentShips: updatedShips,
          currentPhase: 'GAME_OVER',
          winner: 'PLAYER',
          currentPlayer: 'PLAYER'
        };
      }

      // Si touché, le joueur rejoue, sinon c'est au tour de l'IA
      const nextPlayer = result.hit ? 'PLAYER' : 'OPPONENT';

      return {
        ...prev,
        opponentGrid: newOpponentGrid,
        opponentShips: updatedShips,
        currentPlayer: nextPlayer
      };
    });

    // Si pas touché, c'est au tour de l'adversaire (IA simple)
    if (!result.hit) {
      setTimeout(() => {
        aiTurn();
      }, 1000);
    }
  }, [gameState, aiTurn]);

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
        <h1>🚢 BATAILLE NAVALE</h1>
        {gameState.currentPhase === 'GAME_OVER' && (
          <div className="game-over-message">
            {gameState.winner === 'PLAYER' ? '🎉 Vous avez gagné !' : '💀 Vous avez perdu !'}
          </div>
        )}
        {gameState.currentPhase === 'PLAYING' && (
          <div className="game-status">
            {gameState.currentPlayer === 'PLAYER' ? '🎯 Votre tour' : '⏳ Tour de l\'adversaire...'}
          </div>
        )}
        {gameState.currentPhase === 'PLACEMENT' && (
          <div className="game-status">
            📍 Placez vos navires ({shipsToPlace.length} restant{shipsToPlace.length > 1 ? 's' : ''})
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
            title="Votre Grille"
            previewCoords={previewCoords}
            canPlace={canPlace}
          />

          <Grid
            grid={gameState.opponentGrid}
            onCellClick={handleShoot}
            showShips={false}
            disabled={gameState.currentPhase !== 'PLAYING' || gameState.currentPlayer !== 'PLAYER'}
            title="Grille Adversaire"
          />
        </div>
      </div>

      <div className="game-info">
        <div className="ships-status">
          <div className="ships-section">
            <h4>Vos Navires</h4>
            <div className="ships-list">
              {gameState.playerShips.map(ship => (
                <div key={ship.id} className={`ship-status ${ship.sunk ? 'sunk' : ''}`}>
                  {ship.name} {ship.sunk ? '💥' : '✅'}
                </div>
              ))}
            </div>
          </div>
          <div className="ships-section">
            <h4>Navires Adverses</h4>
            <div className="ships-list">
              {gameState.opponentShips.map(ship => (
                <div key={ship.id} className={`ship-status ${ship.sunk ? 'sunk' : ''}`}>
                  {ship.name} {ship.sunk ? '💥' : '❓'}
                </div>
              ))}
            </div>
          </div>
        </div>
        <button className="reset-button" onClick={handleReset}>
          🔄 Nouvelle Partie
        </button>
      </div>
    </div>
  );
}

export default App;
