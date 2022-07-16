package behaviours;

import java.util.LinkedList;

import gods.deity;
import gods.relation;
import main.runtime;

public class findRival extends behaviour{

	private deity owner;
	private runtime context;
	LinkedList<deity> possibleRivals = new LinkedList<deity>();
	private deity target;
	private boolean lookingForTrouble = false;

	public findRival(deity deity, runtime context) {
		owner = deity;
		this.context = context;
	}

	@Override
	public boolean act() {
		if(target == null) {
			for(relation contact:owner.getRelations()) {
				if( contact.getRelation() < -20) {
					possibleRivals.add(contact.getDeity());
				}
			}
			if(possibleRivals.size()>0) {
				target = possibleRivals.get(context.getRandom(0, possibleRivals.size()));

				if(!target.doYouKnowMe(owner)) {
					target.addRelation(new relation(owner,-20, context));
				}
				System.out.println("\t\t\tI hate you " + target.getName());
				
				owner.addBehaviour(new fight(owner, context, target));
				
				return true;
			}
			else{
				if(!lookingForTrouble) {
					System.out.println("\t\t\tI'm looking for trouble!");
					owner.addBehaviour(new interact(owner, context));
					lookingForTrouble = true;
				}
			}
		}
		return false;

	}
}