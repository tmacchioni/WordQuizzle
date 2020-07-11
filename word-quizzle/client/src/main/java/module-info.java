module client {
	requires transitive javafx.controls;
    requires javafx.fxml;
    requires java.rmi;
    requires com.google.gson;
    requires transitive utils;
	requires javafx.base;
	requires transitive javafx.graphics;
    
    opens client to javafx.fxml, com.google.gson;
    exports client;
}