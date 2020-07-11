package database;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import utils.Friend;


//singleton lazy initialization
public class DatabaseConnection {

	private final static String JSON_PATH = ".\\src\\main\\resources\\database.json";

	private static volatile DatabaseConnection instance = null; //riferimento all'istanza
	private static Gson gson;


	//costruttore
	private DatabaseConnection() throws IOException {			

		gson = new GsonBuilder().setPrettyPrinting().create();
		
		File databaseFile = new File(JSON_PATH);

		if(databaseFile.createNewFile()) {
			FileWriter fw = new FileWriter(databaseFile);
			fw.write("{}");
			fw.close();
		}
	}


	//getIstance
	private static DatabaseConnection getInstance() throws IOException {
		//Double check locking patter
		if(instance==null) { //Check for the first time 
			synchronized(DatabaseConnection.class) {
				if( instance == null )//Check for the second time.
					instance = new DatabaseConnection();
			}
		}
		return instance;
	}

	
	public static boolean addPlayer(Player player) throws JsonIOException, JsonSyntaxException, IOException {
		if (instance == null) getInstance();

		String newNickname = player.getNickname();

		BufferedReader reader = new BufferedReader(new FileReader(JSON_PATH));
		Type hashMapType = new TypeToken<HashMap<String, Player>>() {}.getType();

		synchronized (DatabaseConnection.class) {

			HashMap<String, Player> playersMap = gson.fromJson(reader, hashMapType);
			
			reader.close();			

			if(playersMap.containsKey(newNickname)) return false;

			playersMap.put(newNickname, player);

			BufferedWriter writer = new BufferedWriter(new FileWriter(JSON_PATH));
			
			gson.toJson(playersMap, writer);
			writer.close();
			
			return true;
		}
		

	}

	
	public static boolean addPlayer(String nickname, String password) throws JsonIOException, JsonSyntaxException, IOException {
		Player newPlayer = new Player(nickname.toLowerCase(), password);
		return addPlayer(newPlayer);

	}

	
	public static boolean addSetOfPlayers(Set<Player> setPlayer) throws JsonIOException, JsonSyntaxException, IOException{

		boolean result = true;
		for(Player elm : setPlayer) {
			if( !addPlayer(elm)) result = false;
		}

		return result;

	}
	
	
	public static Friend addFriend(String nickname, String friendNickname) throws JsonIOException, JsonSyntaxException, IOException {
		if (instance == null) getInstance();

		BufferedReader reader = new BufferedReader(new FileReader(JSON_PATH));

		Type hashMapType = new TypeToken<HashMap<String, Player>>() {}.getType();

		synchronized (DatabaseConnection.class) {
			HashMap<String, Player> playersMap = gson.fromJson(reader, hashMapType);
			reader.close();

			if(!playersMap.containsKey(nickname) || !playersMap.containsKey(friendNickname)) return null;

			Player player = playersMap.get(nickname);
			Player friend = playersMap.get(friendNickname);

			Friend fplayer = new Friend(player.getNickname(), player.getScore());
			Friend ffriend =  new Friend(friend.getNickname(), friend.getScore());

			player.addFriend(ffriend);
			friend.addFriend(fplayer);

			playersMap.put(player.getNickname(), player);
			playersMap.put(friend.getNickname(), friend);

			BufferedWriter writer = new BufferedWriter(new FileWriter(JSON_PATH));
			gson.toJson(playersMap, writer);
			writer.close();

			return ffriend;
		}

	}


	
	//la modifica è assicurata da HashMap perchè il metodo put modifica una chiave esistente
	public static boolean update(Player player) throws JsonIOException, JsonSyntaxException, IOException {
		if (instance == null) getInstance();

		BufferedReader reader = new BufferedReader(new FileReader(JSON_PATH));

		Type hashMapType = new TypeToken<HashMap<String, Player>>() {}.getType();

		synchronized (DatabaseConnection.class) {
			HashMap<String, Player> playersMap = gson.fromJson(reader, hashMapType);
			reader.close();

			String newNickname = player.getNickname();


			if(!playersMap.containsKey(newNickname)) return false;

//			player = player.clone();

			playersMap.put(player.getNickname(), player);

			BufferedWriter writer = new BufferedWriter(new FileWriter(JSON_PATH));
			gson.toJson(playersMap, writer);
			writer.close();

			return true;
		}

	}

	
	public static Player getPlayerByName(String nickname, String password) throws JsonIOException, JsonSyntaxException, IOException {
		if (instance == null) getInstance();
		
		nickname = nickname.toLowerCase();

		BufferedReader reader = new BufferedReader(new FileReader(JSON_PATH));
		Type hashMapType = new TypeToken<HashMap<String, Player>>() {}.getType();

		synchronized (DatabaseConnection.class) {
			HashMap<String, Player> playersMap = gson.fromJson(reader, hashMapType);

			reader.close();

			if(!playersMap.containsKey(nickname)) return null;

			Player player = playersMap.get(nickname);

			if(player.getPassword().equals(password)) return player;
			else { return null; }

		}
	}
	
	
	public static String getFriendsOf(String nickname) throws JsonIOException, JsonSyntaxException, IOException {
		if (instance == null) getInstance();
		
		nickname = nickname.toLowerCase();

		BufferedReader reader = new BufferedReader(new FileReader(JSON_PATH));

		Type hashMapType = new TypeToken<HashMap<String, Player>>() {}.getType();

		synchronized (DatabaseConnection.class) {
			
			HashMap<String, Player> playersMap = gson.fromJson(reader, hashMapType);

			reader.close();

			if(!playersMap.containsKey(nickname)) return null;

			Player player = playersMap.get(nickname);
			ConcurrentHashMap<String, Friend> friendsMap = player.getFriends();

			JsonElement jelm = gson.toJsonTree(friendsMap);


			return jelm.toString();
		}
	}
	
	
	public static String getRankOf(String nickname) throws JsonIOException, JsonSyntaxException, IOException {
		if (instance == null) getInstance();
		
		nickname = nickname.toLowerCase();

		BufferedReader reader = new BufferedReader(new FileReader(JSON_PATH));

		Type hashMapType = new TypeToken<HashMap<String, Player>>() {}.getType();

		synchronized (DatabaseConnection.class) {
			HashMap<String, Player> playersMap = gson.fromJson(reader, hashMapType);

			reader.close();

			if(!playersMap.containsKey(nickname)) return null;

			Player player = playersMap.get(nickname);
			ConcurrentHashMap<String, Friend> friendsMap = player.getFriends();

			friendsMap.put(player.getNickname(), new Friend(player.getNickname(), player.getScore()));

			List<Friend> friendsList = new ArrayList<Friend>(friendsMap.values());
			Collections.sort(friendsList);
			System.out.println(friendsList.toString());
			JsonElement jelm = gson.toJsonTree(friendsList);

			return jelm.toString();
		}

	}


}
