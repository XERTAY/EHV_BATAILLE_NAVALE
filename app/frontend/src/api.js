const API_BASE = '/api';

async function request(path, options = {}) {
  const url = `${API_BASE}${path}`;
  const res = await fetch(url, {
    ...options,
    headers: { 'Content-Type': 'application/json', ...options.headers },
  });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || `HTTP ${res.status}`);
  }
  if (res.status === 204 || res.headers.get('content-length') === '0') return null;
  return res.json();
}

export async function health() {
  return request('/health', { method: 'GET' }).then(() => true).catch(() => false);
}

export async function initGame(gridSize, shipSizes) {
  return request('/game', {
    method: 'POST',
    body: JSON.stringify({ gridSize, shipSizes }),
  });
}

export async function getGameState() {
  return request('/game');
}

export async function canPlaceShip(playerIndex, x, y, size, orientation) {
  const params = new URLSearchParams({ playerIndex, x, y, size, orientation });
  return request(`/game/placement/can?${params}`);
}

export async function placeShip(playerIndex, x, y, size, orientation, shipName) {
  return request('/game/placement/ship', {
    method: 'POST',
    body: JSON.stringify({ playerIndex, x, y, size, orientation, shipName }),
  });
}

export async function isPlacementReady() {
  return request('/game/placement/ready');
}

export async function finishPlacement() {
  return request('/game/placement/finish', { method: 'POST' });
}

export async function shoot(x, y) {
  return request('/game/shoot', {
    method: 'POST',
    body: JSON.stringify({ x, y }),
  });
}
