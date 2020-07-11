package server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.ArrayBlockingQueue;

import database.DatabaseConnection;
import database.Player;
import utils.Friend;
import utils.Message;

public class NotificationHandler extends Thread{

	private Player player;
	private ObjectOutputStream socketOutput;
	private SocketAddress clientAddress;
	private ArrayBlockingQueue<Message> queue;

	public NotificationHandler(Player player, ObjectOutputStream socketOutput, SocketAddress clientAddress, ArrayBlockingQueue<Message> queue) {
		this.setName("NotificationHandler");
		this.player = player;
		this.socketOutput = socketOutput;
		this.clientAddress = clientAddress;
		this.queue = queue;
	}

	public void run() {

		while(!Thread.currentThread().isInterrupted()) {
			
			try {
				Message inputmsg = (Message) queue.take();

				switch(inputmsg.getType()) {
				case NOTIFICATION:
					parseNotification(inputmsg);
					break;
				default:
					break;
				}
			} catch (IOException | InterruptedException e) {
				return;
			}
		}
		
		return;


	}

	private void parseNotification(Message message) throws IOException {

		Friend friend = null;

		switch(message.getSubtype()) {
		case STATUS: //cambio di stato di un amico
			friend = (Friend) message.getMsg();
			player.getFriends().put(friend.getNickname(), friend);
			DatabaseConnection.update(player);
			write(message);
			break;
		case ADD:
			player.addFriend((Friend) message.getMsg());
			write(message);
			break;
		case INVITE:
			sendInvite(message);
			break;
		default:
			break;
		}
	}
	
	

	private void sendInvite(Message message) throws IOException {

		DatagramChannel dc = DatagramChannel.open();

		ByteArrayOutputStream bstream = new ByteArrayOutputStream();
		ObjectOutputStream ostream = new ObjectOutputStream(bstream);
		ostream.writeObject(message);
		ByteBuffer buffer = ByteBuffer.allocate(bstream.size());	   
		buffer.put(bstream.toByteArray());
		buffer.flip();

		dc.send(buffer, clientAddress);
	}

	private void write(Message msg) throws IOException {
		socketOutput.writeObject(msg);
		socketOutput.reset();
	}

}
