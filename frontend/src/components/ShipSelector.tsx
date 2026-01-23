import { ShipOrientation } from '../types/game';
import './ShipSelector.css';

interface ShipConfig {
  name: string;
  size: number;
  id: string;
}

interface ShipSelectorProps {
  ships: ShipConfig[];
  selectedShip: ShipConfig | null;
  onSelectShip: (ship: ShipConfig) => void;
  orientation: ShipOrientation;
  onToggleOrientation: () => void;
  disabled?: boolean;
}

export default function ShipSelector({
  ships,
  selectedShip,
  onSelectShip,
  orientation,
  onToggleOrientation,
  disabled = false
}: ShipSelectorProps) {
  return (
    <div className="ship-selector">
      <h3 className="ship-selector-title">NAVIRES A PLACER</h3>
      <div className="ship-list">
        {ships.map((ship) => (
          <button
            key={ship.id}
            className={`ship-button ${selectedShip?.id === ship.id ? 'selected' : ''} ${disabled ? 'disabled' : ''}`}
            onClick={() => !disabled && onSelectShip(ship)}
            disabled={disabled}
          >
            <div className="ship-preview">
              {Array.from({ length: ship.size }, (_, i) => (
                <div
                  key={i}
                  className={`ship-segment ${orientation === ShipOrientation.HORIZONTAL ? 'horizontal' : 'vertical'}`}
                />
              ))}
            </div>
            <span className="ship-name">{ship.name}</span>
            <span className="ship-size">({ship.size})</span>
          </button>
        ))}
      </div>
      <button
        className={`orientation-button ${disabled ? 'disabled' : ''}`}
        onClick={onToggleOrientation}
        disabled={disabled}
      >
        {orientation === ShipOrientation.HORIZONTAL ? 'HORIZONTAL' : 'VERTICAL'}
      </button>
    </div>
  );
}
