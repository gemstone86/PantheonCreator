package pantheon.traits;

import pantheon.behaviours.Grow;
import pantheon.gods.Deity;
import pantheon.sim.Simulation;

/* Wants more of everything, and above all more power. */
public class Ambitious extends Trait {

	public Ambitious(Deity owner, Simulation context) {
		super(owner, context);
		owner.changeAggression(5);
		owner.changeCreativity(5);
		owner.changeLust(5);
	}

	@Override
	protected void urge() {
		if(owner.getBehaviour().size() < BUSY) {
			owner.addBehaviour(new Grow(owner, context));
		}
	}

	@Override
	public void removeTrait() {
		owner.changeAggression(-5);
		owner.changeCreativity(-5);
		owner.changeLust(-5);
	}

	@Override
	public String getName() {
		return "ambitious";
	}
}
