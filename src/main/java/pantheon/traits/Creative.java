package pantheon.traits;

import pantheon.behaviours.CreateConcept;
import pantheon.gods.Deity;
import pantheon.sim.Simulation;

/* Loves to make things, and keeps trying to create new concepts. */
public class Creative extends Trait {

	public Creative(Deity owner, Simulation context) {
		super(owner, context);
		owner.changeCreativity(10);
	}

	@Override
	protected void urge() {
		if(owner.getBehaviour().size() < BUSY) {
			owner.addBehaviour(new CreateConcept(owner, context));
		}
	}

	@Override
	public void removeTrait() {
		owner.changeCreativity(-10);
	}

	@Override
	public String getName() {
		return "creative";
	}
}
