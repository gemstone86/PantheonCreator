package pantheon.traits;

import pantheon.gods.Deity;
import pantheon.sim.Simulation;

/*
 * A lasting part of a god's personality. A trait shifts the god's temperament when it is gained,
 * and now and then urges the god into the behaviour typical of the trait.
 */
public abstract class Trait {

	/* Chance in percent each generation that a trait urges its god into its typical behaviour. */
	public static final int URGE_CHANCE = 30;
	/* Traits don't add more to do once a god already has this many behaviours. */
	protected static final int BUSY = 4;

	protected final Deity owner;
	protected final Simulation context;

	protected Trait(Deity owner, Simulation context) {
		this.owner = owner;
		this.context = context;
	}

	/* Called every generation before the god acts. */
	public void motivate() {
		if(context.getRandom(0, 100) < URGE_CHANCE) {
			urge();
		}
	}

	/* Gives the god something to do that fits the trait. */
	protected abstract void urge();

	/* Undoes the trait's effect on the god's temperament. */
	public abstract void removeTrait();

	public abstract String getName();

	public Deity getOwner() {
		return owner;
	}

	@Override
	public String toString() {
		return getName();
	}
}
