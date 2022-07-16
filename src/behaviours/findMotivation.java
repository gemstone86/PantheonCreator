package behaviours;

import gods.deity;
import main.runtime;

public class findMotivation extends behaviour{

	private deity owner;
	private runtime context;

	public findMotivation(deity deity, runtime context) {
		owner = deity;
		this.context = context;
	}

	@Override
	public boolean act() {
		//give me more to do if I only want something to do or if I roll less than 30.
		if(owner.getBehaviour().size() <2 | context.getRandom(1, 100) < 30) {
			System.out.println("\t\tI want something new to do!");
			int rnd = context.getRandom(1,100);

			if(rnd < 10 ) {
				owner.addBehaviour(new findMate(owner, context)); 
				System.out.println("\t\tI want to find a mate");
			}
			if(rnd >9 && rnd < 40) {	
				owner.addBehaviour(new createConcept(owner, context)); 
				System.out.println("\t\tI want to create a concept");
			}
			if(rnd >= 40 && rnd < 60) {
				owner.addBehaviour(new createPlane(owner, context)); 
				System.out.println("\t\tI want to create a plane!");
			}
			if(rnd >=60 && rnd < 70) {
				owner.addBehaviour(new findRival(owner, context)); 
				System.out.println("\t\tI need to find a rival!");
			}
			if(rnd >= 70 && rnd < 80) {
				owner.addBehaviour(new interact(owner, context));
				System.out.println("\t\tI need to socialize!");
			}
			if(rnd >= 80 && rnd < 90) {
				owner.addBehaviour(new grow(owner, context));
				System.out.println("\t\tI need more power!");
			}
			if(rnd >= 90) {
				System.out.println(" \t\tI want... nothing for now");
			}

		}
		return false;
	}
}
