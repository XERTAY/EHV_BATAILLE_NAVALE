package com.ehv.battleship.controller;

import com.ehv.battleship.model.Coordinate;
import com.ehv.battleship.model.Game;
import com.ehv.battleship.model.GameState;
import com.ehv.battleship.model.Player;
import com.ehv.battleship.model.Ship;
import com.ehv.battleship.model.ShipOrientation;
import com.ehv.battleship.model.ShotResult;

import java.util.List;

public class GameController {

    private final Game game;

    public GameController(Game game) {
        if (game == null) {
            throw new IllegalArgumentException("Le jeu ne peut pas être nul");
        }
        this.game = game;
    }

    public int getGridSize() {
        return game.getGridSize();
    }

    public Player getCurrentPlayer() {
        return game.getCurrentPlayer();
    }

    public Player getTargetPlayer() {
        Player current = game.getCurrentPlayer();
        List<Player> opponents = game.getOpponents(current);
        if (opponents.isEmpty()) {
            throw new IllegalStateException("Aucun adversaire disponible");
        }
        return opponents.get(0);
    }

    public boolean isCoordinateInRange(int x, int y) {
        int size = game.getGridSize();
        return x >= 0 && x < size && y >= 0 && y < size;
    }

    public ShotResult playShot(int x, int y) {
        Player current = getCurrentPlayer();
        Player target = getTargetPlayer();
        Coordinate coordinate = new Coordinate(x, y);
        return game.shoot(current, target, coordinate);
    }

    public void endTurn() {
        game.switchTurn();
    }

    public void placeShip(int x, int y, int size, ShipOrientation orientation, String shipName) {
        placeShipForPlayer(game.getPlayers().indexOf(getCurrentPlayer()), x, y, size, orientation, shipName);
    }

    public void placeShipForPlayer(int playerIndex, int x, int y, int size, ShipOrientation orientation, String shipName) {
        if (playerIndex < 0 || playerIndex >= game.getPlayers().size()) {
            throw new IllegalArgumentException("Index joueur invalide : " + playerIndex);
        }
        Player player = game.getPlayers().get(playerIndex);
        Coordinate startCoord = new Coordinate(x, y);

        if (!isCoordinateInRange(x, y)) {
            throw new IllegalArgumentException("Coordonnées hors de la grille");
        }

        List<Coordinate> coordinates = player.getGrid().generateShipCoordinates(
            startCoord, size, orientation);

        int shipId = Ship.generateId();
        Ship ship = new Ship(shipId, shipName, size, coordinates, orientation);
        game.placeShip(player, ship);

        if (game.getState() == GameState.PLACEMENT
                && player.getFleet().isComplete()
                && !areAllFleetsReady()) {
            game.switchTurn();
        }
    }

    public boolean canPlaceShip(int x, int y, int size, ShipOrientation orientation) {
        return canPlaceShipForPlayer(game.getPlayers().indexOf(getCurrentPlayer()), x, y, size, orientation);
    }

    public boolean canPlaceShipForPlayer(int playerIndex, int x, int y, int size, ShipOrientation orientation) {
        if (playerIndex < 0 || playerIndex >= game.getPlayers().size()) {
            return false;
        }
        Player player = game.getPlayers().get(playerIndex);
        Coordinate startCoord = new Coordinate(x, y);

        if (!isCoordinateInRange(x, y)) {
            return false;
        }

        if (!player.getGrid().canPlaceShip(startCoord, size, orientation)) {
            return false;
        }

        List<Coordinate> coordinates = player.getGrid().generateShipCoordinates(
            startCoord, size, orientation);
        Ship tempShip = new Ship(0, "temp", size, coordinates, orientation);

        return player.getFleet().canAddShip(tempShip);
    }

    public boolean areAllFleetsReady() {
        for (Player player : game.getPlayers()) {
            if (!player.getFleet().isComplete()) {
                return false;
            }
        }
        return true;
    }

    public void startPlacementPhase() {
        game.setState(GameState.PLACEMENT);
    }

    public void finishPlacementPhase() {
        if (!areAllFleetsReady()) {
            throw new IllegalStateException("Toutes les flottes doivent être complètes avant de commencer");
        }
        game.start();
    }

    public Game getGame() {
        return game;
    }
}
