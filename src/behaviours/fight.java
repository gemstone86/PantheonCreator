package behaviours;

import java.util.LinkedList;

import gods.deity;
import gods.relation;
import main.runtime;

public class fight extends behaviour {
	private deity owner;
	private runtime context;
	
	private deity target;
	private boolean lookingForTrouble = false;

	public fight(deity deity, runtime context, deity target) {
		owner = deity;
		this.context = context;
		this.target = target;
	}

	@Override
	public boolean act() {
		int random = context.getRandom(0, owner.getDvR()+target.getDvR());
		if(random < owner.getDvR()) {
			System.out.println("\t\tI won the fight!");
			owner.changeHealth(-context.getRandom(0, target.getDvR()/2));
			owner.setAttacker(target);
			
			target.changeHealth(-context.getRandom(1, owner.getDvR()));
			target.setAttacker(owner);
			return false;
		}
		else {
			System.out.println("\t\tI... lost the fight!");
			target.changeHealth(-context.getRandom(0, owner.getDvR()/2));
			target.setAttacker(owner);
			
			owner.changeHealth(-context.getRandom(1, target.getDvR()));
			owner.setAttacker(target);
			return true;
		}
	}
}