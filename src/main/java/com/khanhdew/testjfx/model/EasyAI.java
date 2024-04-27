//package com.khanhdew.testjfx.model;
//
//import com.khanhdew.testjfx.utils.BoardHelper;
//
//public class EasyAI extends Player {
//    public EasyAI(int id, String name) {
//        super(id, name);
//    }
//
//
//    private void minimax(int[][] board) {
//        Piece bestMove = null;
//        int bestScore = Integer.MIN_VALUE;
//
//        for (Piece move : BoardHelper.getAvailableMoves(board, getPlayerId())) {
//            int[][] newBoard = board.clone();
//            newBoard.makeMove(move, this.getId());
//            int score = minimax(newBoard, 0, false);
//            if (score > bestScore) {
//                bestScore = score;
//                bestMove = move;
//            }
//        }
//
//        return bestMove;
//    }
//
//    private int minimax(int[][] board, int depth, boolean isMaximizingPlayer) {
//        if (board.isGameOver() || depth == 10) {
//            return evaluateBoard(board);
//        }
//
//        if (isMaximizingPlayer) {
//            int bestScore = Integer.MIN_VALUE;
//            for (Move move : board.getValidMoves()) {
//                Board newBoard = board.clone();
//                newBoard.makeMove(move, this.getId());
//                int score = minimax(newBoard, depth + 1, false);
//                bestScore = Math.max(score, bestScore);
//            }
//            return bestScore;
//        } else {
//            int bestScore = Integer.MAX_VALUE;
//            for (Move move : board.getValidMoves()) {
//                Board newBoard = board.clone();
//                newBoard.makeMove(move, 3 - this.getId());
//                int score = minimax(newBoard, depth + 1, true);
//                bestScore = Math.min(score, bestScore);
//            }
//            return bestScore;
//        }
//    }
//
//    private int evaluateBoard(int[][] board) {
//        // Implement your own board evaluation function here
//        return 0;
//    }
//}
