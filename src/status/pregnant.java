package status;

import behaviours.behaviour;
import gods.deity;
import main.runtime;

public class pregnant extends status  {

	private deity owner;
	private runtime context;
	private deity partner;	
	int timeLeft;
	
	public pregnant(deity mother, deity father, runtime context) {
		owner = mother;
		this.partner = father;
		timeLeft = context.getRandom(1, 10);
		this.context = context;
	}

	@Override
	public boolean act() {
		if(timeLeft <= 0) {
			System.out.println("\t\tI have given birth!");
			context.createChild(owner, partner);
			return true;
		}
		else {
			System.out.println("\t\tI am still pregnant! I am due in " + (timeLeft-1) + "!");
			timeLeft--;
			return false;
		}
	}
}
