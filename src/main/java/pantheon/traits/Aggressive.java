package pantheon.traits;

import pantheon.behaviours.FindRival;
import pantheon.gods.Deity;
import pantheon.sim.Simulation;

/* Quick to anger: more aggressive, less creative, and keeps looking for rivals. */
public class Aggressive extends Trait {

	public Aggressive(Deity owner, Simulation context) {
		super(owner, context);
		owner.changeAggression(10);
		owner.changeCreativity(-5);
	}

	@Override
	protected void urge() {
		if(owner.getBehaviour().size() < BUSY) {
			owner.addBehaviour(new FindRival(owner, context));
		}
	}

	@Override
	public void removeTrait() {
		owner.changeAggression(-10);
		owner.changeCreativity(5);
	}

	@Override
	public String getName() {
		return "aggressive";
	}
}
