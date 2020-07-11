package client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import utils.Friend;
import utils.Message;
import utils.MessageSubType;
import utils.MessageType;

public class GameListener implements Runnable{

	private Socket socket;
	private String hostname;
	private int port;
	private static String username;
	private String password;
	private String score;
	private static GameController gameController;
	private OutputStream os;
	private static ObjectOutputStream output;
	private InputStream is;
	private ObjectInputStream input;
	private int challengeServerPort;

	public GameListener(String hostname, int port, String username, String password, GameController controller) {
		this.hostname = hostname;
		this.port = port;
		GameListener.username = username;
		this.password = password;
		GameListener.gameController = controller;
	}

	@Override
	public void run() {

		try {
			socket = new Socket(hostname, port);
			os = socket.getOutputStream();
			output = new ObjectOutputStream(os);
			is = socket.getInputStream();
			input = new ObjectInputStream(is);

			

		} catch (IOException e) {
			LoginController.getInstance().showErrorDialog("Could not connect to server");
		}

		try {
			login(username, password);
			
			Thread UDPThread = new Thread(new Runnable() {

				@Override
				public void run() {
					DatagramChannel dc;
					try {
						dc = DatagramChannel.open();
						dc.bind(socket.getLocalSocketAddress());
						ByteBuffer buffer = java.nio.ByteBuffer.allocate(1024);

						while(!Thread.interrupted()) {
							dc.receive(buffer);
							buffer.flip();
							byte[] byts = new byte[buffer.limit()];
							buffer.get(byts);
							ObjectInputStream istream = new ObjectInputStream(new ByteArrayInputStream(byts));
							Message message = (Message) istream.readObject();
							buffer.clear();
							Friend challenger = (Friend) message.getMsg();
							challengeServerPort = message.getPort();
							gameController.showInvitationAlert(challenger.getNickname(), String.valueOf(challenger.getScore()), hostname, challengeServerPort);
						}
//						return;
					} catch (IOException | ClassNotFoundException | InterruptedException | ExecutionException e) {
						e.printStackTrace();
					}finally {
						try {
							output.close();
							input.close();
							socket.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					return;
				}
			});

			UDPThread.start();
			
			while (socket.isConnected() && !Thread.currentThread().isInterrupted()) {
				Message inputmsg = null;
				inputmsg = (Message) input.readObject();

				System.out.println("**************************");
				System.out.println(inputmsg.getUsername());
				System.out.println(inputmsg.getType());
				System.out.println(inputmsg.getSubtype());
				System.out.println(inputmsg.getMsg());
				System.out.println("**************************\n");

				if (inputmsg != null) {
					switch (inputmsg.getType()) {
					case ERROR:
						parseError(inputmsg);
						break;
					case LOGGED:
						score = String.valueOf(inputmsg.getMsg());
						lista_amici(username);
						initializeController(inputmsg);
						break;
					case LIST:
						setFriendsTable(inputmsg);
						break;
					case NOTIFICATION:
						parseNotification(inputmsg);
						break;
					case CHALLENGE:
						parseChallenge(inputmsg);
						break;
					default:
						break;
					}
				}
			}
			
		} catch (IOException | ClassNotFoundException e) {
//			e.printStackTrace();
			gameController.logoutScene();
		} finally {
			try {
				input.close();
				output.close();
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}

	public void login(String username, String password) throws IOException {
		Message newMsg = new Message();
		newMsg.setUsername(username);
		newMsg.setType(MessageType.LOGIN);
		newMsg.setMsg(password);
		write(newMsg);
	}

	public void lista_amici(String username) throws IOException {
		Message newMsg1 = new Message();
		newMsg1.setUsername(username);
		newMsg1.setType(MessageType.LIST);
		newMsg1.setSubtype(MessageSubType.OFFLINE);
		write(newMsg1);

		Message newMsg2 = new Message();
		newMsg2.setUsername(username);
		newMsg2.setType(MessageType.LIST);
		newMsg2.setSubtype(MessageSubType.ONLINE);
		write(newMsg2);
	}

	public void initializeController(Message message) throws IOException {
		gameController.setUsername(username);
		gameController.setPoints((String)message.getMsg());

	}

	public void setFriendsTable(Message message) throws IOException {
		switch(message.getSubtype()) {
		case OFFLINE:
			Gson gson = new Gson();
			String jsonList = (String) message.getMsg();
			Type hashMapType = new TypeToken<ConcurrentHashMap<String, Friend>>() {}.getType();
			ConcurrentHashMap<String, Friend> map = gson.fromJson(jsonList, hashMapType);
			Collection<Friend> friends = map.values();
			friends.forEach(new Consumer<Friend>() {
				@Override
				public void accept(Friend f) {
					f.setStatus("offline");
				}
			});
			gameController.addRows(friends);
			break;
		case ONLINE:
			ArrayList<Friend> listOfOnlineFriends =  (ArrayList<Friend>) message.getMsg();
			gameController.setFriendsOnline(listOfOnlineFriends);
			LoginController.getInstance().showScene();
			break;
		default:
			break;
		}
	}

	public static void sfida(Friend friend) throws IOException {
		Message newMsg = new Message();
		newMsg.setUsername(username);
		newMsg.setType(MessageType.CHALLENGE);
		newMsg.setSubtype(MessageSubType.INVITE);
		newMsg.setMsg(friend);
		write(newMsg);
	}

	public static void logout() throws IOException {
		Message newMsg = new Message();
		newMsg.setUsername(username);
		newMsg.setType(MessageType.LOGOUT);
		write(newMsg);
	}

	public static void aggiungi_amico(String friendToAdd) throws IOException {
		Message newMsg = new Message();		
		newMsg.setUsername(username);
		newMsg.setType(MessageType.ADD);
		newMsg.setMsg((String) friendToAdd);
		write(newMsg);
	}

	public static void rejectChallange() throws IOException {
		Message newMsg = new Message();
		newMsg.setUsername(username);
		newMsg.setType(MessageType.CHALLENGE);
		newMsg.setSubtype(MessageSubType.REJECTED);
		write(newMsg);
	}

	private static void write(Message newMsg) throws IOException {
		output.writeObject(newMsg);
		output.flush();
	}

	private void parseChallenge(Message message) throws IOException {

		switch(message.getSubtype()) {
		case ACCEPTED:
			challengeServerPort = message.getPort();
			gameController.startChallenge(username, score, hostname, challengeServerPort);
			break;
		case REJECTED:
			gameController.hideWaitingAlertScene();
			break;
		default:
			break;
		}
	}

	private void parseNotification(Message message) throws IOException {

		switch(message.getSubtype()) {
		case STATUS: //nuovo stato amico
			gameController.updateRow((Friend) message.getMsg());
			break;
		case ADDED:
			gameController.updateRow((Friend) message.getMsg());
			gameController.showTick();
			break;
		case ADD:
			gameController.updateRow((Friend) message.getMsg());
			break;
		default:
			break;
		}
	}


	private void parseError(Message message) throws IOException {

		switch(message.getSubtype()) {
		case NOTFOUND:
			gameController.showErrorMsg("Giocatore non trovato!");
			break;
		case EXIST:
			gameController.showErrorMsg("Siete già amici!");
			break;
		case YETONLINE:
			LoginController.getInstance().showErrorFields();
			break;
		case FIELDS:
			LoginController.getInstance().showErrorFields();
			break;
		case OFFLINE:
			break;
		default:
			break;
		}

	}
	
	



}
