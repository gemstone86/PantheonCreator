package pantheon.cosmos;

import java.util.LinkedList;

import pantheon.artifacts.Artifact;
import pantheon.artifacts.ArtifactType;
import pantheon.gods.Deity;
import pantheon.sim.Simulation;

public class Cosmos extends Place{

	private Simulation context;
	
	private LinkedList<Plane> underConstruction = new LinkedList<Plane>();
	
	/* The cosmos itself is the space between the planes, where gods start out. */
	public static final String VOID = "The Void";

	public Cosmos(Simulation context) {
		this.context = context;
		setName(VOID);
	}

	/*
	 * Opens a two-way path between two places. Returns false if they were already connected.
	 */
	public boolean connect(Place a, Place b) {
		if(a == b) {
			return false;
		}
		boolean added = a.addConnection(b);
		b.addConnection(a);
		return added;
	}

	/* Planes that are finished and can be lived in. */
	public LinkedList<Plane> getFinishedPlanes() {
		LinkedList<Plane> finished = new LinkedList<Plane>();
		for(Plane plane:createdPlanes) {
			if(plane.isDone()) {
				finished.add(plane);
			}
		}
		return finished;
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
	
	LinkedList<Artifact> artifacts = new LinkedList<Artifact>();

	/*
	 * Starts a new artifact of a random type for a deity, named after one of the deity's domains.
	 */
	public Artifact startArtifact(Deity creator) {
		ArtifactType[] types = ArtifactType.values();
		ArtifactType type = types[context.getRandom(0, types.length)];
		String noun = type == ArtifactType.OTHER ? "Relic" : type.getLabel();
		String domain = creator.getDomains().isEmpty() ? "Power"
				: creator.getDomains().get(context.getRandom(0, creator.getDomains().size()));
		String name = noun + " of " + domain;
		if(isArtifactNameTaken(name)) {
			name = creator.getName() + "'s " + name;
		}
		String base = name;
		for(int number = 2; isArtifactNameTaken(name); number++) {
			name = base + " " + number;
		}
		Artifact artifact = new Artifact(name, type, creator, context.getGeneration());
		artifacts.add(artifact);
		return artifact;
	}

	private boolean isArtifactNameTaken(String name) {
		for(Artifact artifact:artifacts) {
			if(artifact.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	public LinkedList<Artifact> getArtifacts() {
		return artifacts;
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
