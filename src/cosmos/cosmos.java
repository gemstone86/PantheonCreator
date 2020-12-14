package cosmos;

import java.util.LinkedList;

import main.runtime;

public class cosmos extends place{

	private runtime context;
	
	private LinkedList<plane> underConstruction = new LinkedList<plane>();
	
	public cosmos(runtime context) {
		super(context);
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
	
	public plane startBuildingPlane() {
		plane newPlane = new plane(context);
		newPlane.setName(context.getRandomPlaneName());
		createdPlanes.add(newPlane);
		
		return newPlane;
	}
	
	public LinkedList<plane> getPlanesUnderConstruction(){
		return underConstruction;
	}
	
	public int getConstructionSize() {
		return underConstruction.size();
	}
}
