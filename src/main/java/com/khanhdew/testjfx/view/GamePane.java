package com.khanhdew.testjfx.view;

import com.khanhdew.testjfx.model.*;
import com.khanhdew.testjfx.utils.BoardHelper;
import com.khanhdew.testjfx.utils.Mouse;
import javafx.animation.AnimationTimer;


import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;


import java.util.ArrayList;


public class GamePane extends BorderPane {
    public static int WIDTH;
    public static int HEIGHT;

    private BorderPane scorePane;
    BorderPane gamePane;

    private Scene gameScene;
    private Scene scoreScene;
    public Canvas c1;


    Board board = new Board();
    Mouse mouse = new Mouse();

    Player p1 = new HumanPlayer(1, "Player 1");
    Player p2 = new HumanPlayer(2, "Player 2");
    int turn = 1;


    public static ArrayList<Piece> pieces = new ArrayList<>();
    public static int[][] matrix = new int[Board.MAX_ROW][Board.MAX_COL];

    public GamePane() {
        calDimension();
        scorePane = new BorderPane();
        scoreScene = new Scene(scorePane, 300, 800);
        scoreScene.setFill(Color.BLACK);
        gamePane = new BorderPane();
        gameScene = new Scene(gamePane, 800, 800);


        addEventHandler(MouseEvent.MOUSE_CLICKED, mouse);

        //draw GamePane
        c1 = new Canvas(800, 800);
        Board.draw(c1.getGraphicsContext2D());

        setPieces();
        gamePane.getChildren().add(c1);
        for (Piece piece : pieces) {
            gamePane.getChildren().add(piece);
        }

        // Add gamePane and scorePane to different positions in GamePane
        this.setCenter(gamePane);
        this.setRight(scorePane);

        // Player timer


        new AnimationTimer() {
            long lastTick = 0;

            @Override
            public void handle(long now) {
                if (lastTick == 0) {
                    lastTick = now;
                    update();

                    showScore();
                    return;
                }
                if (now - lastTick > 1000000000 / 60) {
                    lastTick = now;
                    update();

                    showScore();
                }
            }
        }.start();
    }

    public void setPieces() {
        int BOARD_COL = Board.MAX_COL;
        int BOARD_ROW = Board.MAX_ROW;
        for (int i = 0; i < BOARD_ROW; i++) {
            for (int j = 0; j < BOARD_COL; j++) {
                pieces.add(new Piece(i, j, PieceState.EMPTY));
            }
        }
//        for (Piece piece : pieces) {
//            if (piece.col == 0 || (piece.row == BOARD_ROW - 1 && piece.col != BOARD_COL - 1))
//                piece.setState(PieceState.BLACK);
//            if (piece.col == BOARD_COL - 1 || (piece.row == 0 && piece.col != 0)) piece.setState(PieceState.WHITE);
//        }

        BoardHelper.toMatrix(matrix, pieces);
    }

    public void resetBoard() {
        pieces.clear();
        setPieces();
    }

    public void update() {
        for (Piece piece : pieces) {
            piece.updateState(piece.getCurrentState());
//            piece.setOnMouseEntered(e -> {
//
//            });
        }
        if (mouse.mouseClicked && mouse.x > 0 && mouse.y > 0) {
            if (move(mouse.getCol(), mouse.getRow(), turn))
                manageTurn();
            mouse.mouseClicked = false;
        }
        showScore();
    }

    public boolean move(int col, int row, int playerId) {
        LOOP:
        for (Piece piece : pieces) {
            if (piece.getCol() == col && piece.getRow() == row) {
                if (piece.getCurrentState() == PieceState.EMPTY) {
                    piece.setState(playerId == 1 ? PieceState.BLACK : PieceState.WHITE);
                    changePieceState(BoardHelper.getPieceChangeForEachMove(matrix, playerId, row, col));
                    BoardHelper.toMatrix(matrix, piece);
                    //BoardHelper.printMatrix(matrix);
                    return true;
                }
            }
        }
        return false;
    }

    public void changePieceState(ArrayList<Piece> changes) {
//        for(Piece piece : changes){
//            System.out.println(piece);
//        }
        for (Piece piece : changes) {
            for (Piece p : pieces) {
                if (p.getCol() == piece.getCol() && p.getRow() == piece.getRow()) {
                    p.setState(piece.getCurrentState());
                }
            }
        }
    }

    public void manageTurn() {
        if (!BoardHelper.canPlay(matrix)) {
            setScore();
            DialogPane dialogPane = new DialogPane();
            if (p1.getScore() > p2.getScore()) {
                dialogPane.setContentText("Player 1 win");
                Button button = new Button("OK");
                dialogPane.getChildren().add(button);
                gamePane.getChildren().add(dialogPane);

            } else if (p1.getScore() < p2.getScore()) {
                dialogPane.setContentText("Player 2 win");
                Button button = new Button("OK");
                dialogPane.getChildren().add(button);
                gamePane.getChildren().add(dialogPane);
            } else {
                dialogPane.setContentText("Draw");
                Button button = new Button("OK");
                dialogPane.getChildren().add(button);
                gamePane.getChildren().add(dialogPane);
            }
        } else {
            if (turn == 1) {
                setScore();
                turn = 2;
            } else {
                setScore();
                turn = 1;
            }
        }
    }

    public void setScore() {
        p1.setScore(BoardHelper.getScore(matrix, 1));
        p2.setScore(BoardHelper.getScore(matrix, 2));
    }

    public void showScore() {
        HBox pane = new HBox(3);

        //Circle
        LinearGradient gradient1 = new LinearGradient(0, 0, 1, 0, true, // proportional
                javafx.scene.paint.CycleMethod.NO_CYCLE, // cycle colors
                new Stop(0, Color.web("#3a3a3a")), new Stop(1, Color.web("#111")));
        Circle c1 = new Circle(50, 50, 50);
        c1.setFill(gradient1);
        c1.setStroke(Color.web("#000"));
        c1.setStrokeWidth(5);
        c1.setStrokeType(StrokeType.INSIDE);
        LinearGradient gradient2 = new LinearGradient(0, 0, 1, 0, true, // proportional
                javafx.scene.paint.CycleMethod.NO_CYCLE, // cycle colors
                new Stop(0, Color.web("#aaa")), new Stop(1, Color.web("#eee")));
        Circle c2 = new Circle(0, 0, 50);
        c2.setFill(gradient2);
        c2.setStroke(Color.web("#bababa"));
        c2.setStrokeWidth(5);
        c2.setStrokeType(StrokeType.INSIDE);

        Text verSus = new Text("VS");
        verSus.setStyle("-fx-font: 24 arial;" +
                "-fx-font-style: italic;" +
                "-fx-font-weight:bolder;" +
                "-fx-fill: black;"
        );

        //TextScore on Circle
        StackPane s1 = new StackPane();
        Text score1 = new Text(p1.getScore() + "");
        score1.setStyle("-fx-font: 24 arial;" +
                "-fx-font-weight:bolder;" +
                "-fx-fill: white;");
        s1.getChildren().add(c1);
        s1.getChildren().add(score1);
        StackPane s2 = new StackPane();
        Text score2 = new Text(p2.getScore() + "");
        score2.setStyle("-fx-font: 24 arial;" +
                "-fx-font-weight:bold;" +
                "-fx-fill: black;");
        s2.getChildren().add(c2);
        s2.getChildren().add(score2);

        //Set Position
        pane.setPrefHeight(300);
        pane.setPrefWidth(300);
        pane.setAlignment(Pos.CENTER);
        pane.getChildren().addAll(s1, verSus, s2);
        pane.setPadding(new Insets(-100, 10, 10, 10));
        pane.setSpacing(20);
        pane.setFillHeight(true);
        scorePane.setTop(pane);
    }
    public void showBtn(){

    }

    public void calDimension() {
        if (Board.MAX_ROW > Board.MAX_COL) {
            WIDTH = (int) (1 / Board.SCALE * 100 * Math.min(Board.MAX_COL, Board.MAX_ROW) + 300);
            HEIGHT = (int) (1 / Board.SCALE * 100 * Math.max(Board.MAX_COL, Board.MAX_ROW));
        } else {
            WIDTH = (int) (1 / Board.SCALE * 100 * Math.max(Board.MAX_COL, Board.MAX_ROW) + 300);
            HEIGHT = (int) (1 / Board.SCALE * 100 * Math.min(Board.MAX_COL, Board.MAX_ROW));
        }
    }
}
