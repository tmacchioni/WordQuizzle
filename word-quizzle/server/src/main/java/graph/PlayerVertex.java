package graph;

import java.util.concurrent.ArrayBlockingQueue;

import database.Player;
import utils.Message;

public  class PlayerVertex {

	private String username;
	private Player player;
	private ArrayBlockingQueue<Message> queue;

	public PlayerVertex(String username, Player player, ArrayBlockingQueue<Message> queue) {
		this.username = username;
		this.player = player;
		this.queue = queue;
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public ArrayBlockingQueue<Message> getQueue() {
		return queue;
	}

	@Override
	public int hashCode() {
		int sum = 0;
		for(byte b : username.getBytes()) {
			sum += b;
		}
		return sum;
	}

	@Override
	public boolean equals(Object obj) {
		
		return username.equals(((PlayerVertex) obj).getUsername());
	}

	@Override
	public String toString() {

		return username;
	}
	
	
	
	
}
