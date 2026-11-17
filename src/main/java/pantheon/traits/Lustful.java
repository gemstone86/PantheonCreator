package pantheon.traits;

import pantheon.behaviours.FindMate;
import pantheon.gods.Deity;
import pantheon.sim.Simulation;

/* Driven by desire, and keeps looking for a mate. */
public class Lustful extends Trait {

	public Lustful(Deity owner, Simulation context) {
		super(owner, context);
		owner.changeLust(10);
	}

	@Override
	protected void urge() {
		if(!owner.checkIfMateFound() && owner.getBehaviour().size() < 3) {
			owner.addBehaviour(new FindMate(owner, context));
		}
	}

	@Override
	public void removeTrait() {
		owner.changeLust(-10);
	}

	@Override
	public String getName() {
		return "lustful";
	}
}
