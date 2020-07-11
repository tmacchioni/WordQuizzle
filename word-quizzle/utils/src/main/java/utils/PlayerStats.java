package utils;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

public class PlayerStats implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String username;
	private LinkedHashMap<String, String> resultsMap;
	private int totPoints;
	private boolean won;
	
	public PlayerStats(String username, int sizeOfMap) {
		this.username = username;
		this.resultsMap = new LinkedHashMap<>(sizeOfMap);
		this.totPoints = 0;
		this.won = false;
	}
	
	public LinkedHashMap<String, String> getResultsMap() {
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
	
	public String getTotPointsAsString() {
		return (totPoints >= 0) ? String.valueOf("+"+this.totPoints) : String.valueOf(this.totPoints);
	}

	public boolean hasWon() {
		return won;
	}

	public void setWon(boolean won) {
		this.won = won;
	}
	
	
}
