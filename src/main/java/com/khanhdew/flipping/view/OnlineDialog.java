package com.khanhdew.flipping.view;

import com.google.common.collect.BiMap;
import com.khanhdew.flipping.net.NetworkEventListener;
import com.khanhdew.flipping.net.NetworkManager;
import com.khanhdew.flipping.utils.Language;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class OnlineDialog {
    private final Stage parentStage;
    private final Language language;
    private final BiMap<String, String> langMap;
    private Stage dialogStage;
    private NetworkManager netManager = NetworkManager.getInstance();

    public OnlineDialog(Stage parentStage, Language language) {
        this.parentStage = parentStage;
        this.language = language;
        this.langMap = language.getLanguage();
    }

    public void show() {
        dialogStage = new Stage();
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(parentStage);
        dialogStage.setTitle(langMap.get("onlineMultiplayer"));

        TabPane tabPane = new TabPane();

        // --- HOST TAB ---
        Tab hostTab = new Tab(langMap.get("hostGame"));
        hostTab.setClosable(false);

        VBox hostBox = new VBox(10);
        hostBox.setPadding(new Insets(15));
        hostBox.setAlignment(Pos.CENTER_LEFT);

        String localIp = NetworkManager.getLocalIpAddress();
        Label ipLabel = new Label(langMap.get("ipAddress") + ": " + localIp);
        ipLabel.setStyle("-fx-font-weight: bold;");

        TextField hostPortField = new TextField("55555");
        TextField hostNameField = new TextField(langMap.get("player1"));
        TextField hostRowsField = new TextField("8");
        TextField hostColsField = new TextField("8");

        GridPane hostGrid = new GridPane();
        hostGrid.setHgap(10);
        hostGrid.setVgap(8);
        hostGrid.add(new Label(langMap.get("port") + ":"), 0, 0);
        hostGrid.add(hostPortField, 1, 0);
        hostGrid.add(new Label(langMap.get("player1Name") + ":"), 0, 1);
        hostGrid.add(hostNameField, 1, 1);
        hostGrid.add(new Label(langMap.get("row") + ":"), 0, 2);
        hostGrid.add(hostRowsField, 1, 2);
        hostGrid.add(new Label(langMap.get("col") + ":"), 0, 3);
        hostGrid.add(hostColsField, 1, 3);

        Label hostStatusLabel = new Label("");
        Button startHostBtn = new Button(langMap.get("hostGame"));
        startHostBtn.getStyleClass().addAll("btn", "btn-primary");

        startHostBtn.setOnAction(e -> {
            try {
                int port = Integer.parseInt(hostPortField.getText().trim());
                int rows = Integer.parseInt(hostRowsField.getText().trim());
                int cols = Integer.parseInt(hostColsField.getText().trim());
                String name = hostNameField.getText().trim();

                hostStatusLabel.setText(langMap.get("waitingForOpponent"));
                startHostBtn.setDisable(true);

                netManager.startHost(port, name, rows, cols, new NetworkEventListener() {
                    @Override
                    public void onConnected(String opponentName, int r, int c, int assignedPlayerId) {
                        Platform.runLater(() -> {
                            dialogStage.close();
                            launchOnlineGame(rows, cols, name, opponentName, assignedPlayerId);
                        });
                    }
                    @Override public void onMoveReceived(int row, int col) {}
                    @Override public void onChatReceived(String sender, String message) {}
                    @Override public void onRestartRequested() {}
                    @Override public void onRestartAccepted() {}
                    @Override public void onRestartDeclined() {}
                    @Override public void onOpponentSurrendered() {}
                    @Override public void onDisconnected(String reason) {
                        Platform.runLater(() -> {
                            hostStatusLabel.setText(reason);
                            startHostBtn.setDisable(false);
                        });
                    }
                    @Override public void onError(String errorMessage) {
                        Platform.runLater(() -> {
                            hostStatusLabel.setText(errorMessage);
                            startHostBtn.setDisable(false);
                        });
                    }
                });
            } catch (NumberFormatException ex) {
                hostStatusLabel.setText(langMap.get("invalidInput"));
            }
        });

        hostBox.getChildren().addAll(ipLabel, hostGrid, startHostBtn, hostStatusLabel);
        hostTab.setContent(hostBox);

        // --- JOIN TAB ---
        Tab joinTab = new Tab(langMap.get("joinGame"));
        joinTab.setClosable(false);

        VBox joinBox = new VBox(10);
        joinBox.setPadding(new Insets(15));
        joinBox.setAlignment(Pos.CENTER_LEFT);

        TextField joinIpField = new TextField("127.0.0.1");
        TextField joinPortField = new TextField("55555");
        TextField joinNameField = new TextField(langMap.get("player2"));

        GridPane joinGrid = new GridPane();
        joinGrid.setHgap(10);
        joinGrid.setVgap(8);
        joinGrid.add(new Label(langMap.get("ipAddress") + ":"), 0, 0);
        joinGrid.add(joinIpField, 1, 0);
        joinGrid.add(new Label(langMap.get("port") + ":"), 0, 1);
        joinGrid.add(joinPortField, 1, 1);
        joinGrid.add(new Label(langMap.get("player2Name") + ":"), 0, 2);
        joinGrid.add(joinNameField, 1, 2);

        Label joinStatusLabel = new Label("");
        Button connectBtn = new Button(langMap.get("joinGame"));
        connectBtn.getStyleClass().addAll("btn", "btn-success");

        connectBtn.setOnAction(e -> {
            try {
                String ip = joinIpField.getText().trim();
                int port = Integer.parseInt(joinPortField.getText().trim());
                String name = joinNameField.getText().trim();

                joinStatusLabel.setText(langMap.get("connecting"));
                connectBtn.setDisable(true);

                netManager.connectToHost(ip, port, name, new NetworkEventListener() {
                    @Override
                    public void onConnected(String opponentName, int r, int c, int assignedPlayerId) {
                        Platform.runLater(() -> {
                            dialogStage.close();
                            launchOnlineGame(r, c, opponentName, name, assignedPlayerId);
                        });
                    }
                    @Override public void onMoveReceived(int row, int col) {}
                    @Override public void onChatReceived(String sender, String message) {}
                    @Override public void onRestartRequested() {}
                    @Override public void onRestartAccepted() {}
                    @Override public void onRestartDeclined() {}
                    @Override public void onOpponentSurrendered() {}
                    @Override public void onDisconnected(String reason) {
                        Platform.runLater(() -> {
                            joinStatusLabel.setText(reason);
                            connectBtn.setDisable(false);
                        });
                    }
                    @Override public void onError(String errorMessage) {
                        Platform.runLater(() -> {
                            joinStatusLabel.setText(errorMessage);
                            connectBtn.setDisable(false);
                        });
                    }
                });
            } catch (NumberFormatException ex) {
                joinStatusLabel.setText(langMap.get("invalidInput"));
            }
        });

        joinBox.getChildren().addAll(joinGrid, connectBtn, joinStatusLabel);
        joinTab.setContent(joinBox);

        tabPane.getTabs().addAll(hostTab, joinTab);

        dialogStage.setOnCloseRequest(e -> netManager.disconnect());

        Scene scene = new Scene(tabPane, 420, 320);
        dialogStage.setScene(scene);
        dialogStage.setResizable(false);
        dialogStage.showAndWait();
    }

    private void launchOnlineGame(int rows, int cols, String p1Name, String p2Name, int localPlayerId) {
        MainPane mainPane = new MainPane("human", "human", language);
        mainPane.setOnlineMode(true, localPlayerId);
        mainPane.setPlayersName(p1Name, p2Name);
        mainPane.init(rows, cols);

        mainPane.setPrefSize(MainPane.getWIDTH(), MainPane.getHEIGHT());
        parentStage.setResizable(true);
        parentStage.setScene(new Scene(mainPane, MainPane.getWIDTH(), MainPane.getHEIGHT()));

        Screen screen = Screen.getPrimary();
        parentStage.setX((screen.getBounds().getWidth() - MainPane.getWIDTH()) / 2);
        parentStage.setY((screen.getBounds().getHeight() - MainPane.getHEIGHT()) / 2 - 50);

        parentStage.setOnCloseRequest(e -> {
            netManager.disconnect();
            Platform.exit();
        });
    }
}
