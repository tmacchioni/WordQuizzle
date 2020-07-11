package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import utils.Message;
import utils.MessageSubType;
import utils.MessageType;
import utils.PlayerStats;

public class ChallengeHandler extends Thread {

	private SocketChannel socket;
	private PlayerStats playerStats;
	private HashMap<String, ArrayList<String>> mapOfWords;
	private Set<String> keySet;
	private int challengeTimeout;
	private String rightPoints;
	private String wrongPoints;

	private ObjectInputStream socketInput;
	private ObjectOutputStream socketOutput;

	private static final int READY_COUNTDOWN = 3;

	public ChallengeHandler(SocketChannel socket, ObjectOutputStream socketOutput, ObjectInputStream socketInput, PlayerStats playerStats,HashMap<String, ArrayList<String>> mapOfWords, int challengeTimeout, String rightPoints, String wrongPoints) {
		this.setName("ChallengeHandler");
		this.socket = socket;
		this.socketOutput = socketOutput;
		this.socketInput = socketInput;
		this.playerStats = playerStats;
		this.mapOfWords = mapOfWords;
		this.keySet = mapOfWords.keySet();
		this.challengeTimeout = challengeTimeout;
		this.rightPoints = rightPoints;
		this.wrongPoints = wrongPoints;
	}

	@Override
	public void run() {

		try {
			int readyCoutdown = READY_COUNTDOWN;
			Iterator<String> iterWord = keySet.iterator();
			String lastWordSent = null;

			while (socket.isConnected()) {

				Message inputmsg = (Message) socketInput.readObject();

				System.out.println("**************************");
				System.out.println(inputmsg.getUsername());
				System.out.println(inputmsg.getType());
				System.out.println(inputmsg.getSubtype());
				System.out.println(inputmsg.getMsg());
				System.out.println("**************************\n");

				if (inputmsg != null && (inputmsg.getType() == MessageType.CHALLENGE)) {
					switch (inputmsg.getSubtype()) {
					case READY:
						if(readyCoutdown == 0) {
							write(MessageSubType.START, Integer.valueOf(challengeTimeout));
							lastWordSent = iterWord.next();
							write(MessageSubType.NEW_WORD, lastWordSent);
						} else {
							write(MessageSubType.READY, String.valueOf(readyCoutdown));
							readyCoutdown--;
							Thread.sleep(1000);
						}
						break;
					case NEW_WORD:
						if(iterWord.hasNext()) {
						} else {
							write(MessageSubType.END, null);
						}
						break;
					case ANSWER:
						checkAnswer(lastWordSent, (String) inputmsg.getMsg());
						if(iterWord.hasNext()) {
							lastWordSent = iterWord.next();
							write(MessageSubType.NEW_WORD, lastWordSent);
						} else {
							write(MessageSubType.END, null);
							return;
						}
						break;
					case TIME_EXPIRED:
						return;
					default:
						break;
					}
				}
			}
		} catch (InterruptedException | IOException | ClassNotFoundException e) {
			e.printStackTrace();
			return;
		} 
	}


	private void checkAnswer(String lastWordSent, String answer) {

		answer = answer.toLowerCase();
		ArrayList<String> translations = mapOfWords.get(lastWordSent);
		if(translations.contains(answer)) { //traduzione corretta
			playerStats.myPut(lastWordSent, rightPoints);
		} else {
			playerStats.myPut(lastWordSent, wrongPoints);
		}
	}


	private void write(MessageSubType subType, Object obj) throws IOException {
		Message newMsg = new Message();
		newMsg.setUsername("server");
		newMsg.setType(MessageType.CHALLENGE);
		newMsg.setSubtype(subType);
		newMsg.setMsg(obj);
		socketOutput.writeObject(newMsg);
		socketOutput.reset();
	}




	

}
