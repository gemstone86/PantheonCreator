package cosmos;

import java.util.LinkedList;

import gods.*;
import main.runtime;

public class place {

	private place parentPlane;
	private deity creator;
	private runtime context;
	
	LinkedList<place> connections = new LinkedList<place>();
	
	private String name;
	
	public place(runtime context) {
		this.context=context;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void addConnection(place newConnection) {
		connections.add(newConnection);
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
	
	
}
