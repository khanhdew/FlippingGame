package com.khanhdew.flipping.utils;

import com.khanhdew.flipping.model.Piece;
import com.khanhdew.flipping.model.PieceState;

import java.util.ArrayList;


public class BoardHelper {


    public static int[][] toMatrix(int[][] matrix, ArrayList<Piece> pieces) {
        for (Piece piece : pieces) {
            toMatrix(matrix, piece);
        }
        return matrix;
    }

    public static int[][] toMatrix(int[][] matrix, Piece piece) {
        if (piece.getCurrentState() == PieceState.BLACK)
            matrix[piece.getRow()][piece.getCol()] = 1;
        if (piece.getCurrentState() == PieceState.WHITE)
            matrix[piece.getRow()][piece.getCol()] = 2;
        if (piece.getCurrentState() == PieceState.EMPTY)
            matrix[piece.getRow()][piece.getCol()] = 0;
        return matrix;
    }

    public static void printMatrix(int[][] matrix) {
        System.out.println("----------------------");
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                System.out.printf("%2d ", matrix[i][j]);
            }
            System.out.println();
        }
    }

    public static ArrayList<Piece> matrixToPieces(int[][] matrix) {
        ArrayList<Piece> pieces = new ArrayList<>();
        for (int i = 0; i < matrix.length; i++)
            for (int j = 0; j < matrix[0].length; j++) {
                if (matrix[i][j] == 1)
                    pieces.add(new Piece(i, j, PieceState.BLACK));
                else if (matrix[i][j] == 2)
                    pieces.add(new Piece(i, j, PieceState.WHITE));
                else{
                    pieces.add(new Piece(i, j, PieceState.EMPTY));
                }
            }
        return pieces;
    }

    public static void printAllPieces(ArrayList<Piece> pieces) {
        for (Piece piece : pieces) {
            if (piece.getCurrentState() != PieceState.EMPTY)
                System.out.println(piece);
        }
    }

    public static boolean canPlay(int[][] board) {
        int emptyCell = 0;
        for (int[] row : board)
            for (int cell : row)
                if (cell == 0)
                    emptyCell++;
        return emptyCell != 0;
    }

    public static ArrayList<Piece> getAvailableMoves(int[][] board, int playerId) {
        PieceState player = playerId == 1 ? PieceState.BLACK : PieceState.WHITE;
        ArrayList<Piece> moves = new ArrayList<>();
        for (int i = 0; i < board.length; i++)
            for (int j = 0; j < board[0].length; j++)
                if (board[i][j] == 0)
                    moves.add(new Piece(j, i, player));
        return moves;
    }

    public static ArrayList<Piece> getPieceChangeForEachMove(int[][] board, int playerId, int row, int col) {
        int opponent;
        if (playerId == 1)
            opponent = 2;
        else
            opponent = 1;
        PieceState playerState;
        if (playerId == 1)
            playerState = PieceState.BLACK;
        else
            playerState = PieceState.WHITE;
        int BOARD_ROW = board.length;
        int BOARD_COL = board[0].length;
        ArrayList<Piece> changes = new ArrayList<>();
        // row = 0
        if (row == 0) {
            if (col == 0) {
                if (board[row][col + 1] == opponent) {
                    changes.add(new Piece(row, col + 1, playerState));
                }
                if (board[row + 1][col] == opponent) {
                    changes.add(new Piece(row + 1, col, playerState));
                }
                if (board[row + 1][col + 1] == opponent) {
                    changes.add(new Piece(row + 1, col + 1, playerState));
                }
            }
            else if (col == BOARD_COL - 1) {
                if (board[row + 1][col] == opponent) {
                    changes.add(new Piece(row + 1, col, playerState));
                }
                if (board[row + 1][col - 1] == opponent) {
                    changes.add(new Piece(row + 1, col - 1, playerState));
                }
                if (board[row][col - 1] == opponent) {
                    changes.add(new Piece(row, col - 1, playerState));
                }
            }
            else
                for (int i = row; i <= row + 1; i++) {
                    for (int j = col - 1; j <= col + 1; j++) {
                        if(i == row && j == col)
                            continue;
                        if (board[i][j] == opponent) {
                            changes.add(new Piece(i, j, playerState));
                        }
                    }
                }
            return changes;
        }
        // row = BOARD_ROW - 1
        if (row == BOARD_ROW - 1) {
            if (col == 0) {
                if (board[row][col + 1] == opponent) {
                    changes.add(new Piece(row, col + 1, playerState));
                }
                if (board[row - 1][col] == opponent) {
                    changes.add(new Piece(row - 1, col, playerState));
                }
                if (board[row - 1][col + 1] == opponent) {
                    changes.add(new Piece(row - 1, col + 1, playerState));
                }
            } else if (col == BOARD_COL - 1) {
                if (board[row - 1][col] == opponent) {
                    changes.add(new Piece(row - 1, col, playerState));
                }
                if (board[row - 1][col - 1] == opponent) {
                    changes.add(new Piece(row - 1, col - 1, playerState));
                }
                if (board[row][col - 1] == opponent) {
                    changes.add(new Piece(row, col - 1, playerState));
                }
            } else {
                if (board[row][col + 1] == opponent) {
                    changes.add(new Piece(row, col + 1, playerState));
                }
                if (board[row - 1][col] == opponent) {
                    changes.add(new Piece(row - 1, col, playerState));
                }
                if (board[row - 1][col + 1] == opponent) {
                    changes.add(new Piece(row - 1, col + 1, playerState));

                }
                if (board[row][col - 1] == opponent) {
                    changes.add(new Piece(row, col - 1, playerState));

                }
                if (board[row - 1][col - 1] == opponent) {
                    changes.add(new Piece(row - 1, col - 1, playerState));

                }
            }
            return changes;
        }
        // col = 0
        if (col == 0) {
            for (int i = row - 1; i <= row + 1; i++) {
                for (int j = col; j <= col + 1; j++) {
                    if (board[i][j] == opponent) {
                        if(i == row && j == col)
                            continue;
                        changes.add(new Piece(i, j, playerState));
                    }
                }
            }
            return changes;
        }
        // col = BOARD_COL - 1
        if (col == BOARD_COL - 1) {
            for (int i = row - 1; i <= row + 1; i++) {
                for (int j = col - 1; j <= col; j++) {
                    if (board[i][j] == opponent) {
                        if(i == row && j == col)
                            continue;
                        changes.add(new Piece(i, j, playerState));
                    }
                }
            }
            return changes;
        }
        for (int i = row - 1; i <= row + 1; i++) {
            for (int j = col - 1; j <= col + 1; j++) {
                if (board[i][j] == opponent) {
                    if(i == row && j == col)
                            continue;
                    changes.add(new Piece(i, j, playerState));
                }
            }
        }
        return changes;
    }

    public static int[] getScore(int[][] board) {
        int[] score = new int[2];
        for (int i = 0; i < board.length; i++)
            for (int j = 0; j < board[0].length; j++) {
                if (board[i][j] == 1)
                    score[0]++;
                else if (board[i][j] == 2)
                    score[1]++;
            }
        return score;
    }
}
