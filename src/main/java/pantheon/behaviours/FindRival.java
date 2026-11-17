package pantheon.behaviours;

import java.util.LinkedList;

import pantheon.gods.Deity;
import pantheon.gods.Relation;
import pantheon.sim.SimEvent;
import pantheon.sim.Simulation;

public class FindRival extends Behaviour {

	private final LinkedList<Deity> possibleRivals = new LinkedList<Deity>();
	private Deity target;
	private boolean lookingForTrouble = false;

	public FindRival(Deity owner, Simulation context) {
		super(owner, context);
	}

	@Override
	public boolean act() {
		if(target == null) {
			for(Relation contact:owner.getRelations()) {
				if(contact.getRelation() < -20 && contact.getDeity() != null && contact.getDeity().isAlive()
						&& !possibleRivals.contains(contact.getDeity())) {
					possibleRivals.add(contact.getDeity());
				}
			}
			if(possibleRivals.size() > 0) {
				target = possibleRivals.get(context.getRandom(0, possibleRivals.size()));

				if(!target.doYouKnowMe(owner)) {
					target.addRelation(new Relation(owner, -20, context));
				}
				say("I hate you " + target.getName() + "!");
				context.emit(new SimEvent.RivalryDeclared(context.getGeneration(), owner.getId(), target.getId()));

				owner.addBehaviour(new Fight(owner, context, target));
				return true;
			}
			else if(!lookingForTrouble) {
				say("I'm looking for trouble!");
				owner.addBehaviour(new Interact(owner, context));
				lookingForTrouble = true;
			}
		}
		return false;
	}
}
