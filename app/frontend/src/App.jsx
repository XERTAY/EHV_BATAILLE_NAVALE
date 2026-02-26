import { useState, useEffect, useCallback } from "react";
import * as api from "./api";

const ORIENTATIONS = [
  { value: "H", label: "H (droite)" },
  { value: "-H", label: "-H (gauche)" },
  { value: "V", label: "V (bas)" },
  { value: "-V", label: "-V (haut)" },
];

function parseTargetGrid(str, size) {
  if (!str) return [];
  const lines = str.trim().split("\n").filter(Boolean);
  const grid = [];
  for (let i = 1; i < lines.length && grid.length < size; i++) {
    const part = lines[i].split("|")[1];
    if (!part) continue;
    grid.push(part.trim().split(/\s+/));
  }
  return grid;
}

/** Symboles console pour la grille de tir : O → ~, X → S, ? → ? */
function targetCellToLegend(cell) {
  if (cell === "O") return "~";
  if (cell === "X") return "S";
  return cell;
}

/** Retourne les cellules occupées par un navire (coordonnées 0-indexées). */
function getShipCells(x, y, size, orientation) {
  const cells = [];
  for (let i = 0; i < size; i++) {
    switch (orientation) {
      case "H": cells.push({ x: x + i, y }); break;
      case "-H": cells.push({ x: x - i, y }); break;
      case "V": cells.push({ x, y: y + i }); break;
      case "-V": cells.push({ x, y: y - i }); break;
      default: cells.push({ x: x + i, y });
    }
  }
  return cells;
}

/** Construit une grille de placement (taille x taille) : '' = eau, 'S' = navire. */
function buildPlacementGrid(gridSize, placedShips) {
  const grid = Array.from({ length: gridSize }, () => Array(gridSize).fill(""));
  for (const ship of placedShips) {
    const cells = getShipCells(ship.x, ship.y, ship.size, ship.orientation);
    for (const { x, y } of cells) {
      if (x >= 0 && x < gridSize && y >= 0 && y < gridSize) grid[y][x] = "S";
    }
  }
  return grid;
}

export default function App() {
  const [state, setState] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);
  const [lastShotResult, setLastShotResult] = useState(null);
  const [initParams, setInitParams] = useState({ gridSize: 10, shipSizes: [5, 4, 4, 3, 2] });
  const [placementCount, setPlacementCount] = useState({ 0: 0, 1: 0 });
  const [placementShips, setPlacementShips] = useState({ 0: [], 1: [] });
  const [placementForm, setPlacementForm] = useState({ x: 1, y: 1, orientation: "H" });

  const fetchState = useCallback(async () => {
    try {
      const s = await api.getGameState();
      setState(s);
      setError(null);
      return s;
    } catch (e) {
      setState(null);
      setError(e.message);
      return null;
    }
  }, []);

  useEffect(() => {
    api.getGameState()
      .then(setState)
      .catch(() => setState(null));
  }, []);

  async function handleInit(e) {
    e.preventDefault();
    setError(null);
    setLoading(true);
    const gridSize = Math.max(5, Number(initParams.gridSize) || 10);
    const shipSizes = initParams.shipSizes?.length
      ? initParams.shipSizes
      : [5, 4, 4, 3, 2];
    try {
      await api.initGame(gridSize, shipSizes);
      setInitParams((p) => ({ ...p, gridSize, shipSizes }));
      setPlacementCount({ 0: 0, 1: 0 });
      setPlacementShips({ 0: [], 1: [] });
      await fetchState();
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }

  async function handlePlaceShip(e) {
    e.preventDefault();
    if (!state) return;
    const cur = state.currentPlayerIndex;
    const shipIndex = placementCount[cur] ?? 0;
    const size = state.state === "PLACEMENT" ? initParams.shipSizes[shipIndex] : 5;
    const x = placementForm.x - 1;
    const y = placementForm.y - 1;
    const or = placementForm.orientation;
    setError(null);
    setLoading(true);
    try {
      const can = await api.canPlaceShip(cur, x, y, size, or);
      if (!can) {
        setError("Placement invalide (hors grille ou chevauchement).");
        setLoading(false);
        return;
      }
      await api.placeShip(cur, x, y, size, or, `Navire ${shipIndex + 1}`);
      setPlacementCount((prev) => ({ ...prev, [cur]: (prev[cur] ?? 0) + 1 }));
      setPlacementShips((prev) => ({
        ...prev,
        [cur]: [...(prev[cur] ?? []), { x, y, size, orientation: or }],
      }));
      await fetchState();
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }

  async function handleFinishPlacement() {
    setError(null);
    setLoading(true);
    try {
      await api.finishPlacement();
      await fetchState();
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }

  async function handleShoot(x, y) {
    if (!state || state.state !== "PLAYING") return;
    setError(null);
    setLastShotResult(null);
    setLoading(true);
    try {
      const result = await api.shoot(x, y);
      setLastShotResult(result);
      await fetchState();
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }

  if (state === undefined) {
    return (
      <main className="app">
        <p>Chargement...</p>
      </main>
    );
  }

  if (!state) {
    return (
      <main className="app">
        <h1>Bataille navale</h1>
        <p>Créez une partie pour commencer.</p>
        {error && <p className="error">{error}</p>}
        <form onSubmit={handleInit}>
          <label>
            Taille grille (min 5):{" "}
            <input
              type="number"
              min={5}
              value={initParams.gridSize}
              onChange={(e) =>
                setInitParams((p) => ({ ...p, gridSize: Number(e.target.value) }))
              }
            />
          </label>
          <label>
            Tailles des navires (séparées par des virgules, ex: 5,4,4,3,2):{" "}
            <input
              value={initParams.shipSizes.join(",")}
              onChange={(e) =>
                setInitParams((p) => ({
                  ...p,
                  shipSizes: e.target.value.split(",").map((s) => parseInt(s.trim(), 10)).filter(Boolean),
                }))
              }
            />
          </label>
          <button type="submit" disabled={loading}>
            {loading ? "Création..." : "Créer la partie"}
          </button>
        </form>
      </main>
    );
  }

  const currentPlayer = state.players?.[state.currentPlayerIndex];
  const targetPlayer = state.players?.[state.currentPlayerIndex === 0 ? 1 : 0];
  const gridSize = state.gridSize ?? 10;
  const shipSizes = initParams.shipSizes.length ? initParams.shipSizes : [5, 4, 4, 3, 2];

  if (state.state === "PLACEMENT") {
    const cur = state.currentPlayerIndex;
    const shipIndex = placementCount[cur] ?? 0;
    const currentSize = shipSizes[shipIndex];
    const allReady = state.players?.every((p) => p.fleetComplete) ?? false;
    const placementGrid = buildPlacementGrid(gridSize, placementShips[cur] ?? []);

    return (
      <main className="app">
        <h1>Bataille navale — Placement</h1>
        {error && <p className="error">{error}</p>}
        <p>
          <strong>Placement de la flotte de {currentPlayer?.name ?? `Joueur ${cur + 1}`}</strong>
        </p>
        <p>
          Navire {shipIndex + 1} / {shipSizes.length} ({currentSize} case{currentSize > 1 ? "s" : ""})
        </p>
        <p>Grille : S = navire, ~ = eau. Coordonnées de 1 à {gridSize}. Orientation : H, -H, V, -V.</p>
        <div className="grid-wrap">
          <table className="grid grid-placement" cellPadding={0} cellSpacing={0}>
            <thead>
              <tr>
                <th></th>
                {Array.from({ length: gridSize }, (_, i) => (
                  <th key={i}>{i + 1}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {placementGrid.map((row, y) => (
                <tr key={y}>
                  <th>{y + 1}</th>
                  {row.map((cell, x) => (
                    <td key={x} className={`cell-${cell || "~"}`}>
                      {cell || "~"}
                    </td>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <form onSubmit={handlePlaceShip}>
          <label>
            x:{" "}
            <input
              type="number"
              min={1}
              max={gridSize}
              value={placementForm.x}
              onChange={(e) => setPlacementForm((f) => ({ ...f, x: Number(e.target.value) }))}
            />
          </label>
          <label>
            y:{" "}
            <input
              type="number"
              min={1}
              max={gridSize}
              value={placementForm.y}
              onChange={(e) => setPlacementForm((f) => ({ ...f, y: Number(e.target.value) }))}
            />
          </label>
          <label>
            Orientation:{" "}
            <select
              value={placementForm.orientation}
              onChange={(e) =>
                setPlacementForm((f) => ({ ...f, orientation: e.target.value }))
              }
            >
              {ORIENTATIONS.map((o) => (
                <option key={o.value} value={o.value}>
                  {o.label}
                </option>
              ))}
            </select>
          </label>
          <button type="submit" disabled={loading}>
            Placer
          </button>
        </form>
        {allReady && (
          <p>
            <button type="button" onClick={handleFinishPlacement} disabled={loading}>
              Finir le placement et lancer la partie
            </button>
          </p>
        )}
      </main>
    );
  }

  if (state.state === "PLAYING") {
    let targetGrid = targetPlayer?.targetGridView
      ? parseTargetGrid(targetPlayer.targetGridView, gridSize)
      : [];
    if (targetGrid.length !== gridSize && gridSize > 0) {
      targetGrid = Array.from({ length: gridSize }, () => Array(gridSize).fill("O"));
    }

    return (
      <main className="app">
        <h1>Bataille navale — Tir</h1>
        {error && <p className="error">{error}</p>}
        {lastShotResult && (
          <p className="shot-result">
            Résultat: {lastShotResult === "HIT" && "TOUCHÉ !"}
            {lastShotResult === "MISS" && "MANQUÉ."}
            {lastShotResult === "ALREADY_HIT" && "Déjà touché ici."}
            {lastShotResult === "ALREADY_MISS" && "Déjà manqué ici."}
          </p>
        )}
        <p>
          <strong>Tour de {currentPlayer?.name ?? `Joueur ${state.currentPlayerIndex + 1}`}</strong>
          {" — Grille de tir vers "}
          {targetPlayer?.name ?? "adversaire"}
        </p>
        <p>~ = vide (jamais tiré), S = touché, ? = manqué. Cliquez sur une case pour tirer.</p>
        <div className="grid-wrap">
          <table className="grid grid-shoot" cellPadding={0} cellSpacing={0}>
            <thead>
              <tr>
                <th></th>
                {Array.from({ length: gridSize }, (_, i) => (
                  <th key={i}>{i + 1}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {targetGrid.map((row, y) => (
                <tr key={y}>
                  <th>{y + 1}</th>
                  {row.map((cell, x) => {
                    const legend = targetCellToLegend(cell);
                    const canShoot = cell === "O";
                    return (
                      <td key={x}>
                        <button
                          type="button"
                          className={`cell cell-${legend}`}
                          onClick={() => handleShoot(x, y)}
                          disabled={loading || !canShoot}
                          title={`${x + 1}, ${y + 1}`}
                        >
                          {legend}
                        </button>
                      </td>
                    );
                  })}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </main>
    );
  }

  if (state.state === "FINISHED") {
    const winner = state.winnerPlayerIndex != null
      ? state.players?.[state.winnerPlayerIndex]?.name ?? `Joueur ${state.winnerPlayerIndex + 1}`
      : "—";
    return (
      <main className="app">
        <h1>Bataille navale — Partie terminée</h1>
        <p>Gagnant : {winner}</p>
        <p>
          <button type="button" onClick={() => { setState(null); setPlacementCount({ 0: 0, 1: 0 }); setPlacementShips({ 0: [], 1: [] }); }}>
            Nouvelle partie
          </button>
        </p>
      </main>
    );
  }

  return (
    <main className="app">
      <h1>Bataille navale</h1>
      <p>État : {state.state}</p>
    </main>
  );
}
