package com.ehv.bataillenavale.backend;

import com.ehv.battleship.controller.GameController;
import com.ehv.battleship.model.Game;
import org.springframework.stereotype.Component;

@Component
public class GameHolder {
    private Game game;
    private GameController gameController;

    public Game getGame() {
        return game;
    }

    public GameController getGameController() {
        return gameController;
    }

    public void setGame(Game game) {
        this.game = game;
        this.gameController = game == null ? null : new GameController(game);
    }

    public boolean hasGame() {
        return game != null;
    }

    public void clear() {
        this.game = null;
        this.gameController = null;
    }
}
