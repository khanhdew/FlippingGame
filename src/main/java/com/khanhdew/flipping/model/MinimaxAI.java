package com.khanhdew.flipping.model;

import com.khanhdew.flipping.utils.BoardHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MinimaxAI extends AI {
    private final Random random;
    private int maxDepth = 3; // Độ sâu tìm kiếm (điều chỉnh theo hiệu suất)

    // Các hằng số để điều chỉnh tầm quan trọng của các yếu tố khác nhau trong hàm đánh giá
    private static final int CORNER_BONUS = 100;
    private static final int STABLE_PIECE_BONUS = 20;
    private static final int MOBILITY_WEIGHT = 10; // Ưu tiên số lượng nước đi
    private static final int PIECE_DIFFERENCE_WEIGHT = 15; // Ưu tiên số quân cờ

    public MinimaxAI(int id, String name) {
        super(id, name);
        this.random = new Random();
    }

    @Override
    public Piece makeMove(int[][] board) {
        return findBestMove(board, maxDepth);
    }

    private Piece findBestMove(int[][] board, int depth) {
        Piece bestMove = null;
        int bestVal = Integer.MIN_VALUE;
        int aiPlayerId = getPlayerId();

        // 1. Tìm tất cả các nước đi hợp lệ
        List<Piece> legalMoves = findLegalMoves(board, aiPlayerId);

        if (legalMoves.isEmpty()) {
            return null; // Không có nước đi nào cả
        }

        for (Piece move : legalMoves) {
            // 2. Tạo bản sao của bàn cờ (quan trọng!)
            int[][] newBoard = copyBoard(board);

            // 3.  Đặt quân cờ và lật quân cờ
            newBoard[move.row][move.col] = aiPlayerId;
            flipPieces(newBoard, move.row, move.col, aiPlayerId);

            // 4. Đánh giá nước đi này (sử dụng Minimax để ước tính)
            int moveVal = minimax(newBoard, depth - 1, false, Integer.MIN_VALUE, Integer.MAX_VALUE); // Thêm Alpha-Beta

            // 5.  Cập nhật nước đi tốt nhất
            if (moveVal > bestVal) {
                bestVal = moveVal;
                bestMove = move;
            }
        }

        return bestMove;
    }

    // Cắt tỉa Alpha-Beta được thêm vào đây!
    private int minimax(int[][] board, int depth, boolean isMaximizingPlayer, int alpha, int beta) {
        if (depth == 0 || !BoardHelper.canPlay(board, getPlayerId())) {
            return evaluateBoard(board);
        }

        int bestVal;
        int currentPlayerId = isMaximizingPlayer ? getPlayerId() : (getPlayerId() == 1 ? 2 : 1);
        List<Piece> legalMoves = findLegalMoves(board, currentPlayerId);

        if (isMaximizingPlayer) {
            bestVal = Integer.MIN_VALUE;
            for (Piece move : legalMoves) {
                int[][] newBoard = copyBoard(board);
                newBoard[move.row][move.col] = currentPlayerId;
                flipPieces(newBoard, move.row, move.col, currentPlayerId);

                int val = minimax(newBoard, depth - 1, false, alpha, beta);
                bestVal = Math.max(bestVal, val);
                alpha = Math.max(alpha, val); // Cập nhật alpha

                if (beta <= alpha) {
                    break; // Cắt tỉa Beta
                }
            }
            return bestVal;
        } else {
            bestVal = Integer.MAX_VALUE;
            for (Piece move : legalMoves) {
                int[][] newBoard = copyBoard(board);
                newBoard[move.row][move.col] = currentPlayerId;
                flipPieces(newBoard, move.row, move.col, currentPlayerId);

                int val = minimax(newBoard, depth - 1, true, alpha, beta);
                bestVal = Math.min(bestVal, val);
                beta = Math.min(beta, val); // Cập nhật beta

                if (beta <= alpha) {
                    break; // Cắt tỉa Alpha
                }
            }
            return bestVal;
        }
    }

    private void flipPieces(int[][] board, int row, int col, int playerId) {
        // Sử dụng BoardHelper để tìm các quân cờ cần lật
        ArrayList<Piece> piecesToFlip = BoardHelper.getPieceChangeForEachMove(board, playerId, row, col);
        for (Piece piece : piecesToFlip) {
            board[piece.row][piece.col] = playerId;
        }
        board[row][col] = playerId; // Đảm bảo rằng ô hiện tại cũng được lật
    }

    private int evaluateBoard(int[][] board) {
        int aiPlayerId = getPlayerId();
        int opponentId = (aiPlayerId == 1) ? 2 : 1;
        int aiPieces = 0;
        int opponentPieces = 0;

        // Tính số nước đi hợp lệ cho cả hai người chơi
        int aiMobility = findLegalMoves(board, aiPlayerId).size();
        int opponentMobility = findLegalMoves(board, opponentId).size();

        // Đếm số quân cờ và thưởng cho các góc
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                if (board[i][j] == aiPlayerId) {
                    aiPieces++;
                    if (isCorner(i, j, board.length, board[0].length)) {
                        aiPieces += CORNER_BONUS;
                    }
                    //TODO: Thêm logic để thưởng cho các ô "ổn định"
                } else if (board[i][j] == opponentId) {
                    opponentPieces++;
                    //Không phạt đối phương có góc, vì sẽ làm AI tránh góc bằng mọi giá
                    //if (isCorner(i, j, board.length, board[0].length)) {
                    //    opponentPieces += CORNER_BONUS;
                    //}
                }
            }
        }

        // Tính toán sự khác biệt
        int pieceDifference = aiPieces - opponentPieces;
        int mobilityDifference = aiMobility - opponentMobility;

        // Kết hợp các yếu tố (điều chỉnh trọng số!)
        return (pieceDifference * PIECE_DIFFERENCE_WEIGHT) + (mobilityDifference * MOBILITY_WEIGHT);
    }

    private boolean isCorner(int row, int col, int rows, int cols) {
        return (row == 0 && col == 0) || (row == 0 && col == cols - 1) ||
               (row == rows - 1 && col == 0) || (row == rows - 1 && col == cols - 1);
    }

    private int[][] copyBoard(int[][] board) {
        int[][] newBoard = new int[board.length][board[0].length];
        for (int i = 0; i < board.length; i++) {
            System.arraycopy(board[i], 0, newBoard[i], 0, board[0].length);
        }
        return newBoard;
    }


    //*** QUAN TRỌNG: Sửa đổi hàm này để trả về danh sách các NƯỚC ĐI HỢP LỆ THỰC SỰ ***/
    private List<Piece> findLegalMoves(int[][] board, int playerId) {
      return BoardHelper.getAvailableMoves(board,playerId);
    }

    // Phiên bản khác của hàm findAvailableMoves, sử dụng khi đánh giá bàn cờ
    @Override
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

}
