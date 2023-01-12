package traits;

import behaviours.findMate;
import behaviours.findRival;
import behaviours.interact;
import gods.deity;
import main.runtime;

public class lustful extends trait{

	private runtime context;
	private deity owner;
	
	public lustful(deity owner, runtime context) {
		this.context = context;
		this.owner = owner;
		owner.setLust(10);
	}

	@Override
	public boolean removeTrait() {
		// TODO Auto-generated method stub
		owner.setLust(-10);
		return true;
	}

	@Override
	public void motivate() {
		// TODO Auto-generated method stub
		if(!owner.checkIfMateFound()) {
			owner.addBehaviour(new findMate(owner, context));
		}
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "lustful";
	}
	
	
}
