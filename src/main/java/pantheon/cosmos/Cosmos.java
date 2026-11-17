package pantheon.cosmos;

import java.util.LinkedList;

import pantheon.gods.Deity;
import pantheon.sim.Simulation;

public class Cosmos extends Place{

	private Simulation context;
	
	private LinkedList<Plane> underConstruction = new LinkedList<Plane>();
	
	public Cosmos(Simulation context) {
		this.context = context;
	}

	LinkedList<String> createdConcepts = new LinkedList<String>();
	LinkedList<Plane> createdPlanes = new LinkedList<Plane>();
	
	public void createConcept(String concept) {
		createdConcepts.add(concept);
	}
	
	public LinkedList<String> getAllConcepts(){
		return createdConcepts;
	}
	
	public String getRandomConcept() {
		return createdConcepts.get(context.getRandom(0,createdConcepts.size()));
	}
	
	public Plane startBuildingPlane(Deity creator) {
		Plane newPlane = new Plane(context);
		newPlane.setName(getUniquePlaneName());
		newPlane.setOwner(creator);
		createdPlanes.add(newPlane);
		underConstruction.add(newPlane);
		
		return newPlane;
	}
	
	public void finishPlane(Plane finished) {
		underConstruction.remove(finished);
	}
	
	private String getUniquePlaneName() {
		String name = context.getRandomPlaneName();
		for(int attempt = 0; attempt < 20 && isPlaneNameTaken(name); attempt++) {
			name = context.getRandomPlaneName();
		}
		//fall back to numbering if every attempt collided
		String base = name;
		int number = 2;
		while(isPlaneNameTaken(name)) {
			name = base + " " + number;
			number++;
		}
		return name;
	}
	
	private boolean isPlaneNameTaken(String name) {
		for(Place existing:createdPlanes) {
			if(name.equals(existing.getName())) {
				return true;
			}
		}
		return false;
	}
	
	public LinkedList<Plane> getPlanes(){
		return createdPlanes;
	}
	
	public LinkedList<Plane> getPlanesUnderConstruction(){
		return underConstruction;
	}
	
	public int getConstructionSize() {
		return underConstruction.size();
	}
	
	public String toString() {
		String returnMessage = "";
		for(Place plane:createdPlanes) {
			returnMessage+=plane.toString()+", ";
		}
		
		return returnMessage;
	}
}
