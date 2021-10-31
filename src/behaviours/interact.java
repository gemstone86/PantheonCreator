package behaviours;

import gods.deity;
import gods.relation;
import main.runtime;

public class interact extends behaviour{

	private deity owner;
	private runtime context;
	private relation target;

	public interact(deity deity, runtime context) {
		owner = deity;
		this.context = context;
		target = new relation(context.getRandomDeity(), context);
		owner.addRelation(target);
	}

	@Override
	public boolean act() {
		int modifyRelation = 0;

		if(target.getRelation()<-10) {
			modifyRelation = context.getRandom(-10, 5);
			System.out.println("\t\t\t" + target.getTarget().getName() + " is such a jerk! (" + modifyRelation +")");
		}
		else if(target.getRelation()>10) {
			modifyRelation = context.getRandom(-5, 10);
			System.out.println("\t\t\t" + target.getTarget().getName() + " is nice! (" + modifyRelation +")");
		}
		else {
			modifyRelation = context.getRandom(-20, 20);
			System.out.println("\t\t\t" + target.getTarget().getName() + " is interesting! (" + modifyRelation +")");
		}

		target.modifyRelation(modifyRelation);
		
		if(context.getRandom(1, 100) > 70) {
			System.out.println("\t\t\tThat's enough interaction with " + target.getTarget().getName());
			return true;
		}
		System.out.println("\t\t\tI want to interact more with "+ target.getTarget().getName());
		return false;

	}
}