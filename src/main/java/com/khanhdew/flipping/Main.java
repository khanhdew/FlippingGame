package com.khanhdew.flipping;


import com.khanhdew.flipping.utils.Language;
import com.khanhdew.flipping.utils.PlayerRegister;
import com.khanhdew.flipping.view.WelcomePaneController;
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
        PlayerRegister playerRegister = new PlayerRegister();
        playerRegister.setLanguage(Language.ENGLISH);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/khanhdew/flipping/welcomepane.fxml"));
        Parent welcomePane = loader.load();

        WelcomePaneController controller = loader.getController();
        controller.setStage(stage);

        Scene scene = new Scene(welcomePane, 800, 500);
        var cssUrl = getClass().getResource("/assets/css/style.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }
        stage.setScene(scene);
        stage.setResizable(true);
        var iconStream = getClass().getResourceAsStream("/assets/img/icon.png");
        if (iconStream != null) {
            stage.getIcons().add(new javafx.scene.image.Image(iconStream));
        }
        stage.show();
    }
}
