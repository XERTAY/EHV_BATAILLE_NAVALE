import { CellStatus } from '../types/game';
import './Cell.css';

interface CellProps {
  status: CellStatus;
  onClick?: () => void;
  onMouseEnter?: () => void;
  onMouseLeave?: () => void;
  isPreview?: boolean;
  disabled?: boolean;
}

export default function Cell({ 
  status, 
  onClick, 
  onMouseEnter, 
  onMouseLeave,
  isPreview = false,
  disabled = false
}: CellProps) {
  const getCellClass = () => {
    const baseClass = 'cell';
    const statusClass = `cell-${status.toLowerCase()}`;
    const previewClass = isPreview ? 'cell-preview' : '';
    const disabledClass = disabled ? 'cell-disabled' : '';
    
    return `${baseClass} ${statusClass} ${previewClass} ${disabledClass}`.trim();
  };

  return (
    <div
      className={getCellClass()}
      onClick={disabled ? undefined : onClick}
      onMouseEnter={disabled ? undefined : onMouseEnter}
      onMouseLeave={disabled ? undefined : onMouseLeave}
    />
  );
}
