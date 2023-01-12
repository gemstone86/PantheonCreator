package traits;

import behaviours.behaviour;
import behaviours.findRival;
import gods.deity;
import main.runtime;

public class aggressive extends trait{

	private runtime context;
	private deity owner;
	
	public aggressive(deity owner, runtime context) {
		this.context = context;
		this.owner = owner;
		owner.setAggression(10);
		owner.setCreativity(-5);
	}
	
	@Override
	public boolean removeTrait() {
		// TODO Auto-generated method stub
		owner.setAggression(-10);
		owner.setCreativity(5);
		return true;
	}

	@Override
	public void motivate() {
		if(owner.getBehaviour().size() < 4) {
			owner.addBehaviour(new findRival(owner, context));
		}
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "aggressive";
	}
}
