package cosmos;

import java.util.LinkedList;

import gods.deity;
import main.runtime;

public class cosmos extends place{

	private runtime context;
	
	private LinkedList<plane> underConstruction = new LinkedList<plane>();
	
	public cosmos(runtime context) {
		this.context = context;
	}

	LinkedList<String> createdConcepts = new LinkedList<String>();
	LinkedList<place> createdPlanes = new LinkedList<place>();
	
	public void createConcept(String concept) {
		createdConcepts.add(concept);
	}
	
	public LinkedList<String> getAllConcepts(){
		return createdConcepts;
	}
	
	public String getRandomConcept() {
		return createdConcepts.get(context.getRandom(0,createdConcepts.size()));
	}
	
	public plane startBuildingPlane(deity creator) {
		plane newPlane = new plane(context);
		newPlane.setName(getUniquePlaneName());
		newPlane.setOwner(creator);
		createdPlanes.add(newPlane);
		underConstruction.add(newPlane);
		
		return newPlane;
	}
	
	public void finishPlane(plane finished) {
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
		for(place existing:createdPlanes) {
			if(name.equals(existing.getName())) {
				return true;
			}
		}
		return false;
	}
	
	public LinkedList<plane> getPlanesUnderConstruction(){
		return underConstruction;
	}
	
	public int getConstructionSize() {
		return underConstruction.size();
	}
	
	public String toString() {
		String returnMessage = "";
		for(place plane:createdPlanes) {
			returnMessage+=plane.toString()+", ";
		}
		
		return returnMessage;
	}
}
