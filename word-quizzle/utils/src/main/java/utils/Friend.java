package utils;

import java.io.Serializable;

import com.google.gson.annotations.Expose;

@SuppressWarnings("serial")
public class Friend implements Comparable<Friend>, Serializable{

	private String nickname;
	private int score;

	@Expose (serialize = false, deserialize = false) //così da escluderlo nella serializzazione di Gson e non quello di Java
	private String status = "offline";

	public Friend(String nickname, int score, String status) {
		this.nickname = nickname;
		this.score = score;
		this.status = status;
	}

	public Friend(String nickname, int score) {
		this(nickname, score, "offline");
	}

	public Friend(String nickname) {
		this(nickname, 0);
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname.toLowerCase();
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return nickname;
	}


	@Override
	public int hashCode() {
		int sum = 0;
		for(byte b : nickname.getBytes()) {
			sum += b;
		}
		return sum;
	}


	@Override
	public boolean equals(Object obj) {
		return nickname.equals(((Friend) obj).getNickname());
	}

	public int compareTo(Friend elm) {
		int elmScore = elm.getScore();

		int cmp =  score > elmScore ? +1 : score < elmScore ? -1 : 0;

		return cmp;

	}	

}
