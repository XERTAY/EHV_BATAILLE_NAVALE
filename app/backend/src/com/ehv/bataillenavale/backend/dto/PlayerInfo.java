package com.ehv.bataillenavale.backend.dto;

public class PlayerInfo {
    private int index;
    private int id;
    private String name;
    private boolean fleetComplete;
    private boolean hasLost;
    private String targetGridView;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isFleetComplete() {
        return fleetComplete;
    }

    public void setFleetComplete(boolean fleetComplete) {
        this.fleetComplete = fleetComplete;
    }

    public boolean isHasLost() {
        return hasLost;
    }

    public void setHasLost(boolean hasLost) {
        this.hasLost = hasLost;
    }

    public String getTargetGridView() {
        return targetGridView;
    }

    public void setTargetGridView(String targetGridView) {
        this.targetGridView = targetGridView;
    }
}
