package com.khanhdew.testjfx.view;

import com.google.common.collect.BiMap;
import com.khanhdew.testjfx.utils.BoardHelper;
import com.khanhdew.testjfx.utils.Language;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

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
    Language language = Language.ENGLISH;
    BiMap<String, String> languageMap = language.getLanguage();
    @FXML
    public ComboBox player1;
    @FXML
    public ComboBox player2;
    MainPane mainPane;

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
        player1.getItems().addAll(languageMap.get("human"), languageMap.get("easyai"));
        player1.setValue(languageMap.get("human"));
        player2Label.setText(languageMap.get("player2"));
        player2.getItems().addAll(languageMap.get("human"), languageMap.get("easyai"));
        player2.setValue(languageMap.get("human"));
        undo.setText(languageMap.get("undo"));
        setting.setText(languageMap.get("setting"));
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
            BoardHelper.printMatrix(mainPane.matrix);
        });
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
