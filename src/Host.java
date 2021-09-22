
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class Host {
	
	public static String hostId=UUID.randomUUID().toString();
	public static String hostQueue="hostQueue";
	public static Connection host_connection;
	public static Channel host_channel;
	public static boolean connected =false;
	public static String nodeId;
	public static String leftId;
	public static String rightId;
	
	public static void main(String[] args)  {
		
		//disconnect from virtual network
		Runtime.getRuntime().addShutdownHook(new Thread() 
	    { 
	      public void run() 
	      {  

	        try {
	        	ConnectionRequest cr= new ConnectionRequest(hostId,nodeId, false);
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ObjectOutputStream out = new ObjectOutputStream(bos);   
				out.writeObject(cr);
				out.flush();
				byte[] bytes = bos.toByteArray();
				host_channel.basicPublish("", hostQueue, null, bytes);
	        }
	        catch(Exception e) {

	        }
	    	
	      } 
	    }); 
		
		if(args.length!=1) {
			System.err.println("Usage: java -jar Host <node_number>");
	        System.exit(1);
		}
		nodeId=args[0];

		
		//connection
		ConnectionFactory factory = new ConnectionFactory();
	    factory.setHost("localhost");
		try{	
			host_connection = factory.newConnection();
	    	host_channel = host_connection.createChannel();
	    	host_channel.queueDeclare(hostId, false, false, false, null);
		}
		catch(Exception e){
			System.err.println("Could not create conneciton to host_channel");
		}
		
		//Ask Overlay for node slot on a rabbitMq channel, if error, exit and tell user,
		try {
			ConnectionRequest cr= new ConnectionRequest(hostId,nodeId, true);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bos);   
			out.writeObject(cr);
			out.flush();
			byte[] bytes = bos.toByteArray();
			host_channel.basicPublish("", hostQueue, null, bytes);		
		} 
		catch (Exception e) {
			System.err.println("Could not send nodeid to overlay");
			e.printStackTrace();
		}	
		
		//callback for recieveing confiramtion
		DeliverCallback deliverCallback = (consumerTag, delivery) -> {
			//Connection request
	    	try {
	    		ConnectionResponse cr;
	    		ByteArrayInputStream bis = new ByteArrayInputStream(delivery.getBody());
	    		ObjectInput in = new ObjectInputStream(bis);
	    		cr = (ConnectionResponse)in.readObject(); 
	    		if(cr.isSuccess()) {
	    			connected=true;
	    			leftId=cr.getLeftId();
	    			rightId=cr.getRightId();
	    		}
	    		else {
	    			connected=false;
	    		}
		        
			} 
			catch(Exception ex){
				
			}
	    	//message reception
	    	try {
	    		String message = new String(delivery.getBody());
	    		System.out.println(message);
		        
			} 
			catch(Exception ex){
				
			}
		};
		
		try {
			host_channel.basicConsume(hostId, false, deliverCallback, consumerTag -> { });
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Could not recieve confiramtion from overlay");
		}	
		
		//if success loop for sendRight and sendLeft commands
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if(connected==false) {
			System.err.println("Connection unsuccessful");
			System.exit(1);
		}
		System.out.println("Connected. Type 'exit' to disconnect,'left' to send to left,'right' to send to right, AFTERWARDS sedn message");
		Scanner userInput = new Scanner(System.in);
		while (userInput.hasNext()) {  
			
			String input = userInput.nextLine();
			Message m;
			switch(input){
				case "exit":
					userInput.close();
					System.exit(0);
					break;
				case "left":
					System.out.println("type message to send left");
					input = userInput.nextLine();
					try {
						m=new Message(leftId,input);
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						ObjectOutputStream out = new ObjectOutputStream(bos);   
						out.writeObject(m);
						out.flush();
						byte[] bytes = bos.toByteArray();
						host_channel.basicPublish("", nodeId, null, bytes);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				case "right":
					System.out.println("type message to send right");
					input = userInput.nextLine();
					try {
						m=new Message(rightId,input);
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						ObjectOutputStream out = new ObjectOutputStream(bos);   
						out.writeObject(m);
						out.flush();
						byte[] bytes = bos.toByteArray();
						host_channel.basicPublish("", nodeId, null, bytes);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				default:
					break;
			}
	
        }  
		userInput.close();
		
	}
	

}
