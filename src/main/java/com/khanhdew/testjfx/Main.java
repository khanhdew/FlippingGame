package com.khanhdew.testjfx;


import com.khanhdew.testjfx.view.GamePane;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    public static void main(String[] args) {
        launch(Main.class, args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Flipping");
        GamePane gamePane = new GamePane();
        stage.setScene(new Scene(gamePane, GamePane.WIDTH, GamePane.HEIGHT));
        stage.setResizable(false);
        stage.getIcons().add(new javafx.scene.image.Image("file:src/main/resources/assets/img/icon.png"));
        stage.show();
    }
}
