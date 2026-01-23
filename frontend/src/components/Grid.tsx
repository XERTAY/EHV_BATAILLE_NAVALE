import { CellStatus, Coordinate } from '../types/game';
import Cell from './Cell';
import './Grid.css';

interface GridProps {
  grid: CellStatus[][];
  onCellClick?: (coord: Coordinate) => void;
  onCellHover?: (coord: Coordinate | null) => void;
  showShips?: boolean;
  disabled?: boolean;
  title: string;
  previewCoords?: Coordinate[];
  canPlace?: boolean;
}

export default function Grid({ 
  grid, 
  onCellClick, 
  onCellHover,
  showShips = true,
  disabled = false,
  title,
  previewCoords = [],
  canPlace = false
}: GridProps) {
  const handleCellClick = (x: number, y: number) => {
    if (!disabled && onCellClick) {
      onCellClick({ x, y });
    }
  };

  const handleCellHover = (x: number, y: number) => {
    if (!disabled && onCellHover) {
      onCellHover({ x, y });
    }
  };

  const handleCellLeave = () => {
    if (!disabled && onCellHover) {
      onCellHover(null);
    }
  };

  const getCellStatus = (x: number, y: number): { status: CellStatus; isPreview: boolean } => {
    const status = grid[y][x];
    const isPreview = previewCoords.some(c => c.x === x && c.y === y);
    
    // Si c'est une prévisualisation valide, on montre le navire
    if (isPreview && canPlace) {
      return { status: CellStatus.SHIP, isPreview: true };
    }
    
    // Si on ne doit pas montrer les navires, on les cache
    if (!showShips && status === CellStatus.SHIP) {
      return { status: CellStatus.EMPTY, isPreview: false };
    }
    
    return { status, isPreview: false };
  };

  return (
    <div className="grid-container">
      <h3 className="grid-title">{title}</h3>
      <div className="grid">
        <div className="grid-labels-row">
          <div className="grid-corner"></div>
          {Array.from({ length: grid[0].length }, (_, i) => (
            <div key={i} className="grid-label">{String.fromCharCode(65 + i)}</div>
          ))}
        </div>
        {grid.map((row, y) => (
          <div key={y} className="grid-row">
            <div className="grid-label">{y + 1}</div>
            {row.map((_, x) => {
              const { status, isPreview } = getCellStatus(x, y);
              return (
                <Cell
                  key={`${x}-${y}`}
                  status={status}
                  onClick={() => handleCellClick(x, y)}
                  onMouseEnter={() => handleCellHover(x, y)}
                  onMouseLeave={handleCellLeave}
                  disabled={disabled}
                  isPreview={isPreview}
                />
              );
            })}
          </div>
        ))}
      </div>
    </div>
  );
}
