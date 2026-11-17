package pantheon.behaviours;

import pantheon.gods.Deity;
import pantheon.sim.Simulation;

public class Grow extends Behaviour {

	public Grow(Deity owner, Simulation context) {
		super(owner, context);
	}

	@Override
	public boolean act() {
		int random = context.getRandom(1, 100);
		int moreRandom = context.getRandom(1, 100);

		if(random < 20) {
			owner.setDvr(owner.getDvR() + 1);
			if(moreRandom < 20) {
				say("I have become more powerful... and I want to test my strength!");
				owner.addBehaviour(new FindRival(owner, context));
			}
			else if(moreRandom > 19 && moreRandom < 81) {
				say("I have become more powerful... and that is good");
			}
			else {
				say("I have become more powerful... and I shall use my new strength!");
				owner.addBehaviour(new FindMotivation(owner, context));
			}
			return true;
		}
		else if(random > 19 && random < 81) {
			say("I need to grow more powerful!");
			return false;
		}
		else {
			say("I can't grow more powerful...");
			return true;
		}
	}
}
