package properties;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import database.DatabaseConnection;


public class PropertiesReader{

	private static final String PROP_PATH = ".\\src\\main\\resources\\config.properties";

	private static volatile PropertiesReader instance = null; //riferimento all'istanza
	private static Properties prop;

	//costruttore
	private PropertiesReader() throws IOException {	
		if (instance != null) {
			throw new RuntimeException(
					"Use getInstance() method to get the single instance of this class.");
		}

		PropertiesReader.prop = new Properties();

		InputStream input = new FileInputStream(PROP_PATH);
		
		// load the properties file
		prop.load(input);

	}
	
	public static String getProperty(String property) throws IOException {
		if(instance==null) { //Check for the first time 
			synchronized(DatabaseConnection.class) {
				if( instance == null )//Check for the second time.
					instance = new PropertiesReader();
			}
		}
		
		return prop.getProperty(property);
		
	}

}
