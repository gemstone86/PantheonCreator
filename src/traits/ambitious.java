package traits;

import behaviours.*;
import gods.deity;
import main.runtime;

public class ambitious extends trait{

	private runtime context;
	private deity owner;
	
	public ambitious(deity owner, runtime context) {
		this.context = context;
		this.owner = owner;
		owner.setAggression(5);
		owner.setCreativity(5);
		owner.setLust(5);
	}
	
	@Override
	public boolean removeTrait() {
		// TODO Auto-generated method stub
		owner.setAggression(-5);
		owner.setCreativity(-5);
		owner.setLust(-5);
		return true;
	}

	@Override
	public void motivate() {
		if(owner.getBehaviour().size() < 4) {
			owner.addBehaviour(new grow(owner, context));
		}
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "aggressive";
	}
}
