package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import utils.Message;
import utils.MessageSubType;
import utils.MessageType;
import utils.PlayerStats;

public class ChallengeListener  implements Runnable{

	private String hostname;
	private int port;
	private static String username;
	private ChallengeController challengeController;
	private WaitingFriendResultController waitingFriendResultController;
	private static ObjectOutputStream output;
	private ObjectInputStream input;

	@SuppressWarnings("unused")
	private boolean timeExpired;

	ChallengeListener(String username, String hostname, int port, ChallengeController challengeController, WaitingFriendResultController waitingFriendResultController) {
		ChallengeListener.username = username;
		this.hostname = hostname;
		this.port = port;
		this.challengeController = challengeController;
		this.waitingFriendResultController = waitingFriendResultController;
		this.timeExpired = false;
	}

	@Override
	public void run() {

		try(SocketChannel socket = SocketChannel.open()) {
			socket.connect(new InetSocketAddress(hostname, port));

			output = new ObjectOutputStream(socket.socket().getOutputStream());
			input = new ObjectInputStream(socket.socket().getInputStream());

			GameController.getInstance().showChallenge();
			sendReadyMsg();

			TimeoutThread timeoutThread = null;
			PlayerStats challengerStats = null;
			PlayerStats challengedStats = null;

			while(!Thread.currentThread().isInterrupted() && socket.isConnected()) {
				Message inputmsg = null;
				inputmsg = (Message) input.readObject();

				System.out.println("**************************");
				System.out.println(inputmsg.getUsername());
				System.out.println(inputmsg.getType());
				System.out.println(inputmsg.getSubtype());
				System.out.println(inputmsg.getMsg());
				System.out.println("**************************\n");

				if (inputmsg != null && (inputmsg.getType() == MessageType.CHALLENGE)) {
					switch(inputmsg.getSubtype()) {
					case READY:
						challengeController.setCountdownLabel((String) inputmsg.getMsg());
						sendReadyMsg();
						break;
					case START:
						int challengeTimeout = ((Integer)inputmsg.getMsg()).intValue();
						timeoutThread = new TimeoutThread(challengeTimeout, output);
						timeoutThread.start();
						challengeController.hideCountdownAndshowChallenge();
						break;
					case NEW_WORD:
						challengeController.setWordLabel((String) inputmsg.getMsg());
						break;
					case END:
						timeoutThread.interrupt();
						waitingFriendResultController.setTimeExpired(false);
						challengeController.showWaitingFriendResultScene();
						break;
					case YOUR_STATS:
						challengerStats = (PlayerStats) inputmsg.getMsg();
						break;
					case OTHER_STATS:
						challengedStats = (PlayerStats) inputmsg.getMsg(); 
						break;
					case FINISHED:
						waitingFriendResultController.showEndChallengeScene(challengerStats, challengedStats);
						socket.socket().close();
						return;
					default:
						break;
					}
				}
			}
		} catch (IOException | ClassNotFoundException e) {
			return;
		}
	}

	private void sendReadyMsg() throws IOException {
		write(MessageSubType.READY, null);
	}

	static void sendAnswer(String answer) throws IOException {
		write(MessageSubType.ANSWER, answer);
	}
	
	private static void write(MessageSubType subtype, Object obj) throws IOException {
		Message newMsg = new Message();
		newMsg.setUsername(username);
		newMsg.setType(MessageType.CHALLENGE);
		newMsg.setSubtype(subtype);
		newMsg.setMsg(obj);
		output.writeObject(newMsg);
		output.flush();
	}


	//thread per il timeout
	private class TimeoutThread extends Thread {

		private int challengeTimeout;
		private ObjectOutputStream output;

		public TimeoutThread(int challengeTimeout, ObjectOutputStream output) {
			this.setName("TimeoutThread");
			this.challengeTimeout = challengeTimeout;
			this.output = output;
		}

		@Override
		public void run() {
			while (challengeTimeout>0 && !TimeoutThread.interrupted()){
				challengeController.setTimeLeftLabel(String.valueOf(challengeTimeout/1000));
				try {
					challengeTimeout -= 1000;
					Thread.sleep(1000);
				}
				catch (InterruptedException e) {
					return;
				}
			}
			if(!TimeoutThread.interrupted()) {
				timeExpired = true;
				Message newMsg = new Message();
				newMsg.setUsername(username);
				newMsg.setType(MessageType.CHALLENGE);
				newMsg.setSubtype(MessageSubType.TIME_EXPIRED);
				try {
					output.writeObject(newMsg);
					output.reset();
				} catch (IOException e) {
					return;
				}
				
			}
		}
	}

}
