module com.khanhdew.testjfx {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;
    requires google.collections;
    requires java.sql;


    opens com.khanhdew.testjfx to javafx.fxml;
    exports com.khanhdew.testjfx;
    exports com.khanhdew.testjfx.view;
    opens com.khanhdew.testjfx.view to javafx.fxml;
}