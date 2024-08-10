package com.khanhdew.flipping.model;

public abstract class Player {
    private int score;
    private int playerId;
    private String name;

    public Player(int playerId, String name) {
        this.playerId = playerId;
        this.score = 0;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
