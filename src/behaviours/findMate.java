package behaviours;

import java.util.LinkedList;

import gods.deity;
import main.runtime;

public class findMate extends behaviour {

	private deity owner;

	public findMate(deity deity) {
		this.owner = deity;
	}

	@Override
	public void act(runtime runtime) {
		System.out.println("\t\t"+owner.getName() + " tries to find a mate");

		if(!owner.checkIfMateFound()) {
			System.out.println("\t\tNo Mate already found");
			LinkedList<deity> listOfPotentialMates = new LinkedList<deity>();
			for(deity i:runtime.getListOfDeities()) {
				//build list of potential mates in this generation

				if( i.checkCompatibility(owner.getGender() )){
					listOfPotentialMates.add(i);
				}
			}
			if(listOfPotentialMates.size() > 0) {
				int rndIndex = main.runtime.getRandom(0,listOfPotentialMates.size()-1);

				//seduce potential mate
				if(listOfPotentialMates.get(rndIndex).seduce(owner)) {
					listOfPotentialMates.get(rndIndex).setMateFound(true, owner);
					owner.setMateFound(true, listOfPotentialMates.get(rndIndex));

					main.runtime.createChild(owner, listOfPotentialMates.get(rndIndex));
				}

				else {
					System.out.println("\t\t\tCouldn't find any mates");
				}
			}
		}
		else {
			System.out.println("\t\t\tI have been seduced already");
		}
	}
}
