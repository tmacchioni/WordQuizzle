package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Consumer;

import org.jgrapht.Graphs;

import database.DatabaseConnection;
import database.Player;
import graph.OnlinePlayersGraph;
import graph.PlayerVertex;
import utils.Friend;
import utils.Message;
import utils.MessageSubType;
import utils.MessageType;

public class PlayerHandler extends Thread{

	private String username;
	private Player player;
	private OnlinePlayersGraph graph;
	private PlayerVertex pvertex;
	
	private SocketChannel socket;
	private InputStream is;
	private ObjectInputStream socketInput;
	private OutputStream os;
	private ObjectOutputStream socketOutput;

	private ArrayBlockingQueue<Message> queue;

	private NotificationHandler nthread;

	public PlayerHandler(SocketChannel playerSocket) throws IOException {
		this.setName("PlayerHandler");
		this.graph = OnlinePlayersGraph.getIstance();
		this.socket = playerSocket;
		this.queue = new ArrayBlockingQueue<Message>(256);
	}

	public void run() {

		try {

			is = socket.socket().getInputStream();
			socketInput = new ObjectInputStream(is);
			os = socket.socket().getOutputStream();
			socketOutput = new ObjectOutputStream(os);

			boolean shutdown = false;
			while (socket.isConnected() && !shutdown) {
				Message inputmsg = (Message) socketInput.readObject();

				System.out.println("**************************");
				System.out.println(inputmsg.getUsername());
				System.out.println(inputmsg.getType());
				System.out.println(inputmsg.getSubtype());
				System.out.println(inputmsg.getMsg());
				System.out.println("**************************\n");
				if (inputmsg != null) {
					switch (inputmsg.getType()) {
					case LOGIN:
						login(inputmsg);
						break;
					case ADD:
						aggiungi_amico(inputmsg);
						break;
					case LIST:
						sendList(inputmsg);
						break;
					case CHALLENGE:
						parseChallangeResponse(inputmsg);
						break;
					case LOGOUT:
						logout();
						shutdown=true;
						break;
					default:
						break;
					}
				}
			}
		}catch (Exception e){
			e.printStackTrace();
			return;
		} finally {

			if(nthread != null)	nthread.interrupt();

			try {
				DatabaseConnection.update(player);
				socketOutput.close();
				socketInput.close();
				socket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			graph.removeVertex(pvertex);

			System.out.println("Il thread di " + player.getNickname() + " è terminato.");

		}

	}

	public void login(Message inputmsg) throws IOException, InterruptedException {

		player = DatabaseConnection.getPlayerByName(inputmsg.getUsername(), (String) inputmsg.getMsg());

		if(player == null) { //utente non trovato o password errata
			writeError(MessageSubType.FIELDS);			
			return;
		}

		username = player.getNickname();
		
		nthread = new NotificationHandler(player, socketOutput, socket.socket().getRemoteSocketAddress(), queue);
		nthread.start();


		if( addToGraph() == false) { //utente già online
			writeError(MessageSubType.YETONLINE);
			return;
		}

		player.setStatus("online");

		//tutto ok scrive al giocatore
		Message newMsg = new Message();
		newMsg.setUsername("server");
		newMsg.setType(MessageType.LOGGED);
		newMsg.setMsg(String.valueOf(player.getScore()));
		write(newMsg);

		//avverte i suoi amici che si è connesso
		Message frdMsg = new Message();
		frdMsg.setUsername("server");
		frdMsg.setType(MessageType.NOTIFICATION);
		frdMsg.setSubtype(MessageSubType.STATUS);
		frdMsg.setMsg( (Friend) new Friend(player.getNickname(), player.getScore(), player.getStatus()));
		writeToFriendQueue(frdMsg);
		return;	

	}

	//lo aggiunge anche se era già presente
	public boolean aggiungi_amico(Message inputmsg) throws IOException, InterruptedException {

		String nickUtente = inputmsg.getUsername();
		String nickAmico = (String) inputmsg.getMsg();

		if(player.getFriends().containsKey(nickAmico) || username.equals(nickAmico)) { //amico già in lista o si auto aggiunge
			writeError(MessageSubType.EXIST);
			return false;
		}

		Friend newFriend = DatabaseConnection.addFriend(nickUtente, nickAmico); 

		if(newFriend == null) {//il giocatore da aggiungere non esiste
			writeError(MessageSubType.NOTFOUND);
			return false;
		}

		PlayerVertex friendVertex = graph.getVertexByName(nickAmico);

		if(friendVertex != null) {//l'amico è online
			newFriend.setStatus(friendVertex.getPlayer().getStatus());
			graph.addEdge(pvertex, friendVertex);

			//messaggio di notifica all'amico
			ArrayBlockingQueue<Message> friendQueue = friendVertex.getQueue();
			Message newMsg = new Message();
			newMsg.setUsername("server");
			newMsg.setType(MessageType.NOTIFICATION);
			newMsg.setSubtype(MessageSubType.ADD);
			newMsg.setMsg(new Friend(player.getNickname(), player.getScore(), player.getStatus()));
			friendQueue.put(newMsg);

		}

		player.addFriend(newFriend);

		//messaggio all'utente richiedente 
		Message newMsg = new Message();
		newMsg.setUsername("server");
		newMsg.setType(MessageType.NOTIFICATION);
		newMsg.setSubtype(MessageSubType.ADDED);
		newMsg.setMsg((Friend) newFriend);
		write(newMsg);

		return true;

	}



	private boolean addToGraph() throws IOException {

		assert(player != null);
		pvertex = new PlayerVertex(username, player, queue);

		if(graph.addVertex(pvertex) == false) { //l'utente è già online
			writeError(MessageSubType.YETONLINE);
			return false; 
		}

		player.getFriends().forEach((k,v) ->  {
				PlayerVertex friendVertex = graph.getVertexByName(v.getNickname());
				if(friendVertex != null) {
					graph.addEdge(pvertex, friendVertex);
					v.setStatus(friendVertex.getPlayer().getStatus());
				}
			} );

		return true;
	}


	private void sendList(Message inputmsg) throws IOException {

		switch(inputmsg.getSubtype()) {
		case OFFLINE:
			//invio elenco di tutti gli amici
			String jsonList = DatabaseConnection.getFriendsOf(player.getNickname());

			System.out.println(jsonList);

			Message newMsg = new Message();
			newMsg.setUsername("server");
			newMsg.setType(MessageType.LIST);
			newMsg.setSubtype(MessageSubType.OFFLINE);
			newMsg.setMsg(jsonList);
			write(newMsg);
			break;
		case ONLINE:
			//invio elenco dei soli amici online
			List<PlayerVertex> listOfOnlineFriends = Graphs.neighborListOf(graph, pvertex);

			ArrayList<Friend> list = new ArrayList<>();

			listOfOnlineFriends.forEach(new Consumer<PlayerVertex>() {

				@Override
				public void accept(PlayerVertex pv) {
					String onlineFriendUsername = pv.getUsername();
					list.add(player.getFriends().get(onlineFriendUsername));
				}
			});

			newMsg = new Message();
			newMsg.setUsername("server");
			newMsg.setType(MessageType.LIST);
			newMsg.setSubtype(MessageSubType.ONLINE);
			System.out.println(list);
			newMsg.setMsg(list);
			write(newMsg);
			break;
		default:
			break;
		}


	}

	private void logout() throws IOException, InterruptedException {

		Message newMsg = new Message();
		newMsg.setUsername("server");
		newMsg.setType(MessageType.NOTIFICATION);
		newMsg.setSubtype(MessageSubType.STATUS);
		newMsg.setMsg((Friend) new Friend(player.getNickname(), player.getScore(), "offline"));
		writeToFriendQueue(newMsg);
		player.setStatus("offline");
	}

	

	private void write(Message msg) throws IOException {
		socketOutput.writeObject(msg);
		socketOutput.reset();
	}

	private void writeToFriendQueue(Message msg) throws IOException, InterruptedException {
		List<PlayerVertex> pvList = Graphs.neighborListOf(graph, pvertex);

		for (PlayerVertex pv : pvList) {
			if(pv.getUsername().equals(player.getNickname()) == false) { 
				pv.getQueue().put(msg);
			}
		}
	}

	private void writeError(MessageSubType mst) throws IOException {
		Message newMsg = new Message();
		newMsg.setUsername("server");
		newMsg.setType(MessageType.ERROR);
		newMsg.setSubtype(mst);
		write(newMsg);
	}

	private void parseChallangeResponse(Message msg) throws IOException {

		switch(msg.getSubtype()) {
		case INVITE:
			Friend friend = (Friend) msg.getMsg();
			PlayerVertex friendVertex = graph.getVertexByName(friend.getNickname());

			if(friendVertex != null) {//l'amico è online

				Challenge challengeThread = new Challenge(this.player, this.pvertex, this.socketOutput, friendVertex);
				challengeThread.start();

			}else {
				writeError(MessageSubType.OFFLINE);
			}
			break;
		default:
			break;
		}
	}








}
