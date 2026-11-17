package pantheon.behaviours;

import pantheon.gods.Deity;
import pantheon.gods.Relation;
import pantheon.sim.Simulation;

public class Interact extends Behaviour {

	private Relation target;
	private boolean end = false;

	public Interact(Deity owner, Simulation context) {
		super(owner, context);
		Deity other = context.getRandomDeityPreferringNear(owner, Simulation.SAME_PLACE_PREFERENCE);

		if(other.equals(owner) || !other.isAlive()) {
			end = true;
		}
		else {
			//keep building on an existing relationship instead of starting a new one each time
			target = owner.getRelationTo(other);
			if(target == null) {
				target = new Relation(other, context);
				owner.addRelation(target);
			}
		}
	}

	@Override
	public boolean act() {
		if(end) {
			say("There's no one to interact with!");
			return true;
		}
		String name = target.getTarget().getName();
		int before = target.getRelation();
		int modifyRelation;
		String opinion;

		if(before < -10) {
			modifyRelation = context.getRandom(-10, 5);
			opinion = " is such a jerk!";
		}
		else if(before > 10) {
			modifyRelation = context.getRandom(-5, 10);
			opinion = " is nice!";
		}
		else {
			modifyRelation = context.getRandom(-20, 20);
			opinion = " is interesting!";
		}
		target.modifyRelation(modifyRelation);
		say(name + opinion + " (From " + before + " to " + target.getRelation() + ")");

		if(context.getRandom(1, 100) > 70) {
			say("That's enough interaction with " + name);
			return true;
		}
		say("I want to interact more with " + name);
		return false;
	}
}
