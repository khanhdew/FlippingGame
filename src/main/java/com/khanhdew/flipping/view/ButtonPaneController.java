package com.khanhdew.flipping.view;

import com.google.common.collect.BiMap;
import com.khanhdew.flipping.utils.BoardHelper;
import com.khanhdew.flipping.utils.DAO;
import com.khanhdew.flipping.utils.Language;
import com.khanhdew.flipping.utils.PlayerRegister;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

public class ButtonPaneController {
    @FXML
    public Button reset;
    @FXML
    public Label rowLabel;
    @FXML
    public Label colLabel;
    @FXML
    public Label player1Label;
    @FXML
    public Label player2Label;
    public Button undo;
    public Button setting;
    public HBox savePane;
    public Button saveGame;
    public Button loadSavedGame;
    Language language = Language.ENGLISH;
    BiMap<String, String> languageMap;
    @FXML
    public ComboBox player1;
    @FXML
    public ComboBox player2;
    MainPane mainPane;
    PlayerRegister playerRegister = PlayerRegister.getInstance();

    public void setGamePane(MainPane mainPane) {
        this.mainPane = mainPane;
        languageMap = mainPane.getLanguageMap();
        reset.setText(languageMap.get("reset"));
//        reset.getStyleClass().add("btn");
        reset.setOnAction(e -> {
            mainPane.reset();
            mainPane.showScore();
        });
        exit.setText(languageMap.get("exit"));
        exit.setOnAction(e -> {
            Platform.exit();
        });
        rowLabel.setText(languageMap.get("row"));
        colLabel.setText(languageMap.get("col"));
        newGame.setText(languageMap.get("newgame"));
        player1Label.setText(languageMap.get("player1"));
        player1.getItems().addAll(playerRegister.getPlayerTypes());
        player1.setValue(languageMap.get("human"));
        player2Label.setText(languageMap.get("player2"));
        player2.getItems().addAll(playerRegister.getPlayerTypes());
        player2.setValue(languageMap.get("human"));
        undo.setText(languageMap.get("undo"));
        setting.setText(languageMap.get("setting"));
        saveGame.setText(languageMap.get("save"));
        loadSavedGame.setText(languageMap.get("load"));
        newGame.setOnAction(e -> {
            try {
                int rows = Integer.parseInt(row.getText());
                int cols = Integer.parseInt(col.getText());

                String player1Type = languageMap.inverse().get(player1.getValue().toString());
                String player2Type = languageMap.inverse().get(player2.getValue().toString());
                mainPane.newGame(rows, cols, player1Type, player2Type);

            } catch (NumberFormatException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle(languageMap.get("error"));
                alert.setHeaderText(languageMap.get("invalidInput"));
                alert.setContentText(languageMap.get("adviceInput"));
                alert.showAndWait();
            }
        });
        undo.setOnAction(e -> {
            mainPane.unDo();
        });
        setting.setOnAction(e -> {
            BoardHelper.printMatrix(mainPane.getMatrix());
        });
        saveGame.setOnAction(e -> {
            DAO.saveGame(mainPane);
        });
        loadSavedGame.setOnAction(e -> {
//             List<MainPane> games = DAO.loadGame(mainPane);
//             savePane.getChildren().clear();
//                for (MainPane game : games) {
//                    Button button = new Button("Game " + (games.indexOf(game) + 1));
//                    button.setOnAction(event -> {
//                        mainPane.newGame(game.getMatrix().length, game.getMatrix()[0].length, "human", "human");
//                    });
//                    savePane.getChildren().add(button);
//                }
            DAO.loadLastSave(mainPane);
        });

        if (mainPane.isOnlineMode()) {
            configureOnlineMode();
        }
    }

    public void configureOnlineMode() {
        if (mainPane == null || !mainPane.isOnlineMode()) return;
        if (languageMap == null) {
            languageMap = mainPane.getLanguageMap();
        }

        if (undo != null) undo.setDisable(true);
        if (saveGame != null) saveGame.setDisable(true);
        if (loadSavedGame != null) loadSavedGame.setDisable(true);
        if (player1 != null) player1.setDisable(true);
        if (player2 != null) player2.setDisable(true);
        if (row != null) row.setDisable(true);
        if (col != null) col.setDisable(true);

        String rematchText = (languageMap != null && languageMap.containsKey("rematch"))
                ? languageMap.get("rematch") : "Rematch";
        if (newGame != null) {
            newGame.setText(rematchText);
            newGame.setOnAction(e -> {
                com.khanhdew.flipping.net.NetworkManager nm = (mainPane != null && mainPane.getNetworkManager() != null)
                        ? mainPane.getNetworkManager() : com.khanhdew.flipping.net.NetworkManager.getInstance();
                nm.sendRestartReq();
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle(rematchText);
                alert.setHeaderText(null);
                alert.setContentText("Rematch request sent to opponent.");
                alert.show();
            });
        }

        String surrenderText = (languageMap != null && languageMap.containsKey("surrender"))
                ? languageMap.get("surrender") : "Surrender";
        if (reset != null) {
            reset.setText(surrenderText);
            reset.setOnAction(e -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle(surrenderText);
                confirm.setHeaderText(null);
                confirm.setContentText("Are you sure you want to surrender?");
                confirm.showAndWait().ifPresent(res -> {
                    if (res == ButtonType.OK) {
                        com.khanhdew.flipping.net.NetworkManager nm = (mainPane != null && mainPane.getNetworkManager() != null)
                                ? mainPane.getNetworkManager() : com.khanhdew.flipping.net.NetworkManager.getInstance();
                        nm.sendSurrender();
                        if (mainPane != null) {
                            mainPane.handleLocalSurrender();
                        }
                    }
                });
            });
        }
    }

    public MainPane getMainPane() {
        return mainPane;
    }

    public void setPlayerRegister(PlayerRegister playerRegister){
        this.playerRegister = playerRegister;
    }

    @FXML
    public Button newGame;
    @FXML
    public Button exit;
    @FXML
    public TextField row;
    @FXML
    public TextField col;

}
