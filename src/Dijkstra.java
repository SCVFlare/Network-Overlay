//import java.io.FileReader;
import java.util.ArrayList;
//import java.util.List;

//import com.opencsv.CSVReader;

public class Dijkstra {

	public static void arrayPrinter(int[] array) {
		for (int i = 0; i < array.length; i++) {
			System.out.print(array[i]);
		}
		System.out.println();
	}
	
	public static void graphPrinter (int[][] Graph, int nb_nodes) {
		System.out.println("");
		for (int i = 0; i < nb_nodes; i++) {
			for (int j = 0; j < nb_nodes; j++) {
				System.out.print(Graph[i][j] + " ");
				
			}
			System.out.println();			
		}
		System.out.println("");
	}
	
	public static int minFinder(ArrayList<Integer> Q, int[] dist) {
		int min = dist[Q.get(0)];
		int min_index = 0;
		for (int i = 0; i < Q.size(); i++) {
			if (dist[Q.get(i)] < min) {
				
				min = dist[Q.get(i)];
				min_index = i; 
			}
		}
		return Q.get(min_index);
	}

	
	public static int[] dijkstra(int[][] Graph, int size, int source) {
		
		int[] dist = new int[size];
		int[] prev = new int[size];
		
		ArrayList<Integer> Q = new ArrayList<Integer>();
		int alt;
		//step 1 init graphs
		for (int i = 0; i < size; i++) {
			dist[i] = Integer.MAX_VALUE;
			prev[i] = -1;
			Q.add(i);
		}
		;
		dist[source] = 0;
		int u;
		while (!(Q.isEmpty())) {
			u = minFinder(Q, dist);
			Q.remove(Integer.valueOf(u));

			for (int i = 0; i < Q.size(); i ++) {
				int v = Q.get(i);
				alt = dist[u] + Graph[v][u];
				if (alt < dist[v]) {
					dist[v] = alt;
					prev[v] = u;
				}
			}
			
		}
		//arrayPrinter(dist);
		//arrayPrinter(prev);
		return prev;
	}
	
	public static ArrayList<Integer> pathFinder(int[] prev, int source, int goal) {
		int current = goal;
		ArrayList<Integer> Path = new ArrayList<Integer>();
		Path.add(0, current);
		while (current != source) {
			Path.add(0,prev[current]);
			current = prev[current];
		}
		return Path;
	}
	
	public static int next(int[] prev, int source, int goal) {

		int current = goal;;
		
		if (source == goal) {
			return -1;
		}
		
		while (current != source) {
			if (prev[current] == source) {
				return current;
			}
			current = prev[current];
		}
		return current;
	}
	
	
	public static ArrayList<ArrayList<Integer>> routingTableBuilder(int[][] Graph){
		
		int nb_nodes = Graph.length;
		int[] prev = new int[nb_nodes];
		ArrayList<Integer> Next = new ArrayList<Integer>();
		ArrayList<ArrayList<Integer>> NextList = new ArrayList <ArrayList<Integer>>(nb_nodes);
		int next;
		
		for (int i = 0; i < nb_nodes; i++) {
			prev = dijkstra(Graph, nb_nodes, i);
			Next = new ArrayList<Integer>();
			for (int j = 0; j < nb_nodes; j++) {
				next = next(prev, i, j);
				Next.add(Integer.valueOf(next));
				
			}
			NextList.add(Next);
			
		}
		return NextList;
		
		
	}
	
	/*public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		
		//Parsing network topology
		String fn = "/home/boyan/Desktop/ids-labs/Project/src/main/resources/matrix.csv";
		int nb_nodes=0;
		List<String[]> r=null;
		try (CSVReader reader = new CSVReader(new FileReader(fn))) {
		      r = reader.readAll();
		      nb_nodes=r.size();
		  } catch (Exception e) {	
			e.printStackTrace();
			System.exit(1);
		}
		int[][] Graph = new int[nb_nodes][nb_nodes];
		
		for (int i = 0; i < nb_nodes; i++) {
			for (int j = 0; j < nb_nodes; j++) {
				Graph[i][j] = Integer.parseInt(r.get(i)[j]);
			}
			
		}
		graphPrinter(Graph, nb_nodes);
		
		ArrayList<ArrayList<Integer>> NextList = new ArrayList<ArrayList<Integer>>(nb_nodes);
		
		NextList = routingTableBuilder(Graph);
		for (int i = 0; i < nb_nodes; i++) {
			System.out.print(NextList.get(i));
			System.out.println("");
			
		}	
	}*/

}
