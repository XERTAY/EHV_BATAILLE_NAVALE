package com.ehv.bataillenavale.backend.dto;

import java.util.List;

public class GameStateResponse {
    private String state;
    private int gridSize;
    private Integer currentPlayerIndex;
    private Integer winnerPlayerIndex;
    private List<PlayerInfo> players;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getGridSize() {
        return gridSize;
    }

    public void setGridSize(int gridSize) {
        this.gridSize = gridSize;
    }

    public Integer getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public void setCurrentPlayerIndex(Integer currentPlayerIndex) {
        this.currentPlayerIndex = currentPlayerIndex;
    }

    public Integer getWinnerPlayerIndex() {
        return winnerPlayerIndex;
    }

    public void setWinnerPlayerIndex(Integer winnerPlayerIndex) {
        this.winnerPlayerIndex = winnerPlayerIndex;
    }

    public List<PlayerInfo> getPlayers() {
        return players;
    }

    public void setPlayers(List<PlayerInfo> players) {
        this.players = players;
    }
}
