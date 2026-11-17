package pantheon.status;

import pantheon.gods.Deity;
import pantheon.sim.Simulation;

/*
 * Something that is happening to a god for a while, like a pregnancy or poison.
 * act() is called once per generation, after the god's behaviours, until it returns true.
 */
public abstract class Status {

	protected final Deity owner;
	protected final Simulation context;

	protected Status(Deity owner, Simulation context) {
		this.owner = owner;
		this.context = context;
	}

	/* Returns true when the status is over and can be removed. */
	public abstract boolean act();

	/* Short description for the UI, e.g. "pregnant, due in 3 generations". */
	public abstract String describe();

	public Deity getOwner() {
		return owner;
	}

	protected void say(String text) {
		context.narrate(owner, 1, text);
	}
}
