package status;

import behaviours.behaviour;
import gods.deity;
import main.runtime;

public class poisoned extends status  {

	private deity owner;
	private runtime context;
	private deity attacker;	
	int damage;
	int time;
	
	public poisoned(deity target, deity attacker, runtime context, int damage, int time) {
		this.owner = target;
		this.attacker = attacker;
		this.damage = damage;
		this.time = time;
	}

	@Override
	public boolean act() {
		if(time > 0) {
			
		
		owner.changeHealth(-damage);
		time--;
		return false;
		}
		else {
			return true;
		}

	}
}
