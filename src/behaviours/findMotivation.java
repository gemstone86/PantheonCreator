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
		if(owner.getBehaviour().size() <2 | context.getRandom(1, 100) < 50) {
			System.out.println("\t\tI want something to do!");
			int rnd = context.getRandom(1,5);
			switch(rnd) {
			case 1: owner.addBehaviour(new findMate(owner, context)); System.out.println("\t\tI want to find a mate"); break;
			case 2: owner.addBehaviour(new createConcept(owner, context)); System.out.println("\t\tI want to create a concept"); break;
			case 3: owner.addBehaviour(new createPlane(owner, context)); System.out.println("\t\tI want to create a plane!");
			default: System.out.println(" \t\tI want... nothing for now"); break;
			}
		}
		return false;
	}
}
