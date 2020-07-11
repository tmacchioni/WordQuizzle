package database;


import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import utils.Friend;

public class Player{

	private String nickname;
	private String password;
	private int score;
	private ConcurrentHashMap<String, Friend> friends;
	private transient String status;

	private Player(String nickname, String password, int score, ConcurrentHashMap<String, Friend> friends) {
		this.nickname = nickname.toLowerCase();
		this.password = password;
		this.score = score;
		this.friends = friends;
	}

	public Player(String nickname, String password) throws IOException {
		this(nickname, password, 0, new ConcurrentHashMap<String, Friend>());
	}


	//Se l'amico esiste gi� allora lo sovrascrive, ritorna il vecchio valore dell'amico
	public Friend addFriend(Friend friend) throws IOException {
		return friends.put(friend.getNickname(), friend);
	}

	public Friend addFriend(Player player) throws IOException {
		Friend newFriend = new Friend(player.getNickname(), player.getScore());
		return this.addFriend(newFriend);
	}


	public String getNickname() {
		return nickname;
	}


	public void setNickname(String nickname) {
		this.nickname = nickname;
	}


	public int getScore() {
		return score;
	}


	public void setScore(int score) {
		this.score = score;
	}


	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public ConcurrentHashMap<String, Friend> getFriends() {
		return this.friends;
	}

	public void setFriends(ConcurrentHashMap<String, Friend> friends) {
		this.friends = friends;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}


	public Player clone() {
		return new Player(nickname, password, score, friends);
	}

	
	@Override
	public String toString() {
		return nickname;
	}


	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		int sum = 0;
		for(byte b : nickname.getBytes()) {
			sum += b;
		}
		return sum;
	}


	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return nickname.equals(((Player) obj).getNickname());
	}








}
