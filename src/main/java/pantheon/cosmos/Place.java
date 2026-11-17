package pantheon.cosmos;

import java.util.LinkedList;

import pantheon.gods.*;
import pantheon.sim.Simulation;

public class Place {

	private Place parentPlane;
	private Deity creator;
	
	LinkedList<Place> connections = new LinkedList<Place>();
	
	private String name;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void addConnection(Place newConnection) {
		connections.add(newConnection);
	}
	
	public void removeConnection(int index) {
		connections.remove(index);
	}
	
	public LinkedList<Place> getConnections() {
		return connections;
	}
	
	public void setOwner(Deity deity) {
		this.creator = deity;
	}
	
	public void removeOwner() {
		this.creator = null;
	}
	
	public Place getParentPlane() {
		return parentPlane;
	}
	
	public Deity getCreator() {
		return creator;
	}
	
	
}
