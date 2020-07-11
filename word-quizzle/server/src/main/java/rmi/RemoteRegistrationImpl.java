package rmi;


import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import database.DatabaseConnection;
import properties.PropertiesReader;
import utils.Message;
import utils.MessageSubType;
import utils.MessageType;
import utils.RemoteRegistrationInterface;

public class RemoteRegistrationImpl extends UnicastRemoteObject implements RemoteRegistrationInterface{

	private static final long serialVersionUID = 1L;

	public RemoteRegistrationImpl() throws RemoteException {
		super();
	}


	public Message registra_utente(String nickUtente, String password) throws IOException {

		int maxNameLenght = Integer.parseInt(PropertiesReader.getProperty("maxNameLenght"));
		int maxPswLenght = Integer.parseInt(PropertiesReader.getProperty("maxPswLength"));


		Message newMsg = new Message();
		newMsg.setUsername("server");

		if("".equals(nickUtente) || nickUtente.length() > maxNameLenght) {
			newMsg.setType(MessageType.ERROR);
			newMsg.setSubtype(MessageSubType.NAME_FIELD);
			newMsg.setMsg("Max " + maxNameLenght + " caratteri.");
			return newMsg;
		}

		if("".equals(password) || password.length() > maxPswLenght) {
			newMsg.setType(MessageType.ERROR);
			newMsg.setSubtype(MessageSubType.PASS_FIELD);
			newMsg.setMsg("Max " + maxPswLenght + " caratteri.");
			return newMsg;
		}

		if(DatabaseConnection.addPlayer(nickUtente, password) == false) {//esiste già
			newMsg.setType(MessageType.ERROR);
			newMsg.setSubtype(MessageSubType.EXIST);
			return newMsg;
		}

		//nessun errore
		newMsg.setType(MessageType.OK);
		return newMsg;
	}


}
