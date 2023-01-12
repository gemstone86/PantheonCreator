package traits;

import behaviours.behaviour;
import behaviours.findRival;
import behaviours.interact;
import gods.deity;
import main.runtime;

public class pacifist extends trait{

	private runtime context;
	private deity owner;
	
	public pacifist(deity owner, runtime context) {
		this.context = context;
		this.owner = owner;
		owner.setAggression(-10);
	}

	@Override
	public boolean removeTrait() {
		// TODO Auto-generated method stub
		owner.setAggression(10);
		return true;
	}

	@Override
	public void motivate() {
		// TODO Auto-generated method stub
		if(owner.getBehaviour().size() < 4) {
			owner.addBehaviour(new interact(owner, context));
		}
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "pacifist";
	}
	
}
