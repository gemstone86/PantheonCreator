package behaviours;

import java.util.LinkedList;

import gods.deity;
import main.runtime;

public class findMate extends behaviour {

	private deity owner;
	private runtime context;
		
	public findMate(deity deity, runtime context) {
		owner = deity;
		this.context = context;
	}

	@Override
	public boolean act() {
		System.out.println("\t\t"+owner.getName() + " tries to find a mate");

		if(!owner.checkIfMateFound()) {
//			System.out.println("\t\tNo Mate already found");
			LinkedList<deity> listOfPotentialMates = new LinkedList<deity>();
			for(deity i:context.getListOfDeities()) {
				//build list of potential mates in this generation

				if( i.checkCompatibility(owner.getGender() )){
					listOfPotentialMates.add(i);
				}
			}
			if(listOfPotentialMates.size() > 0) {
				int rndIndex = context.getRandom(0,listOfPotentialMates.size());

				//seduce potential mate
				if(listOfPotentialMates.get(rndIndex).seduce(owner)) {
					listOfPotentialMates.get(rndIndex).setMateFound(true, owner);
					owner.setMateFound(true, listOfPotentialMates.get(rndIndex));

					
					owner.addBehaviour(new pregnant(owner, owner.getMate(), context)); 
					System.out.println("\t\tI am pregnant!");
					return true;
				}

				else {
					//System.out.println("\t\t\tCouldn't find any mates");
				}
			}
		}
		else {
//			System.out.println("\t\t\tI have been seduced already");
		}
		return false;
	}
}
