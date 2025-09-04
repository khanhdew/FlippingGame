package com.khanhdew.flipping.model;

import java.util.List;
import java.util.Random;

public class EasyAI extends AI {

    private Random random;

    public EasyAI(int id, String name) {
        super(id, name);
        this.random = new Random();
    }

    public Piece bestMove(List<Piece> pieces){
        int index = random.nextInt(pieces.size());
        return pieces.get(index);
    }

    public Piece makeMove(int[][] board) {
        List<Piece> availableMoves = findAvailableMoves(board);
        if (availableMoves.isEmpty()) {
            // Handle the situation when there are no available moves
            // This could be returning null, throwing an exception, etc.
            return null;
        }
        return bestMove(availableMoves);
    }

}