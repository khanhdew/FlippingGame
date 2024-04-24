package com.khanhdew.testjfx;

import com.khanhdew.testjfx.view.GamePane;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.awt.*;

public class WelcomePaneController {
    @FXML
    private TextField rowField;

    @FXML
    private TextField colField;

    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }


    @FXML
    private void startGame() {
        try {
            int rows = Integer.parseInt(rowField.getText());
            int cols = Integer.parseInt(colField.getText());

            // Tạo GamePane với số hàng và cột đã chọn
            GamePane gamePane = new GamePane(rows, cols);
            gamePane.setPrefSize(GamePane.getWIDTH(), GamePane.getHEIGHT());
            stage.setScene(new Scene(gamePane, GamePane.getWIDTH(), GamePane.getHEIGHT()));
            // Đặt vị trí của cửa sổ ở giữa màn hình
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            stage.setX(screenSize.getWidth() / 2 - (double) GamePane.getWIDTH() / 2);
            stage.setY(screenSize.getHeight() / 2 - (double) GamePane.getHEIGHT() / 2);
        }catch (NumberFormatException e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Invalid input");
            alert.setContentText("Please enter a valid number");
            alert.showAndWait();
        }

    }
}
