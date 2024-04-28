package com.khanhdew.testjfx.model;

import com.khanhdew.testjfx.utils.BoardHelper;
import com.khanhdew.testjfx.view.MainPane;

import java.util.List;
import java.util.Random;

public class EasyAI extends AI {

    private Random random;

    public EasyAI(int id, String name) {
        super(id, name);
        this.random = new Random();
    }

    public Piece makeMove(int[][] board) {
        List<Piece> availableMoves = findAvailableMoves(board);
        System.out.println("Available moves: " + availableMoves.size());
        if (availableMoves.isEmpty()) {
            // Handle the situation when there are no available moves
            // This could be returning null, throwing an exception, etc.
            return null;
        }
        Piece chosenMove = availableMoves.get(random.nextInt(availableMoves.size()));
        System.out.println("Chosen move: " + chosenMove.row + ", " + chosenMove.col);
        return chosenMove;
    }

}