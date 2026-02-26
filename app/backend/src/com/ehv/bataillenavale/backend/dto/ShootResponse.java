package com.ehv.bataillenavale.backend.dto;

public class ShootResponse {
    private String result;

    public ShootResponse(String result) {
        this.result = result;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
