module utils {
	requires java.rmi;
	requires com.google.gson;
	
	opens utils to com.google.gson, javafx.base;
	exports utils to client, server;

}