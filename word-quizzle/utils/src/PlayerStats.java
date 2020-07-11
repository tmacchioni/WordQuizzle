package server;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class PlayerStats {
	private String username;
	private HashMap<String, String> resultsMap;
	private int totPoints;
	private boolean won;
	
	public PlayerStats(String username, int sizeOfMap) {
		this.username = username;
		this.resultsMap = new HashMap<>(sizeOfMap);
		this.totPoints = 0;
		this.won = false;
	}
	
	public HashMap<String, String> getResultsMap() {
		return this.resultsMap;
	}
	
	public void initializeResultsMap(Set<String> keySet, String value) {
		
		Iterator<String> iterSet = keySet.iterator();
		while(iterSet.hasNext()) {
			resultsMap.put(iterSet.next(), value);
		}
	}
	
	public String myPut(String key, String value) {
		String prevKey = resultsMap.put(key, value);
		totPoints += Integer.valueOf(value);
		return prevKey;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void addToTot(int x) {
		this.totPoints += x;
	}
	
	public int getTotPoints() {
		return this.totPoints;
	}

	public boolean isWon() {
		return won;
	}

	public void setWon(boolean won) {
		this.won = won;
	}
	
	
}
