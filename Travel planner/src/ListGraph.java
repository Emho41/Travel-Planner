import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class ListGraph<T> implements Graph<T> {
	
	//nodes are places
	private Map <T, Set<Edge<T>>> nodes = new HashMap<>();
	
	@Override
	public void add(T node) {
		nodes.putIfAbsent(node, new HashSet<Edge<T>>());
		
	}
	
	@Override
	public void connect(T a, T b, String name, int weight) {
		if(nodes.containsKey(a) && (nodes.containsKey(b))) {
		if(weight < 0){
	         throw new IllegalArgumentException();
	     }
	     for(Edge<T> edge : nodes.get(a)){
	         if (edge.getDestination().equals(b)){
	             throw new IllegalStateException();
	         }
	     }
	     
	     Set<Edge<T>> edgeA = nodes.get(a);
	     Set<Edge<T>> edgeB = nodes.get(b);

	     edgeA.add(new Edge<T>(b, name, weight));
	     edgeB.add(new Edge<T>(a, name, weight));
	     } else throw new NoSuchElementException();
		
	}


	@Override
	public void setConnectionWeight(T node1, T node2, int weight) {
		if(nodes.containsKey(node1) && (nodes.containsKey(node2))) {
			if(weight<0) {
				throw new IllegalArgumentException();
			}
			Edge<T> edge = getEdgeBetween(node1, node2);
			Edge<T> edge2 = getEdgeBetween(node2, node1);
			edge.setWeight(weight);
			edge2.setWeight(weight);
		} else throw new NoSuchElementException();
		
	}

	@Override
	public Set<T> getNodes() {
		return nodes.keySet();
		
	}

	@Override
	public Collection<Edge<T>> getEdgesFrom(T node) {
		Collection <Edge<T>> edgeFrom = new ArrayList<>();
		if(nodes.containsKey(node)) {
			for(Edge<T> edge : nodes.get(node)) {
				edgeFrom.add(edge);
			}
			return edgeFrom;
		} else throw new NoSuchElementException();
		
		
	}

	@Override
	public Edge<T> getEdgeBetween(T node1, T node2) {
		if(nodes.containsKey(node1) && (nodes.containsKey(node2))) {
			for (Edge<T> edge : nodes.get(node1)) {
				if (edge.getDestination().equals(node2)) {
				return edge;
				}
			
			} return null;
				
		} else throw new NoSuchElementException();
		
	}

	
	
	@Override
	public void disconnect(T a, T b) {
		if(getEdgeBetween(a,b)!=null) {
			
			Edge <T> edge = getEdgeBetween(a, b);
			Edge <T> edge2 = getEdgeBetween(b, a);
			
			Set<Edge<T>> edgeA = nodes.get(a);
		    Set<Edge<T>> edgeB = nodes.get(b);
		     
		    edgeA.remove(edge);
		    edgeB.remove(edge2);
		    
		} else throw new IllegalStateException();
		
	}

	@Override
	public void remove(T node) {
		
		if(nodes.containsKey(node)){
			for(Edge<T> edge : nodes.get(node)) {
				for(Edge <T> edgeRemoved : nodes.get(edge.getDestination())) {
					if(edgeRemoved.getDestination().equals(node)) {
						nodes.get(edge.getDestination()).remove(edgeRemoved);
						break;
					}
				}
			}nodes.remove(node);
		} else throw new NoSuchElementException();
		
	}

	@Override
	public boolean pathExists(T from, T to) {
		if(nodes.containsKey(from) && (nodes.containsKey(to))) {
			
	        Set<T> visited = new HashSet<>();
	        depthFirstVisitAll(from, visited);
	        return visited.contains(to);
		} else return false;
	    
	}

	
	
	@Override
	public List <Edge<T>> getPath(T from, T to) {
		if(nodes.containsKey(from) && (nodes.containsKey(to))) {
			Map<T, T> connection = new HashMap<>();
	        depthFirstConnection(from, null, connection);
			     
	        if (!connection.containsKey(to)) {
	            return null;
	        }
	        return gatherPath(from, to, connection);    
		     
		} else throw new NoSuchElementException();
		
	}
	
	private void depthFirstVisitAll(T current, Set<T> visited) {
        visited.add(current);
        for (Edge<T> edge : nodes.get(current)) {
            if (!visited.contains(edge.getDestination())) {
                depthFirstVisitAll(edge.getDestination(), visited);
            }
        }
    }
	
	private List<Edge<T>> gatherPath(T from, T to, Map<T, T> connection) {
        LinkedList<Edge<T>> path = new LinkedList<>();
        T current = to;
        while (!current.equals(from)) {
            T next = connection.get(current);
            Edge<T> edge = getEdgeBetween(next, current);
            path.addFirst(edge);
            current = next;
        }
        return Collections.unmodifiableList(path);
    }
	
	private void depthFirstConnection(T to, T from, Map<T, T> connection) {
        connection.put(to, from);
        for (Edge<T> edge : nodes.get(to)) {
            if (!connection.containsKey(edge.getDestination())) {
                depthFirstConnection(edge.getDestination(), to, connection);
            }
        }

    }
	
	@Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (T node : nodes.keySet()) {
            sb.append(node).append(": ").append(nodes.get(node)).append("\n");
        }
        return sb.toString();
    }
	
	
	
	 
	
}
