package com.khanhdew.testjfx.utils;

import com.khanhdew.testjfx.model.Piece;
import com.khanhdew.testjfx.model.PieceState;

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
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                System.out.printf("%2d ", matrix[i][j]);
            }
            System.out.println();
        }
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
            if (col == 0)
                for (int i = row; i <= row + 1; i++) {
                    for (int j = col; j <= col + 1; j++) {
                        if (board[i][j] == opponent) {
                            changes.add(new Piece(i, j, playerState));
                            board[i][j] = playerId;
                        }
                    }
                }
            else if (col == BOARD_COL - 1)
                for (int i = row; i <= row + 1; i++) {
                    for (int j = col - 1; j <= col; j++) {
                        if (board[i][j] == opponent) {
                            changes.add(new Piece(i, j, playerState));
                            board[i][j] = playerId;
                        }
                    }
                }
            else for (int i = row;
                      i <= row + 1;
                      i++) {
                    for (int j = col - 1; j <= col + 1; j++) {
                        if (board[i][j] == opponent) {
                            changes.add(new Piece(i, j, playerState));
                            board[i][j] = playerId;
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
                    board[row][col + 1] = playerId;
                }
                if (board[row - 1][col] == opponent) {
                    changes.add(new Piece(row - 1, col, playerState));
                    board[row - 1][col] = playerId;
                }
                if (board[row - 1][col+1] == opponent) {
                    changes.add(new Piece(row - 1, col + 1, playerState));
                    board[row - 1][col+1] = playerId;
                }
            } else if (col == BOARD_COL - 1) {
                if (board[row-1][col] == opponent) {
                    changes.add(new Piece(row-1, col, playerState));
                    board[row-1][col] = playerId;
                }
                if (board[row - 1][col-1] == opponent) {
                    changes.add(new Piece(row - 1, col-1, playerState));
                    board[row - 1][col-1] = playerId;
                }
                if (board[row][col-1] == opponent) {
                    changes.add(new Piece(row, col - 1, playerState));
                    board[row][col-1] = playerId;
                }
            } else {
                if (board[row][col + 1] == opponent) {
                    changes.add(new Piece(row, col + 1, playerState));
                    board[row][col + 1] = playerId;
                }
                if (board[row - 1][col] == opponent) {
                    changes.add(new Piece(row - 1, col, playerState));
                    board[row - 1][col] = playerId;
                }
                if (board[row - 1][col+1] == opponent) {
                    changes.add(new Piece(row - 1, col + 1, playerState));
                    board[row - 1][col+1] = playerId;
                }
                if (board[row][col - 1] == opponent) {
                    changes.add(new Piece(row, col - 1, playerState));
                    board[row][col - 1] = playerId;
                }
                if (board[row - 1][col-1] == opponent) {
                    changes.add(new Piece(row - 1, col-1, playerState));
                    board[row - 1][col-1] = playerId;
                }
            }
            return changes;
        }
        // col = 0
        if (col == 0) {
            for (int i = row - 1; i <= row + 1; i++) {
                for (int j = col; j <= col + 1; j++) {
                    if (board[i][j] == opponent) {
                        changes.add(new Piece(i, j, playerState));
                        board[i][j] = playerId;
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
                        changes.add(new Piece(i, j, playerState));
                        board[i][j] = playerId;
                    }
                }
            }
            return changes;
        }
        for (int i = row - 1; i <= row + 1; i++) {
            for (int j = col - 1; j <= col + 1; j++) {
                if (board[i][j] == opponent) {
                    changes.add(new Piece(i, j, playerState));
                    board[i][j] = playerId;
                }
            }
        }
        return changes;
    }

    public static int getScore(int[][] board, int playerId) {
        int score = 0;
        for (int i = 0; i < board.length; i++)
            for (int j = 0; j < board[0].length; j++)
                if (board[i][j] == playerId)
                    score++;
        return score;
    }
}
