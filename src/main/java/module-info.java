module com.khanhdew.flipping {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;
    requires google.collections;
    requires java.sql;


    opens com.khanhdew.flipping to javafx.fxml;
    exports com.khanhdew.flipping;
    exports com.khanhdew.flipping.view;
    opens com.khanhdew.flipping.view to javafx.fxml;
}