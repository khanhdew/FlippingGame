package com.khanhdew.flipping.view;

import com.google.common.collect.BiMap;
import com.khanhdew.flipping.model.*;
import com.khanhdew.flipping.utils.BoardHelper;
import com.khanhdew.flipping.utils.Language;
import com.khanhdew.flipping.utils.Mouse;
import javafx.animation.*;


import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.kordamp.bootstrapfx.BootstrapFX;


import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Stack;


public class MainPane extends BorderPane {
    public static int WIDTH;
    public static int HEIGHT;

    private BorderPane scorePane;
    private BorderPane gamePane;

    private Canvas c1;
    private Language gameLanguage = Language.ENGLISH;
    private BiMap<String, String> languageMap;

    private Board board;
    Mouse mouse = new Mouse();

    Player p1;
    Player p2;
    int turn = 1;
    private long lastMoveTime = 0;
    private final boolean hasAImoved = false;


    private ArrayList<Piece> pieces = new ArrayList<>();
    private int[][] matrix;
    private final Stack<Piece> historyMove = new Stack<>();

    public MainPane() {
    }

    public MainPane(String player1Type, String player2Type, Language gameLanguage) {
        this.gameLanguage = gameLanguage;
        languageMap = gameLanguage.getLanguage();
        setPlayers(player1Type, player2Type);
    }

    private void setPlayers(String player1Type, String player2Type) {
        switch (player1Type) {
            case "human":
                p1 = new HumanPlayer(1, languageMap.get("player1"));
                break;
            case "easyai":
                p1 = new EasyAI(1, languageMap.get("player1"));
                break;
            case "minimaxai":
                p1 = new MinimaxAI(1, languageMap.get("player1"));
                break;
        }
        switch (player2Type) {
            case "human":
                p2 = new HumanPlayer(2, languageMap.get("player2"));
                break;
            case "easyai":
                p2 = new EasyAI(2, languageMap.get("player2"));
                break;
            case "minimaxai":
                p2 = new MinimaxAI(2, languageMap.get("player1"));
                break;
        }
    }

    public void setPlayersName(String player1Name, String player2Name) {
        p1.setName(player1Name);
        p2.setName(player2Name);
    }

    public void init(int row, int col) {
        board = new Board(row, col);
        matrix = new int[Board.getMaxRow()][Board.getMaxCol()];
        calDimension();

        scorePane = new BorderPane();
        Scene scoreScene = new Scene(scorePane, 300, HEIGHT);
        scoreScene.setFill(Color.BLACK);
        scorePane.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        gamePane = new BorderPane();
        Scene gameScene = new Scene(gamePane, 800, HEIGHT);


        gamePane.addEventHandler(MouseEvent.MOUSE_CLICKED, mouse);

        //draw MainPane
        c1 = new Canvas(800, HEIGHT);
        Board.draw(c1.getGraphicsContext2D());

        setPieces();
        gamePane.getChildren().add(c1);
        for (Piece piece : pieces) {
            gamePane.getChildren().add(piece);
        }

        // Add mainPane and scorePane to different positions in MainPane
        this.setCenter(gamePane);
        this.setRight(scorePane);

        // Player timer
        showBtn();
        showMenu();
        showScore();

        new AnimationTimer() {
            long lastTick = 0;

            @Override
            public void handle(long now) {
                if (lastTick == 0) {
                    lastTick = now;
                    update();
                    return;
                }
                if (now - lastTick > 1000000000 / 60) {
                    lastTick = now;
                    update();
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
//                piece.setOnMouseClicked(e -> {
//                    //TODO: Highlight surrounding pieces
//                    highlightSurroundingPieces(getTurn(), BoardHelper.getPieceChangeForEachMove(matrix, getTurn(), piece.getRow(), piece.getCol()));
//                    if(piece.getCurrentState()!=PieceState.EMPTY)
//                    System.out.println("mouse entered" + piece);
//                });
//                piece.setOnMouseExited(e -> {
//                    //TODO: Unhighlight surrounding pieces
//                    unhighlightSurroundingPieces(getTurn(), BoardHelper.getPieceChangeForEachMove(matrix, getTurn(), piece.getRow(), piece.getCol()));
//                    if(piece.getCurrentState()!=PieceState.EMPTY)
//                        System.out.println("mouse exited" +piece);
//                });
                if((i == BOARD_ROW/2 && j == BOARD_COL/2)||(i == BOARD_ROW/2 - 1 && j == BOARD_COL/2 - 1)){
                    piece.setState(PieceState.BLACK);
                }
                if((i == BOARD_ROW/2 - 1 && j == BOARD_COL/2)||(i == BOARD_ROW/2 && j == BOARD_COL/2 - 1)){
                    piece.setState(PieceState.WHITE);
                }
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

    public void reset() {
        gamePane.getChildren().clear();
        gamePane.getChildren().add(c1);
        pieces.clear();
        historyMove.clear();
        setPieces();
        for (Piece piece : pieces) {
            gamePane.getChildren().add(piece);
        }
        p1.setScore(0);
        p2.setScore(0);
        turn = 1;
        mouse.clear();
        mouse.mouseClicked = true;
    }

    public void update() {

        for (Piece piece : pieces) {
            piece.initCir(piece.getCurrentState());
            if(matrix[piece.getRow()][piece.getCol()] == 3){
                piece.setState(PieceState.HINT);
            } else if(matrix[piece.getRow()][piece.getCol()] == 0){
                piece.setState(PieceState.EMPTY);
            }
        }
        long now = System.currentTimeMillis();
        if (now - lastMoveTime > 600) {
            if (mouse.mouseClicked) {
                manageTurn();
            }
            lastMoveTime = now;
        }
    }

    public int getTurn() {
        return turn;
    }

    public boolean move(int col, int row, int playerId) {
        for (Piece piece : pieces) {
            if (piece.getCol() == col && piece.getRow() == row) {
                if (piece.getCurrentState() == PieceState.EMPTY || piece.getCurrentState() == PieceState.HINT) {
                    FadeTransition ft = new FadeTransition(Duration.millis(400), piece);
                    ft.setFromValue(1.0);
                    ft.setToValue(0.5);
                    ft.setCycleCount(2);
                    ft.setAutoReverse(true);
                    ft.play();
                    piece.setState(playerId == 1 ? PieceState.BLACK : PieceState.WHITE);
                    changePieceState(BoardHelper.getPieceChangeForEachMove(matrix, playerId, row, col), playerId);
                    BoardHelper.toMatrix(matrix, piece);
                    historyMove.push(piece);
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
                    // Tạo hiệu ứng lật
                    ScaleTransition st1 = new ScaleTransition(Duration.millis(300), p);
                    RotateTransition rt = new RotateTransition(Duration.millis(800), p);
                    rt.setByAngle(360);
                    rt.setCycleCount(1);
                    rt.play();
                    st1.setByX(-0.2);
                    st1.setByY(-1);
                    st1.setCycleCount(1);
                    matrix[p.row][p.col] = playerId;
                    // Đặt hành động khi hiệu ứng hoàn tất
                    st1.setOnFinished(e -> {
                        p.setState(piece.getCurrentState());
                        ScaleTransition st2 = new ScaleTransition(Duration.millis(300), p);
                        st2.setByX(0.2);
                        st2.setByY(1);
                        st2.setCycleCount(1);
                        st2.play();
                    });
                    st1.play();
                }
            }
        }
    }

    public void manageTurn() {
        if (!BoardHelper.canPlay(matrix, turn)) {
            setScore();
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle(languageMap.get("gameOver"));
                if (p1.getScore() > p2.getScore()) {
                    alert.setHeaderText(p1.getName() + " " + languageMap.get("win"));
                    alert.setContentText(p1.getName() + " " + languageMap.get("winContext") + p1.getScore());
                } else if (p1.getScore() < p2.getScore()) {
                    alert.setHeaderText(p2.getName() + " " + languageMap.get("win"));
                    alert.setContentText(p2.getName() + " " + languageMap.get("winContext") + p2.getScore());
                } else {
                    alert.setHeaderText(languageMap.get("draw"));
                    alert.setContentText(languageMap.get("drawContext") + p1.getScore());
                }
                alert.showAndWait();
            });
            mouse.mouseClicked = false;
        } else {
            BoardHelper.hint(matrix, turn);
            if (turn == 1) {
                handleAI(p1);
                if (mouse.x > 0 && mouse.y > 0) {
                    if(!BoardHelper.isLegalMove(matrix, turn, mouse.getRow(), mouse.getCol()))
                        return;
                    // Only allow the user to move if it's not the AI's turn
                    if (move(mouse.getCol(), mouse.getRow(), turn)) {
                        turn = 2;
//                        manageTurn();
                        setScore();
                        showScore();
                    }
                }

            } else {
                handleAI(p2);
                if (mouse.x > 0 && mouse.y > 0) {
                    // Only allow the user to move if it's not the AI's turn
                    if(!BoardHelper.isLegalMove(matrix, turn, mouse.getRow(), mouse.getCol()))
                        return;
                    if (move(mouse.getCol(), mouse.getRow(), turn)) {
                        turn = 1;
//                        manageTurn();
                        setScore();
                        showScore();
                    }
                }

            }
        }
    }

    public void setScore() {
        int[] score = BoardHelper.getScore(matrix);
        p1.setScore(score[0]);
        p2.setScore(score[1]);
    }


    public void highlightSurroundingPieces(int playerId, ArrayList<Piece> changes) {
        //infinte loop
        FadeTransition ft = new FadeTransition(Duration.millis(100));
        ft.setFromValue(1.0);
        ft.setToValue(0.5);
        ft.setCycleCount(2);
        ft.setAutoReverse(true);

        for (Piece piece : changes) {
            for (Piece p : pieces) {
                if (p.getRow() == piece.getRow() && p.getCol() == piece.getCol() && p.getCurrentState() != PieceState.EMPTY) {
                    p.updateState(playerId == 1 ? PieceState.BLACK : PieceState.WHITE);
                    ft.setNode(p);
                    ft.play();
                }
            }
        }
    }

    public void unhighlightSurroundingPieces(int playerId, ArrayList<Piece> changes) {
        for (Piece piece : changes) {
            for (Piece p : pieces) {
                if (p.getRow() == piece.getRow() && p.getCol() == piece.getCol()) {
                    p.updateState(p.getLastState());
                }
            }
        }
    }

    public void showScore() {
        //Create scale and fade animation
        ScaleTransition st = new ScaleTransition(Duration.millis(800));
        st.setByX(0.1);
        st.setByY(0.1);
        st.setCycleCount(Animation.INDEFINITE);
        st.setAutoReverse(true);
        FadeTransition ft = new FadeTransition(Duration.millis(800));
        ft.setFromValue(1.0);
        ft.setToValue(0.5);
        ft.setCycleCount(Animation.INDEFINITE);
        ft.setAutoReverse(true);

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
        if (turn == 1) {
            st.setNode(c1);
            ft.setNode(c1);
            st.play();
            ft.play();
        } else {
            st.setNode(c2);
            ft.setNode(c2);
            st.play();
            ft.play();
        }

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
        pane.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
        scorePane.setTop(pane);

    }

    public void showBtn() {
        FXMLLoader loader = null;
        VBox pane;
        try {
            loader = new FXMLLoader(new URL("file:src/main/resources/com/khanhdew/flipping/button-Pane.fxml"));
            pane = loader.load();
            ButtonPaneController controller = loader.getController();
            controller.setGamePane(this);
            pane.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        scorePane.setCenter(pane);
    }

    public void showMenu() {
        Menu gameMenu = new Menu("Game");
        MenuItem newItem = new MenuItem("New game");
        newItem.setOnAction(e -> {
            reset();
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

    public void unDo() {
        mouse.clear();
        if (!historyMove.isEmpty()) {
            Piece piece = historyMove.pop();
            piece.setState(PieceState.EMPTY);
            ArrayList<Piece> changes = BoardHelper.getPieceChangeForEachMove(matrix, turn, piece.getRow(), piece.getCol());
            unDoChanges(changes);
            matrix[piece.getRow()][piece.getCol()] = 0;
            BoardHelper.toMatrix(matrix, piece);
            turn = turn == 1 ? 2 : 1;
            setScore();
            showScore();
        }

    }

    public void unDoChanges(ArrayList<Piece> changes) {
        for (Piece piece : changes) {
            for (Piece p : pieces) {
                if (p.getRow() == piece.getRow() && p.getCol() == piece.getCol()) {
                    p.setState(piece.getLastState());
                }

            }
        }
        BoardHelper.toMatrix(matrix, changes);
    }

    public void newGame(int row, int col, String player1Type, String player2Type) {
        // Tạo board mới với số hàng và cột đã chọn
        reset();
        calDimension();
        setPrefHeight(HEIGHT);
        setPrefWidth(WIDTH);
        reset();
//        init(row, col);
        board = new Board(row, col);
        matrix = new int[Board.getMaxRow()][Board.getMaxCol()];

        // Tạo người chơi mới dựa trên loại người chơi đã chọn
        setPlayers(player1Type, player2Type);
        if (p1 instanceof AI) {
            System.out.println("AI");
        } else {
            System.out.println("Human");
        }
        if (p2 instanceof AI) {
            System.out.println("AI");
        } else {
            System.out.println("Human");
        }
        showScore();
        // Đặt lại điểm số
        p1.setScore(0);
        p2.setScore(0);

        // Đặt lại lượt chơi
        turn = 1;

        reDraw();
    }

    public void reDraw() {
        // Vẽ lại bảng
        c1 = new Canvas(800, 800);

        // Vẽ lại game
        showScore();
        gamePane.getChildren().clear();
        gamePane.getChildren().add(c1);
        pieces.clear();
        setPieces();
        for (Piece piece : pieces) {
            piece.setPieceSize(Board.getSquareSize());
            gamePane.getChildren().add(piece);
        }
        Board.draw(c1.getGraphicsContext2D());
    }

    public void handleAI(Player ai) {
        Piece nextMove;
        if (ai instanceof AI) {
            nextMove = ((AI) ai).makeMove(matrix);
            if (move(nextMove.getCol(), nextMove.getRow(), turn)) {
                turn = (turn == 1) ? 2 : 1;
                setScore();
                showScore();
            }
        }

    }

    public void loadGame(MainPane temp) {
        int rows = temp.getMatrix().length;
        int cols = temp.getMatrix()[0].length;
        setBoard(rows, cols);
        setMatrix(temp.getMatrix());
        setPlayers("human", "human");
        setTurn(temp.getTurn());
        setScore();
        showScore();
        gamePane.getChildren().clear();
        gamePane.getChildren().add(c1);
        pieces.clear();
        setPieces();
        for (Piece piece : pieces) {
            gamePane.getChildren().add(piece);
        }

    }

    public Language getGameLanguage() {
        return gameLanguage;
    }

    public void setGameLanguage(Language gameLanguage) {
        this.gameLanguage = gameLanguage;
        languageMap.clear();
        languageMap = gameLanguage.getLanguage();
    }

    public BiMap<String, String> getLanguageMap() {
        return languageMap;
    }

    public void setLanguageMap(BiMap<String, String> languageMap) {
        this.languageMap = languageMap;
    }

    public Player getP1() {
        return p1;
    }

    public void setP1(Player p1) {
        this.p1 = p1;
    }

    public Player getP2() {
        return p2;
    }

    public void setP2(Player p2) {
        this.p2 = p2;
    }

    public int[][] getMatrix() {
        return matrix;
    }

    public void setMatrix(int[][] matrix) {
        this.matrix = matrix;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(int row, int col) {
        board = new Board(row, col);
    }

    public ArrayList<Piece> getPieces() {
        return pieces;
    }

    public void setPieces(ArrayList<Piece> pieces) {
        this.pieces = pieces;
    }

    public BorderPane getGamePane() {
        return gamePane;
    }

    public void setGamePane(BorderPane gamePane) {
        this.gamePane = gamePane;
    }

    public Canvas getC1() {
        return c1;
    }

    public void setC1(Canvas c1) {
        this.c1 = c1;
    }
}
