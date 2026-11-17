package pantheon.behaviours;

import pantheon.gods.Deity;
import pantheon.sim.Simulation;

/*
 * Something a deity wants to do. act() is called once per generation until it returns true.
 */
public abstract class Behaviour {

	protected final Deity owner;
	protected final Simulation context;

	protected Behaviour(Deity owner, Simulation context) {
		this.owner = owner;
		this.context = context;
	}

	/*
	 * Performs one turn of this behaviour.
	 * Returns true when the behaviour is finished and can be removed.
	 */
	public abstract boolean act();

	public Deity getOwner() {
		return owner;
	}

	/* Reports a line of the owner's commentary. */
	protected void say(String text) {
		context.narrate(owner, 1, text);
	}
}
