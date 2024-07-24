package com.khanhdew.testjfx.utils;

import com.khanhdew.testjfx.model.Board;
import com.khanhdew.testjfx.model.Piece;
import com.khanhdew.testjfx.view.MainPane;
import javafx.scene.canvas.Canvas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DAO {
    static Connection connection = DbConnector.getInstance().getConnection();

    public static void saveGame(MainPane mainPane) {
        int[][] matrix = mainPane.getMatrix();
        String matrixString = matrixToString(matrix);
        String query = "INSERT INTO game (width, height, board, player1,  player2, turn) VALUES (?,?, ?, ?, ?, ?)";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(3, matrixString);
            preparedStatement.setInt(2, matrix.length);
            preparedStatement.setInt(1, matrix[0].length);
            preparedStatement.setInt(4, mainPane.getP1().getPlayerId());
            preparedStatement.setInt(5, mainPane.getP2().getPlayerId());
            preparedStatement.setInt(6, mainPane.getTurn());
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String matrixToString(int[][] matrix) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                sb.append(matrix[i][j]);
            }
        }
        return sb.toString();
    }

    public static List<MainPane> loadGame(MainPane mainPane) {
        List<MainPane> games = new ArrayList<>();
        String query = "SELECT * FROM game inner join player on game.id = player.id WHERE player1 = ? AND player2 = ?";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, mainPane.getP1().getPlayerId());
            preparedStatement.setInt(2, mainPane.getP2().getPlayerId());
            preparedStatement.executeQuery();
            while (preparedStatement.getResultSet().next()) {
                MainPane temp = new MainPane();
                int rows = preparedStatement.getResultSet().getInt("height");
                int cols = preparedStatement.getResultSet().getInt("width");
                String matrixString = preparedStatement.getResultSet().getString("board");
                int[][] matrix = stringToMatrix(matrixString, rows, cols);
                temp.setBoard(rows, cols);
                temp.getP1().setPlayerId(preparedStatement.getResultSet().getInt("player1"));
                temp.getP2().setPlayerId(preparedStatement.getResultSet().getInt("player2"));
                temp.setMatrix(matrix);
                temp.setTurn(preparedStatement.getResultSet().getInt("turn"));
                games.add(temp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return games;
    }

    public static void loadLastSave(MainPane mainPane) {
        String query = "SELECT * FROM game WHERE player1 = ? AND player2 = ? ORDER BY id DESC LIMIT 1";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, mainPane.getP1().getPlayerId());
            preparedStatement.setInt(2, mainPane.getP2().getPlayerId());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int rows = resultSet.getInt("height");
                    int cols = resultSet.getInt("width");
                    String matrixString = resultSet.getString("board");
                    int[][] matrix = stringToMatrix(matrixString, rows, cols);
                    BoardHelper.printMatrix(matrix);
                    ArrayList<Piece> pieces = BoardHelper.matrixToPieces(matrix);
                    mainPane.setBoard(rows, cols);
                    mainPane.getP1().setPlayerId(resultSet.getInt("player1"));
                    mainPane.getP2().setPlayerId(resultSet.getInt("player2"));
                    mainPane.setMatrix(matrix);
                    mainPane.setTurn(resultSet.getInt("turn"));
                    mainPane.setPieces(pieces);
                    mainPane.getGamePane().getChildren().clear();
                    mainPane.setC1(new Canvas(800, 800));
                    mainPane.getGamePane().getChildren().add(mainPane.getC1());
                    for (Piece piece : pieces) {
                        piece.setPieceSize(Board.getSquareSize());
                        mainPane.getGamePane().getChildren().add(piece);
                    }
                    Board.draw(mainPane.getC1().getGraphicsContext2D());
                    mainPane.setScore();
                    mainPane.showScore();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static int[][] stringToMatrix(String matrixString, int rows, int cols) {
        int[][] matrix = new int[rows][cols];
        int index = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = matrixString.charAt(index) - '0';
                index++;
            }
        }
        return matrix;
    }

}
