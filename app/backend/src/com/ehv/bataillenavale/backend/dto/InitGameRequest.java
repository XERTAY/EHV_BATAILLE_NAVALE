package com.ehv.bataillenavale.backend.dto;

import java.util.List;

public class InitGameRequest {
    private int gridSize;
    private List<Integer> shipSizes;

    public int getGridSize() {
        return gridSize;
    }

    public void setGridSize(int gridSize) {
        this.gridSize = gridSize;
    }

    public List<Integer> getShipSizes() {
        return shipSizes;
    }

    public void setShipSizes(List<Integer> shipSizes) {
        this.shipSizes = shipSizes;
    }
}
