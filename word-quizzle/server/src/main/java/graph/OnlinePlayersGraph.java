package graph;


import java.util.Set;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.concurrent.AsSynchronizedGraph;

//Grafo dei Player online sincronizzato
@SuppressWarnings("serial")
public class OnlinePlayersGraph extends AsSynchronizedGraph<PlayerVertex, DefaultEdge>{

	private static OnlinePlayersGraph instance = null;

	private OnlinePlayersGraph() {
		super(new SimpleGraph<PlayerVertex, DefaultEdge>(DefaultEdge.class));		 
	}

	public static OnlinePlayersGraph getIstance() {
		if(instance==null) {
			synchronized(OnlinePlayersGraph.class) {
				if( instance == null )
					instance = new OnlinePlayersGraph();
			}
		}
		return instance;
	}

		
	public synchronized PlayerVertex getVertexByName(String nickname) {
		
		Set<PlayerVertex> vertexSet = vertexSet();

		for(PlayerVertex vertex : vertexSet) {
			if(vertex.getUsername().equals(nickname)) return vertex;
		}
		
		return null;
	}

	public synchronized boolean addEdge(PlayerVertex vertex, String nickname) {
		
		PlayerVertex friend = this.getVertexByName(nickname);
		
		if(friend == null) return false;
		else {
			this.addEdge(vertex, friend);
			return true;
		}
		
	}
	
	

}


