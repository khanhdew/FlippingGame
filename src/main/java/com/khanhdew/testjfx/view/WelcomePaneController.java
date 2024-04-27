package com.khanhdew.testjfx.view;

import com.google.common.collect.BiMap;
import com.khanhdew.testjfx.utils.Language;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.awt.*;
import java.net.URL;
import java.util.ResourceBundle;

public class WelcomePaneController implements Initializable {
    public Button startButton;
    public Label rowLabel;
    public Label player1Label;
    public Label player2Label;
    public Label colLabel;
    public Label player1NameLabel;
    public Label player2NameLabel;
    public TextField player1Name;
    public TextField player2Name;
    Alert alert = new Alert(Alert.AlertType.ERROR);
    Language language = Language.ENGLISH;
    BiMap<String, String> languageMap = language.getLanguage();
    public ComboBox player1;
    public ComboBox player2;
    public ComboBox languageOption;
    @FXML
    private TextField rowField;

    @FXML
    private TextField colField;

    @FXML
    private ImageView imageView;


    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }


    @FXML
    private void startGame() {
        try {
            int rows = Integer.parseInt(rowField.getText());
            int cols = Integer.parseInt(colField.getText());
            if (rows < 4 || cols < 4) {
                alert.showAndWait();
                return;
            }

//            Tạo MainPane với số hàng và cột đã chọn
            MainPane mainPane = new MainPane(languageMap.inverse().get(player1.getValue().toString()), languageMap.inverse().get(player2.getValue().toString()),this.language);
            mainPane.init(rows, cols);
            mainPane.setPlayersName(player1Name.getText(), player2Name.getText());
            mainPane.setPrefSize(MainPane.getWIDTH(), MainPane.getHEIGHT());
            stage.setScene(new Scene(mainPane, MainPane.getWIDTH(), MainPane.getHEIGHT()));
//            Đặt vị trí của cửa sổ ở giữa màn hình
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            stage.setX(screenSize.getWidth() / 2 - (double) MainPane.getWIDTH() / 2);
            stage.setY(screenSize.getHeight() / 2 - (double) MainPane.getHEIGHT() / 2);
        } catch (Exception e) {
            alert.showAndWait();
            e.printStackTrace();
        }

    }

    private void setLanguage(Language language) {
        this.language = language; // Cập nhật ngôn ngữ hiện tại
        languageMap = language.getLanguage(); // Cập nhật languageMap
        // Cập nhật các thành phần giao diện người dùng
        player1.getItems().clear();
        player1.getItems().addAll(languageMap.get("human"), languageMap.get("easyai"));
        player1.setValue(languageMap.get("human"));
        player2.getItems().clear();
        player2.getItems().addAll(languageMap.get("human"), languageMap.get("easyai"));
        player2.setValue(languageMap.get("human"));
        alert.setTitle(languageMap.get("error"));
        alert.setHeaderText(languageMap.get("invalidInput"));
        alert.setContentText(languageMap.get("adviceInput"));
        startButton.setText(languageMap.get("newgame"));
        rowLabel.setText(languageMap.get("row"));
        colLabel.setText(languageMap.get("col"));
        player1NameLabel.setText(languageMap.get("player1Name"));
        player2NameLabel.setText(languageMap.get("player2Name"));
        player1Label.setText(languageMap.get("player1"));
        player2Label.setText(languageMap.get("player2"));
        player1Name.setText(languageMap.get("player1"));
        player2Name.setText(languageMap.get("player2"));
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        imageView.setImage(new javafx.scene.image.Image("file:src/main/resources/assets/img/icon.png"));
        languageOption.getItems().addAll("English", "Tiếng Việt");
        languageOption.setValue("English");
        languageOption.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            // Đặt hành động bạn muốn thực hiện khi mục được chọn thay đổi ở đây
            if (newValue.equals("English")) {
                setLanguage(Language.ENGLISH);
            } else if (newValue.equals("Tiếng Việt")){
                setLanguage(Language.VIETNAMESE);
            }
        });
        setLanguage(Language.ENGLISH);
    }
}
