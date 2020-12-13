package behaviours;

import gods.deity;
import main.runtime;

public class findMotivation extends behaviour{

	
	public findMotivation(deity deity) {
		owner = deity;
	}

	private deity owner;

	@Override
	public boolean act(runtime runtime) {
		if(owner.getBehaviour().size() <2) {
			System.out.println("\t\tI want something to do!");
			int rnd = main.runtime.getRandom(1,5);
			switch(rnd) {
			case 1: owner.addBehaviour(new findMate(owner)); System.out.println("\t\tI want to find a mate"); break;
			case 2: owner.addBehaviour(new createConcept(owner)); System.out.println("\t\tI want to create a concept"); break;
			default: System.out.println(" \t\tI want... nothing for now"); break;
			}
		}
		return false;
	}
}
