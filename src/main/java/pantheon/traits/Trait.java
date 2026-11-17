package pantheon.traits;

import pantheon.behaviours.*;
import pantheon.gods.*;
import pantheon.sim.Simulation;

public class Trait {

	private Deity owner;
//	private Simulation context;
	
	public boolean act() {
		return true;
	}
	
//	public Simulation getContext() {
//		return context;
//	}
	
//	public void setContext(Simulation context) {
//		this.context = context;
//	}
//	
	public Deity getOwner() {
		return owner;
	}
	
	public Behaviour motivate() {
		return null;
	}
}
