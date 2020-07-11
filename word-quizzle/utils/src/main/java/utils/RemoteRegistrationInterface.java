package utils;


import java.io.IOException;
import java.rmi.Remote;

public interface RemoteRegistrationInterface extends Remote{

	//registrazione utente
	public Message registra_utente(String nickUtente, String password) throws IOException;
}
