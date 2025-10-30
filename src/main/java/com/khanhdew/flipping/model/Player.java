package com.khanhdew.flipping.model;

public abstract class Player {
    private int score;
    private int playerId;
    private String name;
    private long time;
    private final double TIME_LIMIT_IN_MINUTE = 30;

    public Player(int playerId, String name) {
        this.playerId = playerId;
        this.score = 0;
        this.name = name;
        time = (int) TIME_LIMIT_IN_MINUTE * 60 * 1000;
    }

    public static Player createPlayer(int playerId, String name, String type) {
        switch (type) {
            case "easyai":
                return new EasyAI(playerId, name);
            case "human":
                return new HumanPlayer(playerId, name);
            case "minimaxai":
                return new MinimaxAI(playerId, name);
            default:
                return null;
        }
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

    public long getTime() {
        return time;
    }

    public void tick() {
        time -= 1000;
    }

    public void reset() {
        this.score = 0;
        this.time = (int) TIME_LIMIT_IN_MINUTE * 60 * 1000;
    }
}
