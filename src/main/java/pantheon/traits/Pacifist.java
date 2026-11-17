package pantheon.traits;

import pantheon.behaviours.Interact;
import pantheon.gods.Deity;
import pantheon.sim.Simulation;

/* Prefers talking to fighting. */
public class Pacifist extends Trait {

	public Pacifist(Deity owner, Simulation context) {
		super(owner, context);
		owner.changeAggression(-10);
	}

	@Override
	protected void urge() {
		if(owner.getBehaviour().size() < BUSY) {
			owner.addBehaviour(new Interact(owner, context));
		}
	}

	@Override
	public void removeTrait() {
		owner.changeAggression(10);
	}

	@Override
	public String getName() {
		return "pacifist";
	}
}
