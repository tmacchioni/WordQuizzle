package utils;

import java.io.Serializable;


public class Message implements Serializable{
	
	private static final long serialVersionUID = 3745436914774335047L;
	
	private String username;
	private MessageType type;
	private MessageSubType subtype;
	private Object msg;
	private int port; //serve per la sfida
	
	public Message(String username, MessageType type, MessageSubType subtype, Object msg) {
		this.username = username;
		this.type = type;
		this.subtype = subtype;
		this.msg = msg;
	}
	
	public Message(String username, MessageType type, MessageSubType subtype) {
		this(username, type, subtype, new Object());
	}
	
	public Message() {
		this("", null, null, "");
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public MessageType getType() {
		return type;
	}

	public void setType(MessageType type) {
		this.type = type;
	}

	public MessageSubType getSubtype() {
		return subtype;
	}

	public void setSubtype(MessageSubType subtype) {
		this.subtype = subtype;
	}

	public Object getMsg() {
		return msg;
	}

	public void setMsg(Object msg) {
		this.msg = msg;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

}
