package cosmos;

import java.util.LinkedList;

import main.runtime;

public class cosmos extends place{

	private runtime context;
	
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
}
