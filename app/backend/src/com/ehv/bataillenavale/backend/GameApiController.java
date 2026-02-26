package com.ehv.bataillenavale.backend;

import com.ehv.bataillenavale.backend.dto.*;
import com.ehv.battleship.model.Game;
import com.ehv.battleship.model.GameState;
import com.ehv.battleship.model.Player;
import com.ehv.battleship.model.ShipOrientation;
import com.ehv.battleship.model.ShotResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api")
public class GameApiController {

    private final GameHolder gameHolder;

    public GameApiController(GameHolder gameHolder) {
        this.gameHolder = gameHolder;
    }

    @GetMapping("/health")
    public String health() {
        return "ok";
    }

    @PostMapping("/game")
    public ResponseEntity<GameStateResponse> initGame(@RequestBody InitGameRequest request) {
        if (request.getGridSize() < 5) {
            throw new IllegalArgumentException("La taille de grille doit être au moins 5");
        }
        if (request.getShipSizes() == null || request.getShipSizes().isEmpty()) {
            throw new IllegalArgumentException("La flotte doit contenir au moins un navire");
        }
        int gridSize = request.getGridSize();
        List<Integer> shipSizes = request.getShipSizes();
        List<Player> players = Arrays.asList(
            new Player("Joueur 1", gridSize, shipSizes),
            new Player("Joueur 2", gridSize, shipSizes)
        );
        Game game = new Game(gridSize, players);
        gameHolder.setGame(game);
        gameHolder.getGameController().startPlacementPhase();
        return ResponseEntity.status(HttpStatus.CREATED).body(buildGameStateResponse());
    }

    @GetMapping("/game")
    public ResponseEntity<GameStateResponse> getGameState() {
        if (!gameHolder.hasGame()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(buildGameStateResponse());
    }

    @GetMapping("/game/placement/can")
    public ResponseEntity<Boolean> canPlaceShip(
            @RequestParam("playerIndex") int playerIndex,
            @RequestParam("x") int x,
            @RequestParam("y") int y,
            @RequestParam("size") int size,
            @RequestParam("orientation") String orientation) {
        if (!gameHolder.hasGame()) {
            return ResponseEntity.notFound().build();
        }
        ShipOrientation orient = parseOrientation(orientation);
        if (orient == null) {
            return ResponseEntity.badRequest().build();
        }
        boolean can = gameHolder.getGameController().canPlaceShipForPlayer(playerIndex, x, y, size, orient);
        return ResponseEntity.ok(can);
    }

    @PostMapping("/game/placement/ship")
    public ResponseEntity<GameStateResponse> placeShip(@RequestBody PlaceShipRequest request) {
        if (!gameHolder.hasGame()) {
            return ResponseEntity.notFound().build();
        }
        ShipOrientation orient = parseOrientation(request.getOrientation());
        if (orient == null) {
            throw new IllegalArgumentException("Orientation invalide. Utilisez H, -H, V ou -V.");
        }
        String shipName = request.getShipName() != null ? request.getShipName() : "Navire";
        gameHolder.getGameController().placeShipForPlayer(
            request.getPlayerIndex(),
            request.getX(),
            request.getY(),
            request.getSize(),
            orient,
            shipName
        );
        return ResponseEntity.ok(buildGameStateResponse());
    }

    @GetMapping("/game/placement/ready")
    public ResponseEntity<Boolean> isPlacementReady() {
        if (!gameHolder.hasGame()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(gameHolder.getGameController().areAllFleetsReady());
    }

    @PostMapping("/game/placement/finish")
    public ResponseEntity<GameStateResponse> finishPlacement() {
        if (!gameHolder.hasGame()) {
            return ResponseEntity.notFound().build();
        }
        gameHolder.getGameController().finishPlacementPhase();
        return ResponseEntity.ok(buildGameStateResponse());
    }

    @PostMapping("/game/shoot")
    public ResponseEntity<ShootResponse> shoot(@RequestBody ShootRequest request) {
        if (!gameHolder.hasGame()) {
            return ResponseEntity.notFound().build();
        }
        if (!gameHolder.getGameController().isCoordinateInRange(request.getX(), request.getY())) {
            throw new IllegalArgumentException("Coordonnées hors de la grille");
        }
        ShotResult result = gameHolder.getGameController().playShot(request.getX(), request.getY());
        gameHolder.getGameController().endTurn();
        return ResponseEntity.ok(new ShootResponse(result.name()));
    }

    private static ShipOrientation parseOrientation(String s) {
        if (s == null) return null;
        switch (s.toUpperCase()) {
            case "H": return ShipOrientation.HORIZONTAL;
            case "-H": return ShipOrientation.HORIZONTAL_LEFT;
            case "V": return ShipOrientation.VERTICAL;
            case "-V": return ShipOrientation.VERTICAL_UP;
            default: return null;
        }
    }

    private GameStateResponse buildGameStateResponse() {
        Game game = gameHolder.getGame();
        GameStateResponse resp = new GameStateResponse();
        resp.setState(game.getState().name());
        resp.setGridSize(game.getGridSize());
        resp.setCurrentPlayerIndex(game.getPlayers().indexOf(gameHolder.getGameController().getCurrentPlayer()));
        if (game.isFinished() && game.getWinner() != null) {
            resp.setWinnerPlayerIndex(game.getPlayers().indexOf(game.getWinner()));
        } else {
            resp.setWinnerPlayerIndex(null);
        }
        List<PlayerInfo> players = new ArrayList<>();
        for (int i = 0; i < game.getPlayers().size(); i++) {
            Player p = game.getPlayers().get(i);
            PlayerInfo info = new PlayerInfo();
            info.setIndex(i);
            info.setId(p.getId());
            info.setName(p.getName());
            info.setFleetComplete(p.getFleet().isComplete());
            info.setHasLost(p.hasLost());
            info.setTargetGridView(p.getGrid().toTargetViewString());
            players.add(info);
        }
        resp.setPlayers(players);
        return resp;
    }
}
