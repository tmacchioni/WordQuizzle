package server;


import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import properties.PropertiesReader;
import rmi.RemoteRegistrationImpl;


public class ServerMain {

	public static void main(String[] args) throws IOException {
		
		String host = PropertiesReader.getProperty("host");
		int port = Integer.parseInt(PropertiesReader.getProperty("port"));
		int RMIPort = Integer.parseInt(PropertiesReader.getProperty("RMIPort"));

		/*
		 * Generazione RMI
		 */
		RemoteRegistrationImpl rmtObj = null;
		Registry registry = null;
		
		try {

			rmtObj = new RemoteRegistrationImpl();

			/* Creazione di un registry sulla porta RMIPort */
			LocateRegistry.createRegistry(RMIPort);
			registry = LocateRegistry.getRegistry(RMIPort);


			/* Pubblicazione dello stub nel registry */
			registry.rebind("REG-SERVER", rmtObj);

			
			System.out.println("RMI is ready at " + InetAddress.getLocalHost().getHostAddress() + ":" + RMIPort);
		} catch(RemoteException e) {
			e.printStackTrace();
		}
		

		/*
		 * Impostazione ServerSocketChannel
		 */
		try(ServerSocketChannel ssc = ServerSocketChannel.open()){

			ssc.socket().bind(new InetSocketAddress(host, port));

			System.out.println("Server is listening at " +  InetAddress.getLocalHost().getHostAddress() + ":" + port);
			
			while(true) {
				SocketChannel client = ssc.accept();
				(new PlayerHandler(client)).start();
				System.out.println("New Player accepted");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		System.exit(0);

	}


}
