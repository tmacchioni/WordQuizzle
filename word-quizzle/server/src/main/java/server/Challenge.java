package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jgrapht.Graphs;

import database.Player;
import graph.OnlinePlayersGraph;
import graph.PlayerVertex;
import properties.PropertiesReader;
import rest.MyMemoryTranslator;
import utils.Friend;
import utils.Message;
import utils.MessageSubType;
import utils.MessageType;
import utils.PlayerStats;

public class Challenge extends Thread{

	private Player challengerPlayer;
	private Player challengedPlayer;
	private PlayerVertex challengerVertex;
	private ObjectOutputStream challengerOutput;
	private PlayerVertex challengedVertex;
	private HashMap<String, ArrayList<String>> mapOfWords;

	public Challenge(Player challengerPlayer, PlayerVertex challengerVertex, ObjectOutputStream challengerOutput, PlayerVertex challengedVertex) {
		this.setName("Challenge");
		this.challengerPlayer = challengerPlayer;
		this.challengerVertex = challengerVertex;
		this.challengerOutput = challengerOutput;
		this.challengedVertex = challengedVertex;
	}

	public void run() {

		try {
			challengedPlayer = challengedVertex.getPlayer();
			
			challengerPlayer.setStatus("playing");
			challengerPlayer.getFriends().put(challengedPlayer.getNickname(), new Friend(challengedPlayer.getNickname(), challengedPlayer.getScore(), "playing"));
			
			challengedPlayer.setStatus("playing");
			challengedPlayer.getFriends().put(challengerPlayer.getNickname(), new Friend(challengerPlayer.getNickname(), challengerPlayer.getScore(), "playing"));

			final int APPROVAL_TIMEOUT = Integer.valueOf(PropertiesReader.getProperty("approvalTimeout"));
			final int CHALLENGE_TIMEOUT = Integer.valueOf(PropertiesReader.getProperty("challengeTimeout"));
			final int NUM_WORDS = Integer.valueOf(PropertiesReader.getProperty("numWords"));
			final String RIGHT_POINTS = PropertiesReader.getProperty("rightPoints");
			final String WRONG_POINTS = PropertiesReader.getProperty("wrongPoints");
			final String EXTRA_POINTS = PropertiesReader.getProperty("extraPoints");

			//inializzo una nuova socket lato server
			ServerSocketChannel serverChannel = ServerSocketChannel.open();
			serverChannel.socket().bind(new InetSocketAddress(0));

			//mando notifica al client sfidato con la porta con cui collegarsi
			Message newMsg = new Message();
			newMsg.setUsername("server");
			newMsg.setType(MessageType.NOTIFICATION);
			newMsg.setSubtype(MessageSubType.INVITE);
			newMsg.setMsg(new Friend(challengerPlayer.getNickname(), challengerPlayer.getScore()));
			newMsg.setPort(serverChannel.socket().getLocalPort());
			challengedVertex.getQueue().put(newMsg);

			serverChannel.socket().setSoTimeout(APPROVAL_TIMEOUT);
			Socket challengedSocket; //dopo lo incapsulo in un channel perché altrimenti in NIO non funziona il timeout
			try {
				challengedSocket = serverChannel.socket().accept();//lo sfidato è connesso
			}catch(SocketTimeoutException e) {
				//timeout scaduto, si comunica un rigetto al client challenger e si esce dal thread
				newMsg = new Message();
				newMsg.setUsername("server");
				newMsg.setType(MessageType.CHALLENGE);
				newMsg.setSubtype(MessageSubType.REJECTED);
				challengerOutput.writeObject(newMsg);
				challengerOutput.reset();
				return;
			}
			
			
			//si notifica a tutti i suoi amici che sta giocando			
			Message notifyMsg = new Message();
			notifyMsg.setUsername(challengedPlayer.getNickname());
			notifyMsg.setType(MessageType.NOTIFICATION);
			notifyMsg.setSubtype(MessageSubType.STATUS);
			notifyMsg.setMsg( new Friend(challengedPlayer.getNickname(), challengedPlayer.getScore(), "playing"));
			
			OnlinePlayersGraph graph = OnlinePlayersGraph.getIstance();
			List<PlayerVertex> pvList = Graphs.neighborListOf(graph, challengedVertex);

			for (PlayerVertex pv : pvList) {
				if(pv.getUsername().equals(challengedPlayer.getNickname()) == false) { 
					pv.getQueue().put(notifyMsg);
				}
			}
			
			SocketChannel challengedChannel = challengedSocket.getChannel();	

			//si comunica l'accettazione al client challenger e si attende che si connetta alla porta
			newMsg = new Message();
			newMsg.setUsername("server");
			newMsg.setType(MessageType.CHALLENGE);
			newMsg.setSubtype(MessageSubType.ACCEPTED);
			newMsg.setPort(serverChannel.socket().getLocalPort());
			challengerOutput.writeObject(newMsg);
			challengerOutput.reset();

			serverChannel.socket().setSoTimeout(0);
			SocketChannel challengerChannel = serverChannel.accept();
			//lo sfidante è connesso
			
			//si notifica a tutti i suoi amici che sta giocando
			notifyMsg = new Message();
			notifyMsg.setUsername(challengerPlayer.getNickname());
			notifyMsg.setType(MessageType.NOTIFICATION);
			notifyMsg.setSubtype(MessageSubType.STATUS);
			notifyMsg.setMsg(new Friend(challengerPlayer.getNickname(), challengerPlayer.getScore(), "playing"));
			
			pvList = Graphs.neighborListOf(graph, challengerVertex);

			for (PlayerVertex pv : pvList) {
				if(pv.getUsername().equals(challengerPlayer.getNickname()) == false) { 
					pv.getQueue().put(notifyMsg);
				}
			}

			//si costruisce una HashMap di NUM_WORDS 
			mapOfWords = MyMemoryTranslator.getRandomTranslations(NUM_WORDS);

			ObjectOutputStream newChallengerOutput = new ObjectOutputStream(challengerChannel.socket().getOutputStream());
			ObjectOutputStream newChallengedOutput = new ObjectOutputStream(challengedChannel.socket().getOutputStream());
			ObjectInputStream newChallengerInput = new ObjectInputStream(challengerChannel.socket().getInputStream());
			ObjectInputStream newChallengedInput = new ObjectInputStream(challengedChannel.socket().getInputStream());

			PlayerStats challengerStats = new PlayerStats(challengerPlayer.getNickname(), mapOfWords.size());
			challengerStats.initializeResultsMap(mapOfWords.keySet(), "0");
			
			PlayerStats challengedStats = new PlayerStats(challengedPlayer.getNickname(), mapOfWords.size());
			challengedStats.initializeResultsMap(mapOfWords.keySet(), "0");
			
			ChallengeHandler challengerThread = new ChallengeHandler(challengerChannel, newChallengerOutput, newChallengerInput, challengerStats, mapOfWords, CHALLENGE_TIMEOUT, "+"+RIGHT_POINTS, WRONG_POINTS);
			ChallengeHandler challengedThread = new ChallengeHandler(challengedChannel, newChallengedOutput, newChallengedInput, challengedStats, mapOfWords, CHALLENGE_TIMEOUT, "+"+RIGHT_POINTS, WRONG_POINTS);
			challengerThread.start();
			challengedThread.start();
			challengerThread.join();
			challengedThread.join();
			
			int challengerTotPoints =challengerStats.getTotPoints();
			int challengedTotPoints = challengedStats.getTotPoints();
			
			
			if(challengerTotPoints > challengedTotPoints){
				challengerStats.setWon(true);
				challengerStats.myPut("Extra points:", "+"+EXTRA_POINTS);
				challengerTotPoints += Integer.valueOf(EXTRA_POINTS).intValue();
			}else if(challengerTotPoints < challengedTotPoints) {
				challengedStats.setWon(true);
				challengedStats.myPut("Extra points:", "+"+EXTRA_POINTS);
				challengedTotPoints += Integer.valueOf(EXTRA_POINTS).intValue();
			}
			
			sendStats(challengerStats, newChallengerOutput, MessageSubType.YOUR_STATS);
			sendStats(challengedStats, newChallengedOutput, MessageSubType.YOUR_STATS);
			
			sendStats(challengerStats, newChallengedOutput, MessageSubType.OTHER_STATS);
			sendStats(challengedStats, newChallengerOutput, MessageSubType.OTHER_STATS);

			challengerPlayer.setStatus("online");
			challengerPlayer.setScore(challengerPlayer.getScore() + challengerTotPoints);
			challengerPlayer.getFriends().put(challengedPlayer.getNickname(), new Friend(challengedPlayer.getNickname(), challengedPlayer.getScore(), "online"));
			sendNewStatusAndScoreToFriends(challengerVertex, new Friend(challengerPlayer.getNickname(), challengerPlayer.getScore(), "online"));
			
			
			challengedPlayer.setStatus("online");
			challengedPlayer.setScore(challengedPlayer.getScore() + challengedTotPoints);
			challengedPlayer.getFriends().put(challengerPlayer.getNickname(), new Friend(challengerPlayer.getNickname(), challengerPlayer.getScore(), "online"));
			sendNewStatusAndScoreToFriends(challengedVertex, new Friend(challengedPlayer.getNickname(), challengedPlayer.getScore(), "online") );
	
			
			newMsg = new Message();
			newMsg.setUsername("server");
			newMsg.setType(MessageType.CHALLENGE);
			newMsg.setSubtype(MessageSubType.FINISHED);
			newChallengerOutput.writeObject(newMsg);
			newChallengerOutput.reset();
			newChallengedOutput.writeObject(newMsg);
			newChallengedInput.reset();
					
			return;
				
		} catch(IOException | InterruptedException e) {
			return;
		}
	}
	
	
	public void sendStats(PlayerStats stats, ObjectOutputStream output, MessageSubType subType) throws IOException {
		Message newMsg = new Message();
		newMsg.setUsername("server");
		newMsg.setType(MessageType.CHALLENGE);
		newMsg.setSubtype(subType);
		newMsg.setMsg(stats);
		output.writeObject(newMsg);
		output.reset();
	}
	
	
	public void sendNewStatusAndScoreToFriends(PlayerVertex vertex, Friend friendObj) throws IOException, InterruptedException {
		Message newMsg = new Message();
		newMsg.setUsername("server");
		newMsg.setType(MessageType.NOTIFICATION);
		newMsg.setSubtype(MessageSubType.STATUS);
		newMsg.setMsg( friendObj);
		
		List<PlayerVertex> pvList = Graphs.neighborListOf(OnlinePlayersGraph.getIstance(), vertex);

		for (PlayerVertex pv : pvList) {
			if(pv.getUsername().equals(friendObj.getNickname()) == false) { 
				pv.getQueue().put(newMsg);
			}
		}
	}
}
