package pantheon.behaviours;

import pantheon.gods.Deity;
import pantheon.sim.Simulation;

public class FindMotivation extends Behaviour {

	/* Gods more aggressive than this go looking for rivals whenever they aren't busy. */
	public static final int AGGRESSION_FOR_TROUBLE = 10;

	public FindMotivation(Deity owner, Simulation context) {
		super(owner, context);
	}

	@Override
	public boolean act() {
		//give me more to do if I only want something to do or if I roll less than 30.
		if(owner.getBehaviour().size() < 3 | context.getRandom(1, 100) < 30) {
			int rnd = context.getRandom(1, 100);

			if(rnd < 10) {
				owner.addBehaviour(new FindMate(owner, context));
				say("I want to find a mate");
			}
			if(rnd > 9 && rnd < 35) {
				owner.addBehaviour(new CreateConcept(owner, context));
				say("I want to create a concept");
			}
			if(rnd >= 35 && rnd < 40) {
				if(!owner.hasBehaviour(CraftArtifact.class)) {
					owner.addBehaviour(new CraftArtifact(owner, context));
					say("I want to craft an artifact");
				}
			}
			if(rnd >= 40 && rnd < 60) {
				owner.addBehaviour(new CreatePlane(owner, context));
				say("I want to create a plane!");
			}
			if(rnd >= 60 && rnd < 70) {
				owner.addBehaviour(new FindRival(owner, context));
				say("I need to find a rival!");
			}
			if(rnd >= 70 && rnd < 80) {
				owner.addBehaviour(new Interact(owner, context));
				say("I need to socialize!");
			}
			if(rnd >= 80 && rnd < 90) {
				owner.addBehaviour(new Grow(owner, context));
				say("I need more power!");
			}
			if(rnd >= 90 && rnd < 96) {
				owner.addBehaviour(new Move(owner, context));
				say("I should move!");
			}
			if(rnd >= 96) {
				if(!owner.hasBehaviour(Crime.class)) {
					owner.addBehaviour(new Crime(owner, context));
					say("I am tempted by crime...");
				}
			}
		}
		//aggressive gods go looking for trouble when they aren't busy
		if(owner.getAggression() > AGGRESSION_FOR_TROUBLE && owner.getBehaviour().size() < 4) {
			owner.addBehaviour(new FindRival(owner, context));
		}
		return false;
	}
}
