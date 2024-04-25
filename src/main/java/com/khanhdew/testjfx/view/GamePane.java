package com.khanhdew.testjfx.view;

import com.khanhdew.testjfx.model.*;
import com.khanhdew.testjfx.utils.BoardHelper;
import com.khanhdew.testjfx.utils.Mouse;
import javafx.animation.AnimationTimer;


import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;


import java.io.IOException;
import java.util.ArrayList;


public class GamePane extends BorderPane {
    public static int WIDTH;
    public static int HEIGHT;

    private BorderPane scorePane;
    BorderPane gamePane;

    private Scene gameScene;
    private Scene scoreScene;
    public Canvas c1;


    Board board;
    Mouse mouse = new Mouse();

    Player p1;
    Player p2;
    int turn = 1;


    public static ArrayList<Piece> pieces = new ArrayList<>();
    public static int[][] matrix;

    public GamePane(int row, int col) {
        board = new Board(row, col);
        matrix = new int[Board.getMaxRow()][Board.getMaxCol()];
        calDimension();
        p1 = new HumanPlayer(1, "Player 1");
        p2 = new HumanPlayer(2, "Player 2");
        scorePane = new BorderPane();
        scoreScene = new Scene(scorePane, 300, 800);
        scoreScene.setFill(Color.BLACK);
        gamePane = new BorderPane();
        gameScene = new Scene(gamePane, 800, 800);


        gamePane.addEventHandler(MouseEvent.MOUSE_CLICKED, mouse);

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
        showBtn();
        showMenu();


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
        int BOARD_COL = Board.getMaxCol();
        int BOARD_ROW = Board.getMaxRow();
        for (int i = 0; i < BOARD_ROW; i++) {
            for (int j = 0; j < BOARD_COL; j++) {
                Piece piece = new Piece(i, j, PieceState.EMPTY);
//                piece.setOnMouseEntered(e -> {
//                    System.out.println("Entered" + piece);
//                    highlightSurroundingPieces(getTurn(), BoardHelper.getPieceChangeForEachMove(matrix, getTurn(), piece.getRow(), piece.getCol()));
//                });
//                piece.setOnMouseExited(e -> {
//                    System.out.println("Exited" + piece);
//                    unhighlightSurroundingPieces(getTurn(), BoardHelper.getPieceChangeForEachMove(matrix, getTurn(), piece.getRow(), piece.getCol()));
//                });
                pieces.add(piece);
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
        gamePane.getChildren().clear();
        gamePane.getChildren().add(c1);
        pieces.clear();
        setPieces();
        for (Piece piece : pieces) {
            gamePane.getChildren().add(piece);
        }
        p1.setScore(0);
        p2.setScore(0);
        turn = 1;
    }

    public void update() {
        for (Piece piece : pieces) {
            piece.updateState(piece.getCurrentState());
        }
        if (mouse.mouseClicked && mouse.x > 0 && mouse.y > 0) {
            if (move(mouse.getCol(), mouse.getRow(), turn))
                manageTurn();
            mouse.mouseClicked = false;
        }
        showScore();
    }

    private int getTurn() {
        return turn;
    }

    public boolean move(int col, int row, int playerId) {
        for (Piece piece : pieces) {
            if (piece.getCol() == col && piece.getRow() == row) {
                if (piece.getCurrentState() == PieceState.EMPTY) {
                    piece.setState(playerId == 1 ? PieceState.BLACK : PieceState.WHITE);
                    changePieceState(BoardHelper.getPieceChangeForEachMove(matrix, playerId, row, col), playerId);
                    BoardHelper.toMatrix(matrix, piece);
                    //BoardHelper.printMatrix(matrix);
                    return true;
                }
            }
        }
        return false;
    }

    public void changePieceState(ArrayList<Piece> changes, int playerId) {
//        for(Piece piece : changes){
//            System.out.println(piece);
//        }
        for (Piece piece : changes) {
            for (Piece p : pieces) {
                if (p.getCol() == piece.getCol() && p.getRow() == piece.getRow()) {
                    p.setState(piece.getCurrentState());
                    matrix[p.row][p.col] = playerId;
                }
            }
        }
    }

    public void manageTurn() {
        if (!BoardHelper.canPlay(matrix)) {
            setScore();
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Game Over");
                if (p1.getScore() > p2.getScore()) {
                    alert.setHeaderText("Player 1 Wins!");
                    alert.setContentText("Player 1 has won the game with a score of " + p1.getScore());
                } else if (p1.getScore() < p2.getScore()) {
                    alert.setHeaderText("Player 2 Wins!");
                    alert.setContentText("Player 2 has won the game with a score of " + p2.getScore());
                } else {
                    alert.setHeaderText("Draw!");
                    alert.setContentText("The game is a draw with both players scoring " + p1.getScore());
                }
                alert.showAndWait();
            });
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


    public void highlightSurroundingPieces(int playerId, ArrayList<Piece> changes) {
        for (Piece piece : changes) {
            for (Piece p : pieces) {
                if (p.getRow() == piece.getRow() && p.getCol() == piece.getCol() && p.getCurrentState() != PieceState.EMPTY) {
                    if (p.getCurrentState() == PieceState.EMPTY) {
                        p.setHighlight(true);
                        p.updateState(p.getCurrentState());
                    }
                    if (playerId == 1 && p.getCurrentState() == PieceState.WHITE) {
                        p.setHighlight(true);
                        p.updateState(p.getCurrentState());
                    }
                    if (playerId == 2 && p.getCurrentState() == PieceState.BLACK) {
                        p.setHighlight(true);
                        p.updateState(p.getCurrentState());
                    }
                }
            }
        }
    }

    public void unhighlightSurroundingPieces(int playerId, ArrayList<Piece> changes) {
        for (Piece piece : changes) {
            for (Piece p : pieces) {
                if (p.getRow() == piece.getRow() && p.getCol() == piece.getCol()) {
                    p.setHighlight(false);
                    p.updateState(p.getCurrentState());
                }
            }
        }
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

    public void showBtn() {
        VBox pane = new VBox();
        pane.setAlignment(Pos.TOP_CENTER);
        pane.setSpacing(10);
        Button reset = new Button("New Game");
        reset.setOnAction(e -> {
            resetBoard();
        });
        Button exit = new Button("Exit");
        exit.setOnAction(e -> {
            Platform.exit();
        });


        Label row = new Label("Row");
        TextField rowField = new TextField();
        rowField.setPrefWidth(50);
        Label col = new Label("Col");
        TextField colField = new TextField();
        colField.setPrefWidth(50);

        Label player1 = new Label("Player 1");
        ComboBox player1Field = new ComboBox();
        player1Field.getItems().addAll("Human", "EasyAI");
        player1Field.setValue("Human");
        Label player2 = new Label("Player 2");
        ComboBox player2Field = new ComboBox();
        player2Field.getItems().addAll("Human", "EasyAI");
        player2Field.setValue("Human");

        HBox gameSizeSettingPane = new HBox();
        gameSizeSettingPane.setAlignment(Pos.CENTER);
        gameSizeSettingPane.getChildren().addAll(row, rowField, col, colField);
        gameSizeSettingPane.setSpacing(10);
        gameSizeSettingPane.setStyle("-fx-end-margin: 50px");
        gameSizeSettingPane.setPrefSize(pane.getWidth(),pane.getHeight());
        pane.getChildren().addAll(gameSizeSettingPane,player1,player1Field,player2,player2Field,reset,exit);

        scorePane.setCenter(pane);
    }

    public void showMenu() {
        Menu gameMenu = new Menu("Game");
        MenuItem newItem = new MenuItem("New game");
        newItem.setOnAction(e -> {
            resetBoard();
        });
        gameMenu.getItems().add(newItem);
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> {
            Platform.exit();
        });
        gameMenu.getItems().add(exitItem);

        Menu helpMenu = new Menu("Help");
        MenuItem aboutItem = new MenuItem("About");
        aboutItem.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("About");
            alert.setHeaderText("Flipping Game");
            alert.setContentText("This is a simple Flipping game developed by Khanh Dew");
            alert.showAndWait();
        });
        helpMenu.getItems().add(aboutItem);
        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().add(gameMenu);
        menuBar.getMenus().add(helpMenu);
        this.setTop(menuBar);
    }

    public void calDimension() {
        if (Board.getMaxRow() > Board.getMaxCol()) {
            WIDTH = (int) (1 / Board.getSCALE() * 100 * Math.min(Board.getMaxCol(), Board.getMaxRow()) + 300);
            HEIGHT = (int) (1 / Board.getSCALE() * 100 * Math.max(Board.getMaxCol(), Board.getMaxRow()) + 28);
        } else {
            WIDTH = (int) (1 / Board.getSCALE() * 100 * Math.max(Board.getMaxCol(), Board.getMaxRow()) + 300);
            HEIGHT = (int) (1 / Board.getSCALE() * 100 * Math.min(Board.getMaxCol(), Board.getMaxRow()) + 28);
        }
    }

    public static int getWIDTH() {
        return WIDTH;
    }

    public static int getHEIGHT() {
        return HEIGHT;
    }
}
