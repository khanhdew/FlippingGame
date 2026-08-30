package com.khanhdew.flipping.view;

import com.google.common.collect.BiMap;
import com.khanhdew.flipping.model.*;
import com.khanhdew.flipping.net.NetworkEventListener;
import com.khanhdew.flipping.net.NetworkManager;
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
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import org.kordamp.bootstrapfx.BootstrapFX;


import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;


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
    volatile int turn = 1;
    private long lastMoveTime = System.currentTimeMillis();
    private final boolean hasAImoved = false;
    private final int TIME_LIMIT_EACH_TURN = 10;
    private final int COOLDOWN = 600;


    private ArrayList<Piece> pieces = new ArrayList<>();
    private int[][] matrix;
    private final Stack<Piece> historyMove = new Stack<>();

    private boolean isOnlineMode = false;
    private int localPlayerId = 1;
    private boolean isDisconnected = false;
    private ChatPane chatPane;
    private Label onlineStatusBanner;
    private NetworkManager networkManager = NetworkManager.getInstance();
    private ButtonPaneController buttonPaneController;

    public MainPane() {
        this.gameLanguage = Language.ENGLISH;
        this.languageMap = gameLanguage.getLanguage();
        setPlayers("human", "human");
    }

    public MainPane(String player1Type, String player2Type, Language gameLanguage) {
        this.gameLanguage = gameLanguage != null ? gameLanguage : Language.ENGLISH;
        this.languageMap = this.gameLanguage.getLanguage();
        setPlayers(player1Type, player2Type);
    }

    private void setPlayers(String player1Type, String player2Type) {
        String p1Title = (languageMap != null && languageMap.containsKey("player1")) ? languageMap.get("player1") : "Player 1";
        String p2Title = (languageMap != null && languageMap.containsKey("player2")) ? languageMap.get("player2") : "Player 2";
        p1 = Player.createPlayer(1, p1Title, player1Type);
        p2 = Player.createPlayer(2, p2Title, player2Type);
    }

    public void setPlayersName(String player1Name, String player2Name) {
        if (p1 != null) p1.setName(player1Name);
        if (p2 != null) p2.setName(player2Name);
    }

    public void setOnlineMode(boolean isOnline, int localPlayerId) {
        this.isOnlineMode = isOnline;
        this.localPlayerId = localPlayerId;
        this.isDisconnected = false;
        if (isOnline) {
            registerNetworkListener();
            if (scorePane != null && chatPane == null) {
                chatPane = new ChatPane(gameLanguage);
                chatPane.setNetworkManager(networkManager);
                scorePane.setBottom(chatPane);
            }
            if (buttonPaneController != null) {
                buttonPaneController.configureOnlineMode();
            }
        }
    }

    public boolean isOnlineMode() {
        return isOnlineMode;
    }

    public int getLocalPlayerId() {
        return localPlayerId;
    }

    public void registerNetworkListener() {
        if (networkManager == null) return;
        networkManager.setEventListener(new NetworkEventListener() {
            @Override
            public void onConnected(String opponentName, int rows, int cols, int assignedPlayerId) {
            }

            @Override
            public void onMoveReceived(int row, int col) {
                Platform.runLater(() -> applyRemoteMove(row, col));
            }

            @Override
            public void onChatReceived(String sender, String message) {
                if (chatPane != null) {
                    chatPane.appendMessage(sender, message);
                }
            }

            @Override
            public void onRestartRequested() {
                Platform.runLater(() -> handleRestartRequested());
            }

            @Override
            public void onRestartAccepted() {
                Platform.runLater(() -> {
                    reset();
                    showScore();
                });
            }

            @Override
            public void onRestartDeclined() {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle(getLangText("rematch", "Rematch"));
                    alert.setHeaderText(null);
                    alert.setContentText("Opponent declined the rematch request.");
                    alert.show();
                });
            }

            @Override
            public void onOpponentSurrendered() {
                Platform.runLater(() -> handleOpponentSurrender());
            }

            @Override
            public void onDisconnected(String reason) {
                Platform.runLater(() -> handleDisconnection(reason));
            }

            @Override
            public void onError(String errorMessage) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle(getLangText("error", "Error"));
                    alert.setHeaderText(null);
                    alert.setContentText(errorMessage);
                    alert.show();
                });
            }
        });
    }

    public String getLangText(String key, String fallback) {
        if (languageMap != null && languageMap.containsKey(key)) {
            return languageMap.get(key);
        }
        return fallback;
    }

    public void handleRestartRequested() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(getLangText("rematch", "Rematch"));
        alert.setHeaderText(getLangText("rematch", "Rematch"));
        alert.setContentText(getLangText("rematchPrompt", "Opponent requested a rematch. Do you accept?"));
        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                if (networkManager != null) {
                    networkManager.sendRestartAck();
                }
                reset();
                showScore();
            } else {
                if (networkManager != null) {
                    networkManager.sendRestartDecl();
                }
            }
        });
    }

    public void handleOpponentSurrender() {
        if (timer != null) {
            timer.cancel();
        }
        mouse.mouseClicked = false;
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(getLangText("gameOver", "Game Over"));
        alert.setHeaderText(getLangText("win", "Wins!"));
        alert.setContentText("Opponent surrendered! You win!");
        alert.show();
    }

    public void handleLocalSurrender() {
        if (timer != null) {
            timer.cancel();
        }
        mouse.mouseClicked = false;
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(getLangText("gameOver", "Game Over"));
        alert.setHeaderText(getLangText("surrender", "Surrender"));
        alert.setContentText("You surrendered. Game over.");
        alert.show();
    }

    public void handleDisconnection(String reason) {
        if (isDisconnected) {
            return;
        }
        isDisconnected = true;
        if (timer != null) {
            timer.cancel();
        }
        mouse.mouseClicked = false;
        if (onlineStatusBanner != null) {
            onlineStatusBanner.setText("Disconnected");
            onlineStatusBanner.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #6c757d; -fx-padding: 5;");
        }
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(getLangText("connectionLost", "Connection Lost"));
        alert.setHeaderText(null);
        alert.setContentText((reason != null && !reason.isEmpty()) ? reason : getLangText("connectionLost", "Connection lost with opponent."));
        alert.show();
    }

    public void applyRemoteMove(int row, int col) {
        if (move(col, row, turn)) {
            turn = (turn == 1) ? 2 : 1;
            setScore();
            showScore();
            lastMoveTime = System.currentTimeMillis();
        }
    }

    public void init(int row, int col) {
        board = new Board(row, col);
        matrix = new int[Board.getMaxRow()][Board.getMaxCol()];
        calDimension();
        showTimer();
        scorePane = new BorderPane();
        Scene scoreScene = new Scene(scorePane, 300, HEIGHT);
        scoreScene.setFill(Color.BLACK);
        scorePane.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
//        scorePane.setCenter(timerPane);
        gamePane = new BorderPane();

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

        if (isOnlineMode) {
            if (chatPane == null) {
                chatPane = new ChatPane(gameLanguage);
                chatPane.setNetworkManager(networkManager);
            }
            scorePane.setBottom(chatPane);
            if (buttonPaneController != null) {
                buttonPaneController.configureOnlineMode();
            }
        }

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
                if ((i == BOARD_ROW / 2 && j == BOARD_COL / 2) || (i == BOARD_ROW / 2 - 1 && j == BOARD_COL / 2 - 1)) {
                    piece.setState(PieceState.BLACK);
                }
                if ((i == BOARD_ROW / 2 - 1 && j == BOARD_COL / 2) || (i == BOARD_ROW / 2 && j == BOARD_COL / 2 - 1)) {
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
        p1.reset();
        p2.reset();
        turn = 1;
        mouse.clear();
        mouse.mouseClicked = true;
        isDisconnected = false;
        setTimer();
    }

    private final HBox timerPane = new HBox(2);

    Timer timer = new Timer();

    private void showTimer() {
        setTimer();
        new AnimationTimer() {
            @Override
            public void handle(long now) {

                timerPane.setAlignment(Pos.CENTER);
                timerPane.getChildren().clear(); // Clear previous timer texts
                Text timer1 = new Text(timeConvert(p1.getTime()));
                timer1.setStyle("-fx-font: 24 arial;" +
                        "-fx-font-weight:bolder;" +
                        "-fx-fill: black;");
                Text timer2 = new Text(timeConvert(p2.getTime()));
                timer2.setStyle("-fx-font: 24 arial;" +
                        "-fx-font-weight:bolder;" +
                        "-fx-fill: black;");
                timerPane.getChildren().addAll(timer1, timer2);
            }
        }.start();
    }

    private void setTimer() {
        // Cancel any existing timer to prevent multiple concurrent timer tasks
        if (timer != null) {
            timer.cancel();
            timer = new Timer();
        }

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (turn == 1) {
                    p1.tick();
                } else {
                    p2.tick();
                }
                if (p1.getTime() <= 0 || p2.getTime() <= 0) {
                    handleGameOver();
                }
            }
        }, 1000, 1000);
    }

    public void update() {
        // set player timer
        for (Piece piece : pieces) {
            piece.initCir(piece.getCurrentState());
            if (matrix[piece.getRow()][piece.getCol()] == 3) {
                piece.setState(PieceState.HINT);
                if (!isOnlineMode || turn == localPlayerId) {
                    piece.setOnMouseEntered(e -> {
                        // get all pieces that will change
                        ArrayList<Piece> changes = BoardHelper.getPieceChangeForEachMove(matrix, getTurn(), piece.getRow(), piece.getCol());
                        // highlight all pieces that will change
                        highLightChangeablePieces(changes);
                    });
                    piece.setOnMouseExited(e -> {
                        unHighLight();
                    });
                    piece.setOnMouseClicked(event -> unHighLight());
                } else {
                    piece.setOnMouseEntered(null);
                    piece.setOnMouseExited(null);
                    piece.setOnMouseClicked(null);
                }
            } else if (matrix[piece.getRow()][piece.getCol()] == 0) {
                piece.setState(PieceState.EMPTY);
            } else {
                // clear listeners
                piece.setOnMouseEntered(null);
                piece.setOnMouseExited(null);
                piece.setOnMouseClicked(null);
            }
        }
        // slow down the game
        long now = System.currentTimeMillis();
        if (now - lastMoveTime > COOLDOWN) {
            if (mouse.mouseClicked) {
                manageTurn();
            }
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
        for (Piece piece : changes) {
            for (Piece p : pieces) {
                if (p.getCol() == piece.getCol() && p.getRow() == piece.getRow()) {
                    // Create single scale transition
                    ScaleTransition st = new ScaleTransition(Duration.millis(400), p);
                    st.setFromX(1.0);
                    st.setToX(0.2);
                    st.setAutoReverse(true);
                    st.setCycleCount(1);
                    st.setInterpolator(Interpolator.EASE_BOTH);

                    ScaleTransition st2 = new ScaleTransition(Duration.millis(400), p);
                    st2.setFromX(0.2);
                    st2.setToX(1.0);
                    st2.setAutoReverse(true);
                    st2.setCycleCount(1);
                    st2.setInterpolator(Interpolator.EASE_BOTH);

                    // Create a rotation transition for 3D effect
                    RotateTransition rt = new RotateTransition(Duration.millis(400), p);
                    rt.setFromAngle(0);
                    rt.setToAngle(180);
                    rt.setAxis(Rotate.Y_AXIS);
                    rt.setCycleCount(1);
                    rt.setInterpolator(Interpolator.LINEAR);

                    // Update matrix
                    matrix[p.row][p.col] = playerId;

                    // Change piece state at the halfway point
                    st.setOnFinished(e -> p.setState(piece.getCurrentState()));

                    // Play both transitions
                    st.play();
                    rt.play();
                    st2.play();
                }
            }
        }
    }

    public void manageTurn() {
        // Game over condition - no player can make a move
        if (!BoardHelper.canPlay(matrix, turn) && !BoardHelper.canPlay(matrix, turn == 1 ? 2 : 1)) {
            handleGameOver();
            return;
        }

        // Game over condition - player time out
        if (p1.getTime() <= 0 || p2.getTime() <= 0) {
            handleGameOver();
            return;
        }

        // Current player cannot play, switch turns
        if (!BoardHelper.canPlay(matrix, turn)) {
            turn = (turn == 1) ? 2 : 1;
            showScore();
            return;
        }

        // Show available moves for current player
        if (!isOnlineMode || turn == localPlayerId) {
            BoardHelper.hint(matrix, turn);
        } else {
            // In online mode, clear hints when it's not our turn
            for (int i = 0; i < matrix.length; i++) {
                for (int j = 0; j < matrix[0].length; j++) {
                    if (matrix[i][j] == 3) {
                        matrix[i][j] = 0;
                    }
                }
            }
        }

        long now = System.currentTimeMillis();

        if (isOnlineMode && turn != localPlayerId) {
            mouse.clear();
            return;
        }

        // Handle AI move if current player is AI (offline only)
        if (!isOnlineMode) {
            Player currentPlayer = (turn == 1) ? p1 : p2;
            handleAI(currentPlayer);
        }

        // Handle human player move if mouse click detected
        if (mouse.x > 0 && mouse.y > 0) {
            handleHumanMove(now);
        }
    }

    private void handleGameOver() {
        setScore();
        if (timer != null) {
            timer.cancel();
        }
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(getLangText("gameOver", "Game Over"));

            if (p1.getScore() > p2.getScore()) {
                alert.setHeaderText(p1.getName() + " " + getLangText("win", "Wins!"));
                alert.setContentText(p1.getName() + " " + getLangText("winContext", "has won the game with a score of ") + p1.getScore());
            } else if (p1.getScore() < p2.getScore()) {
                alert.setHeaderText(p2.getName() + " " + getLangText("win", "Wins!"));
                alert.setContentText(p2.getName() + " " + getLangText("winContext", "has won the game with a score of ") + p2.getScore());
            } else {
                alert.setHeaderText(getLangText("draw", "Draw!"));
                alert.setContentText(getLangText("drawContext", "The game has ended in a draw with both players scoring ") + p1.getScore());
            }

            alert.showAndWait();
        });
        mouse.mouseClicked = false;
    }

    private void makeTimeoutMove(long now) {
        ArrayList<Piece> availableMoves = BoardHelper.getAvailableMoves(matrix, turn);
        if (!availableMoves.isEmpty()) {
            Piece randomMove = availableMoves.get(0);
            if (move(randomMove.getCol(), randomMove.getRow(), turn)) {
                turn = (turn == 1) ? 2 : 1;
                setScore();
                showScore();
                lastMoveTime = now;
            }
        }
    }

    private void handleHumanMove(long now) {
        if (isOnlineMode && turn != localPlayerId) {
            mouse.clear();
            return;
        }

        int moveRow = mouse.getRow();
        int moveCol = mouse.getCol();

        if (!BoardHelper.isLegalMove(matrix, turn, moveRow, moveCol)) {
            return;
        }

        if (move(moveCol, moveRow, turn)) {
            if (isOnlineMode && networkManager != null) {
                networkManager.sendMove(moveRow, moveCol);
            }
            turn = (turn == 1) ? 2 : 1;
            setScore();
            showScore();
            lastMoveTime = now;
        }
    }

    public void setScore() {
        int[] score = BoardHelper.getScore(matrix);
        p1.setScore(score[0]);
        p2.setScore(score[1]);
    }


    public void highLightChangeablePieces(ArrayList<Piece> changes) {
        //infinte loop
        FadeTransition ft = new FadeTransition(Duration.millis(100));
        ft.setFromValue(1.0);
        ft.setToValue(0.5);
        ft.setCycleCount(2);
        ft.setAutoReverse(true);

        for (Piece piece : changes) {
            for (Piece p : pieces) {
                if (p.getRow() == piece.getRow() && p.getCol() == piece.getCol() && p.getCurrentState() != PieceState.EMPTY) {
                    p.setOpacity(0.5);
                }
            }
        }
    }

    public void unHighLight() {
        pieces.forEach(p -> p.setOpacity(1));
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
        VBox vBox = new VBox(2);
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

        if (onlineStatusBanner == null) {
            onlineStatusBanner = new Label();
        }
        if (isOnlineMode) {
            if (turn == localPlayerId) {
                String yourTurnText = (languageMap != null && languageMap.containsKey("yourTurn"))
                        ? languageMap.get("yourTurn") : "Your Turn";
                onlineStatusBanner.setText(yourTurnText);
                onlineStatusBanner.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #28a745; -fx-alignment: center;");
            } else {
                String oppTurnText = (languageMap != null && languageMap.containsKey("opponentTurn"))
                        ? languageMap.get("opponentTurn") : "Opponent's Turn";
                onlineStatusBanner.setText(oppTurnText);
                onlineStatusBanner.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #dc3545; -fx-alignment: center;");
            }
            vBox.setAlignment(Pos.CENTER);
            vBox.getChildren().addAll(pane, timerPane, onlineStatusBanner);
        } else {
            vBox.getChildren().addAll(pane, timerPane);
        }

        scorePane.setTop(vBox);
    }


    private String timeConvert(long time) {
        long minutes = (time / 1000) / 60;
        int seconds = (int) ((time / 1000) % 60);
        return String.format("%02d:%02d", minutes, seconds);
    }

    public void showBtn() {
        FXMLLoader loader = null;
        VBox pane;
        try {
            loader = new FXMLLoader(getClass().getResource("/com/khanhdew/flipping/button-Pane.fxml"));
            pane = loader.load();
            buttonPaneController = loader.getController();
            buttonPaneController.setGamePane(this);
            pane.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load button-Pane.fxml", e);
        }
        scorePane.setCenter(pane);
    }

    public void showMenu() {
        Menu gameMenu = new Menu("Game");
        MenuItem newItem = new MenuItem("New game");
        newItem.setOnAction(e -> {
            if (isOnlineMode) {
                if (networkManager != null) {
                    networkManager.sendRestartReq();
                }
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                String rematchText = getLangText("rematch", "Rematch");
                alert.setTitle(rematchText);
                alert.setHeaderText(null);
                alert.setContentText("Rematch request sent to opponent.");
                alert.show();
            } else {
                reset();
            }
        });
        gameMenu.getItems().add(newItem);
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> {
            NetworkManager.getInstance().disconnect();
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
                lastMoveTime = System.currentTimeMillis();
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

    public ChatPane getChatPane() {
        return chatPane;
    }

    public void setChatPane(ChatPane chatPane) {
        this.chatPane = chatPane;
    }

    public Label getOnlineStatusBanner() {
        return onlineStatusBanner;
    }

    public void setOnlineStatusBanner(Label onlineStatusBanner) {
        this.onlineStatusBanner = onlineStatusBanner;
    }

    public NetworkManager getNetworkManager() {
        return networkManager;
    }

    public void setNetworkManager(NetworkManager networkManager) {
        this.networkManager = networkManager;
        if (chatPane != null) {
            chatPane.setNetworkManager(networkManager);
        }
        if (isOnlineMode) {
            registerNetworkListener();
        }
    }

    public ButtonPaneController getButtonPaneController() {
        return buttonPaneController;
    }

    public void setButtonPaneController(ButtonPaneController buttonPaneController) {
        this.buttonPaneController = buttonPaneController;
    }

    public Mouse getMouse() {
        return mouse;
    }

    public void setLocalPlayerId(int localPlayerId) {
        this.localPlayerId = localPlayerId;
    }

    public Stack<Piece> getHistoryMove() {
        return historyMove;
    }

    public BorderPane getScorePane() {
        return scorePane;
    }

    public boolean isDisconnected() {
        return isDisconnected;
    }
}
