package com.ehv.battleship.model;

import java.util.List;

public class Player {

    private static int nextId = 1;
    private final int id;
    private final String name;
    private final Grid grid;
    private final Fleet fleet;
    private boolean ready;
    private final AI ai;

    public Player(String name, int gridSize) {
        this(name, gridSize, null, null);
    }

    public Player(String name, int gridSize, List<Integer> fleetShipSizes) {
        this(name, gridSize, null, fleetShipSizes);
    }

    public Player(String name, int gridSize, AI ai) {
        this(name, gridSize, ai, null);
    }

    public Player(String name, int gridSize, AI ai, List<Integer> fleetShipSizes) {
        this.id = nextId++;
        this.name = name;
        this.grid = new Grid(gridSize);
        if (fleetShipSizes == null) {
            this.fleet = new Fleet();
        } else {
            this.fleet = new Fleet(fleetShipSizes);
        }
        this.ready = false;
        this.ai = ai;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Grid getGrid() {
        return grid;
    }

    public Fleet getFleet() {
        return fleet;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public boolean hasLost() {
        return fleet.areAllShipsSunk();
    }

    public boolean isAI() {
        return ai != null;
    }

    public AI getAI() {
        return ai;
    }
}
