package com.khanhdew.testjfx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.BootstrapFX;

public class BootstrapFXExample extends Application {
    @Override
    public void start(Stage primaryStage) {
        VBox vbox = new VBox();
        vbox.setSpacing(10);

        Button btn = new Button("Click me");
        btn.getStyleClass().setAll("btn", "btn-primary");

        vbox.getChildren().add(btn);

        Scene scene = new Scene(vbox, 300, 250);
        scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());

        primaryStage.setTitle("BootstrapFX Example");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
