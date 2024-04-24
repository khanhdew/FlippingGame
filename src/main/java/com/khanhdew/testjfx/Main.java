package com.khanhdew.testjfx;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    public static void main(String[] args) {
        launch(Main.class, args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Flipping");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("welcomepane.fxml"));
        Parent welcomePane = loader.load();

        WelcomePaneController controller = loader.getController();
        controller.setStage(stage);


        Scene scene = new Scene(welcomePane, 500, 500);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.getIcons().add(new javafx.scene.image.Image("file:src/main/resources/assets/img/icon.png"));
        stage.show();
    }
}
