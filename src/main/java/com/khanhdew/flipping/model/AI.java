package com.khanhdew.flipping.model;

import java.util.ArrayList;

public abstract class AI extends Player{
    public AI(int playerId, String name) {
        super(playerId, name);
    }
    public ArrayList<Piece> findAvailableMoves(int[][] board) {
        ArrayList<Piece> moves = new ArrayList<>();
        for(int i = 0; i < board.length; i++) {
            for(int j = 0; j < board[0].length; j++) {
                if(board[i][j] == 0) {
                    moves.add(new Piece(i, j, PieceState.EMPTY));
                }
            }
        }
        return moves;
    }
    public Piece makeMove(int[][] board) {
        return null;
    }
}
