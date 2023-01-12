package behaviours;

import java.util.LinkedList;

import gods.deity;
import main.runtime;
import status.pregnant;

public class findMate extends behaviour {

	private deity owner;
	private runtime context;
	private int attempts = 0;
	
	public findMate(deity deity, runtime context) {
		owner = deity;
		this.context = context;
	}

	@Override
	public boolean act() {
		if(owner.getLust()>10) {
			
		
		System.out.println("\t\tI'm trying to find a mate");

		if(!owner.checkIfMateFound()) {
			LinkedList<deity> listOfPotentialMates = new LinkedList<deity>();
			for(deity i:context.getListOfDeities()) {
				//build list of potential mates in this generation

				if( i.checkCompatibility(owner.getGender()) && !owner.equals(i)){
					listOfPotentialMates.add(i);
				}
			}
			if(!listOfPotentialMates.contains(owner) && owner.random(1, 20) < 3 || context.getListOfDeities().size() == 1) {
				listOfPotentialMates.add(owner);
			}
			if(listOfPotentialMates.size() > 0) {
				int rndIndex = context.getRandom(0,listOfPotentialMates.size());

				//seduce potential mate
				if(listOfPotentialMates.get(rndIndex).seduce(owner)) {
					listOfPotentialMates.get(rndIndex).setMateFound(true, owner);
					owner.setMateFound(true, listOfPotentialMates.get(rndIndex));

					int check = context.getRandom(1, 100);
					if(check < 25-context.getListOfDeities().size()) {
						owner.addStatus(new pregnant(owner, owner.getMate(), context)); 
						System.out.println("\t\tI am pregnant!");
						owner.setLust(-10);
						return true;
					}
					System.out.println("\t\tI am not pregnant yet");
					return false;
					
				}

				else {
					attempts++;
					if(attempts++ > 3) {
						System.out.println("\t\t\tThere are no potential mates, so I give up");
						return true;
					}
					System.out.println("\t\t\tThere has to be someone out there for me");
				}
			}
		}
		}
		else {
			owner.setLust(context.getRandom(1, 3));
		}
		return false;
	}
}
