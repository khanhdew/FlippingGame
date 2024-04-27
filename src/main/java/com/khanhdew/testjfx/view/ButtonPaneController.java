package com.khanhdew.testjfx.view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class ButtonPaneController {

    @FXML
    public ComboBox player1;
    @FXML
    public ComboBox player2;
    MainPane mainPane;

    public void setGamePane(MainPane mainPane) {
        this.mainPane = mainPane;
    }

    @FXML
    public Button newGame;
    @FXML
    public Button exit;
    @FXML
    public TextField row;
    @FXML
    public TextField col;

    @FXML
    public void startNewGame(ActionEvent actionEvent) {
    }
}
