package traits;

import behaviours.*;
import gods.deity;
import main.runtime;

public class creative extends trait{

	private runtime context;
	private deity owner;
	
	public creative(deity owner, runtime context) {
		this.context = context;
		this.owner = owner;
		owner.setCreativity(10);
	}

	@Override
	public boolean removeTrait() {
		// TODO Auto-generated method stub
		owner.setCreativity(-10);
		return true;
	}

	@Override
	public void motivate() {
		// TODO Auto-generated method stub
		if(owner.getBehaviour().size() < 4) {	
			owner.addBehaviour(new createConcept(owner, context));
		}
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "creative";
	}
	
	
}
