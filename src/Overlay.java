import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import com.opencsv.CSVReader;

//import Dijkstra.java;

public class Overlay {
	public static String hostQueue="hostQueue";
	public static List<VirtualNode>nodes=new ArrayList<VirtualNode>();
	public static List<PhysicalNodeThread>nodeThreads=new ArrayList<PhysicalNodeThread>();
	public static Connection host_connection;
	public static Channel host_channel;
	

	public static void main(String[] args) {
	 
		if(args.length!=1) {
			System.err.println("Usage: java -jar Overlay <csv topology file>");
	            System.exit(1);
		}

		//Parsing network topology
		String fn = args[0];
		int nb_nodes=0;
		List<String[]> r=null;
		try (CSVReader reader = new CSVReader(new FileReader(fn))) {
		      r = reader.readAll();
		      nb_nodes=r.size();
		  } catch (Exception e) {	
			System.err.println("Could not parse csv");
			System.exit(1);
		}

		
		//hugo start
		//Init routingtable algo

		int[][] Graph = new int[nb_nodes][nb_nodes];
		int entry;
		for (int i = 0; i < nb_nodes; i++) {
			for (int j = 0; j < nb_nodes; j++) {
				entry = Integer.parseInt(r.get(i)[j]);
				if (i == j) {
					Graph[i][j] = 0;
				}
				else {
					if (entry == 0){
						Graph[i][j] = 9;
					}
					else {
						Graph[i][j] = entry;
					}
				}
			}
			
		}
		
		//Dijkstra.graphPrinter(Graph, nb_nodes);
		ArrayList<ArrayList<Integer>> NextList = new ArrayList<ArrayList<Integer>>(nb_nodes);
		NextList = Dijkstra.routingTableBuilder(Graph);
		
		//hugo end
			
		
		// suppose we have more than 2 ndoes for now
		for (int i = 1; i < nb_nodes+1; i++) {
			ArrayList<Integer> list = NextList.get(i-1);
			Map<String,String> table = new HashMap<String,String>();
			
			
			//set up virtual topology of Nodes
			String nodeId = String.valueOf(i);
			String leftId;
			String rightId;
			if(i==1) {
				leftId=String.valueOf(nb_nodes);
				rightId=String.valueOf(i+1);
			}
			else if(i==nb_nodes) {
				leftId=String.valueOf(nb_nodes-1);
				rightId=String.valueOf(1);
			}
			else {
				leftId=String.valueOf(i-1);
				rightId=String.valueOf(i+1);
			}
			//set up physical topology of nodeThreads
			for(int j=1;j<nb_nodes+1;j++) {
				if(i!=j) {
					String nextHop=String.valueOf(list.get(j-1)+1);
					table.put(String.valueOf(j), nextHop);
				}
			}
			
			VirtualNode n = new VirtualNode(nodeId,leftId,rightId);
			nodes.add(n);
			PhysicalNodeThread nt = new PhysicalNodeThread(nodeId,table,n);
			nodeThreads.add(nt);

		}
		
		//strating physical nodes
		for(PhysicalNodeThread n:nodeThreads) {
			n.start();
		}


		//Wait on rabbitMq channel for hosts to connect.
		ConnectionFactory factory = new ConnectionFactory();
	    factory.setHost("localhost");

		try{
			host_connection = factory.newConnection();
	    	host_channel = host_connection.createChannel();
	    	host_channel.queueDeclare(hostQueue, false, false, false, null);
		}	
		catch(Exception e){
			System.err.println("Could not create conneciton to host_queue");
		}
	    
	    
	    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
	    	ConnectionRequest hc=null;
	    	try {
	    		ByteArrayInputStream bis = new ByteArrayInputStream(delivery.getBody());
	    		ObjectInput in = new ObjectInputStream(bis);
	    		hc = (ConnectionRequest)in.readObject(); 
	    		System.out.println("Received Connection request from host_id:"+hc.getHostId()+" for node_id:"+hc.getNodeId()+"for connect?:"+hc.getMode());
			}
	    	catch(Exception e) {
	    		System.out.println("Could not deserialize connection object");
	    	}
			VirtualNode target=getNode(hc.getNodeId());
			
			ConnectionResponse cr ;
			//treat connect request
			if(hc.getMode()==true) {
				if(target!=null && target.getHostId()==null) {
					System.out.println("CONNECT request satisfied for host_id:"+hc.getHostId()+" for node_id:"+hc.getNodeId());
					cr = new ConnectionResponse(true,target.getLeftId(),target.getRightId());
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					ObjectOutputStream out = new ObjectOutputStream(bos);   
					out.writeObject(cr);
					out.flush();
					byte[] bytes = bos.toByteArray();
					host_channel.basicPublish("", hc.getHostId(), null, bytes);
					target.setHostId(hc.getHostId());
		
				}
				else {
					System.out.println("CONNECT request NOT satisfied for host_id:"+hc.getHostId()+" for node_id:"+hc.getNodeId());
					cr = new ConnectionResponse(false,null,null);
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					ObjectOutputStream out = new ObjectOutputStream(bos);   
					out.writeObject(cr);
					out.flush();
					byte[] bytes = bos.toByteArray();
					host_channel.basicPublish("", hc.getHostId(), null, bytes);
				}
			}
			
			//disiconnect request
			if(hc.getMode()==false) {
				if(target!=null && target.getHostId()!=null && target.getHostId().equals(hc.getHostId())) {
					target.setHostId(null);
					System.out.println("DISCONNECT request satisfied for host_id:"+hc.getHostId()+" for node_id:"+hc.getNodeId());
				}
				else {
					System.out.println("DISCONNECT request NOT satisfied for host_id:"+hc.getHostId()+" for node_id:"+hc.getNodeId());
				}
				
			}
			
			
        };
        
        //wait for connceiond
        System.out.println("Waiting for connections from hosts. To exit press CTRL+C");

        	try {
        	host_channel.basicConsume(hostQueue, true, deliverCallback, consumerTag -> { });
        	} catch (IOException e) {
    			System.err.println("Could not recieve nodeId from host");
    		}
        
		
	}
	static private VirtualNode getNode(String nodeId){
		VirtualNode res=null;
		for(VirtualNode n:nodes){
			if(n.getNid().equals(nodeId)){
				res=n;
				break;
			}
		}
		return res;
	}


}
