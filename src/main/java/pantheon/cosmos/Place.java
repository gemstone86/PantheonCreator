package cosmos;

import java.util.LinkedList;

import gods.*;
import main.runtime;

public class place {

	private place parentPlane;
	private deity creator;
	
	LinkedList<place> connections = new LinkedList<place>();
	
	private String name;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void AddConnection(place newConnection) {
		if(connections.contains(newConnection)) {
			System.out.println("\t\t\tThis plane already contains this connection!");
	
		}
		else {
			System.out.println("\t\t\tI have added a new connection!");
			connections.add(newConnection);
	
		}
	}
	
	public void removeConnection(int index) {
		connections.remove(index);
	}
	
	public LinkedList<place> getConnections() {
		return connections;
	}
	
	public void setOwner(deity deity) {
		this.creator = deity;
	}
	
	public void removeOwner() {
		this.creator = null;
	}
	
	public place getParentPlane() {
		return parentPlane;
	}
	
	public deity getCreator() {
		return creator;
	}

	public int getNumberOfConnections() {
		// TODO Auto-generated method stub
		return connections.size();
	}
	
	
}
