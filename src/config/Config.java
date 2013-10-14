package config;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;

import com.thoughtworks.xstream.XStream;

public class Config {

	private volatile static Config config = null;	
	
	
	private Config() {

		
	}
		
	public static Config getConfig(String configFile) {	
		if (config == null){
			try {
				config = (Config) readXmlObject(configFile);
				System.out.println("Reading Config Object from file "+configFile+".");
			} catch (Exception e) {
				e.printStackTrace();
				config = new Config();				
			}			
		}
		return config;
	}
	
	

	public static void saveConfig(String file) {
		XStream xstream = new XStream();
		try {
			ObjectOutputStream xmlOut = xstream.createObjectOutputStream(
					new OutputStreamWriter(new FileOutputStream(file), "UTF8"));
			try {
				xmlOut.writeObject(config);
			} finally {
				xmlOut.close();
			}
		} catch (IOException ex){
			ex.printStackTrace();
		}
	}

	
	private static Object readXmlObject(String file) {
		XStream xstream = new XStream();
		Object outObj = null;
		
		try {
			ObjectInputStream xmlIn = xstream.createObjectInputStream(
					new InputStreamReader(new FileInputStream(file), "UTF8"));
			try {
				outObj = xmlIn.readObject();
			} finally {
				xmlIn.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException ex){
			ex.printStackTrace();
		}
		return outObj; 
	}
}