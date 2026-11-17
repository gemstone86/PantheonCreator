package gods;

import main.runtime;

public class relation {

	private deity target;
	private int relation;
	private runtime context;
	
	public relation(deity target, runtime context) {
		this.target = target;
		this.context = context;
		relation = context.getRandom(-5,5);
	}
	public relation(deity target, int startingRelation, runtime context) {
		this.target = target;
		this.context = context;
		relation = startingRelation;
	}
	public deity getTarget() {
		return target;
	}
	public int getRelation() {
		return relation;
	}
	
	public void modifyRelation(int relation) {
		this.relation += relation;
	}
	public deity getDeity() {
		// TODO Auto-generated method stub
		return target;
	}
	
}
