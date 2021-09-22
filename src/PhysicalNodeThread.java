import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class PhysicalNodeThread extends Thread {
	
	private String nId;
	private Map<String,String> routingTable;
	private VirtualNode node;
	Connection connection;
	Channel channel;
	
	public PhysicalNodeThread(String nId, Map<String, String> routingTable, VirtualNode n) {
		this.nId = nId;
		this.routingTable = routingTable;
		this.node=n;
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		try {
			connection = factory.newConnection();
			channel =connection.createChannel();
			channel.queueDeclare(nId, false, false, false, null);
		} catch (Exception e) {
			System.err.println("couldn't creat queue for physical node:"+nId);
			e.printStackTrace();
		}
	}
	
	public void run(){
		
		//recieve messages
		DeliverCallback deliverCallback = (consumerTag, delivery) -> {
	    	try {
				ByteArrayInputStream bis = new ByteArrayInputStream(delivery.getBody());
    			ObjectInput in = new ObjectInputStream(bis);
	    		Message mes = (Message)in.readObject(); 
				System.out.println("I:"+nId+" received message:" +mes.getText()+" for nID:"+mes.getDestination());
				//FORWARD message 
				if(!mes.getDestination().equals(nId)) {
					try {
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						ObjectOutputStream out = new ObjectOutputStream(bos);   
						out.writeObject(mes);
						out.flush();
						byte[] bytes = bos.toByteArray();
						String nextHop = routingTable.get(mes.getDestination());
						this.channel.basicPublish("", nextHop, null, bytes);
						System.out.println("Node "+nId+" send meesage:" +mes.getText()+" for DESTINATION:"+mes.getDestination());
					} catch (Exception e) {
						System.err.println("Node "+nId+" couldn't send meesage:" +mes.getText()+" for DESTINATION:"+mes.getDestination());
					}	
					
				}
				//RECEIVE message
				else {
					System.out.println("I:"+nId+" got a MESSAGE!:"+mes.getText()+". Sending to host if any");
					//send to host
					if(node.getHostId()!=null) {
						String response = "RECIEVED:{"+mes.getText()+"}";
						this.channel.basicPublish("", node.getHostId(), null, response.getBytes());
					}
					
				}
		        
			} 
			catch(Exception ex){
				System.out.println("Could not deserialize message");
			}
		};

		//channel to listen for messages
		try {
			channel.basicConsume(nId, true, deliverCallback, consumerTag -> { });
		} catch (IOException e) {
			System.out.println("P-NodeId:"+nId+" ould not recieve message");
		}


	}

	

}
