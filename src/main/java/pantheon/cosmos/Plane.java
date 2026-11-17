package pantheon.cosmos;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import pantheon.gods.Deity;
import pantheon.sim.Simulation;
import pantheon.sim.Snapshot;

public class Plane extends Place{

	int build = 0;
	
	boolean done = false;
	LinkedList<Deity> builders = new LinkedList<Deity>();
	Simulation context;
	LinkedList<String> concepts = new LinkedList<String>();
	
	public Plane(Simulation context) {
		this.context = context;
		// TODO Auto-generated constructor stub
	}
	
	public boolean construct(int points) {
		if(done) {
			return true;
		}
		build += points;
		if(build > 99) {
			done = true;
			return true;
		}
		return false;
	}
	public void startConstruction(Deity builder){
		if(!builders.contains(builder)) {
			builders.add(builder);
		}
	}
	
	public boolean isDone() {
		return done;
	}

	public int getProgress() {
		return Math.min(build, 100);
	}

	public Snapshot.PlaneView toView() {
		List<String> builderIds = new ArrayList<String>();
		for(Deity builder:builders) {
			builderIds.add(builder.getId());
		}
		return new Snapshot.PlaneView(getName(), getCreator() == null ? null : getCreator().getId(),
				getProgress(), done, List.copyOf(concepts), builderIds);
	}
	
	public String toString() {
		String line = "";
		if(concepts.size() < 1) {
			return this.getName();
		}
		else {
			for(int i = 0; i<concepts.size();i++) {
				if(i==0) {
					line +=concepts.get(i);
				}
				else{
					line += ", "+concepts.get(i);
				}
			}
		}
		return this.getName() + " (" + line +")";
	}

	public boolean addConcept(String randString) {
		// TODO Auto-generated method stub
		
		if(concepts.contains(randString)) {
			return false;
		}
		concepts.add(randString);
		
		return true;
		
	}

}
