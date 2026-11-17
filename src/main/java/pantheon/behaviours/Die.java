package pantheon.behaviours;

import pantheon.gods.Deity;
import pantheon.sim.Simulation;

public class Die extends Behaviour {

	public Die(Deity owner, Simulation context) {
		super(owner, context);
	}

	@Override
	public boolean act() {
		say(owner.getName() + " has died of old age");
		owner.die("old age", null);
		return true;
	}
}
