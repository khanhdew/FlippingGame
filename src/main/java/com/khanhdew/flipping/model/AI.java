package com.khanhdew.flipping.model;

import com.khanhdew.flipping.utils.BoardHelper;

import java.util.ArrayList;

public abstract class AI extends Player{
    public AI(int playerId, String name) {
        super(playerId, name);
    }
    public ArrayList<Piece> findAvailableMoves(int[][] board) {
        return BoardHelper.getAvailableMoves(board, getPlayerId());
    }
    public Piece makeMove(int[][] board) {
        return null;
    }
}
