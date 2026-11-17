package pantheon.gods;

import pantheon.sim.Simulation;

public class Relation {

	private Deity target;
	private int relation;
	private Simulation context;
	
	public Relation(Deity target, Simulation context) {
		this.target = target;
		this.context = context;
		relation = context.getRandom(-5,5);
	}
	public Relation(Deity target, int startingRelation, Simulation context) {
		this.target = target;
		this.context = context;
		relation = startingRelation;
	}
	public Deity getTarget() {
		return target;
	}
	public int getRelation() {
		return relation;
	}
	
	public void modifyRelation(int relation) {
		this.relation += relation;
	}
	public Deity getDeity() {
		// TODO Auto-generated method stub
		return target;
	}
	
}
