package com.khanhdew.flipping.model;

import com.khanhdew.flipping.utils.BoardHelper;

import java.util.List;
import java.util.Random;

public class EasyAI extends AI {

    private Random random;

    public EasyAI(int id, String name) {
        super(id, name);
        this.random = new Random();
    }

    public Piece bestMove(List<Piece> pieces, int[][] board) {
        int index = 0;
        int maxScore = Integer.MIN_VALUE;
        for (int i = 0; i < pieces.size(); i++) {
            Piece piece = pieces.get(i);
            int score = BoardHelper.getPieceChangeForEachMove(board, getPlayerId(), piece.getRow(), piece.getRow()).size();
            if (score > maxScore) {
                maxScore = score;
                index = i;
            }
        }
        return pieces.get(index);
    }

    public Piece makeMove(int[][] board) {
        List<Piece> availableMoves = findAvailableMoves(board);
        if (availableMoves.isEmpty()) {
            return null;
        }
        return bestMove(availableMoves, board);
    }

}