package pantheon.status;

import pantheon.gods.Deity;
import pantheon.sim.Simulation;

/*
 * Loses some health every generation for a while. If the poison kills, the poisoner gets the blame.
 */
public class Poisoned extends Status {

	private final Deity poisoner;
	private final int damage;
	private int time;

	public Poisoned(Deity owner, Deity poisoner, Simulation context, int damage, int time) {
		super(owner, context);
		this.poisoner = poisoner;
		this.damage = damage;
		this.time = time;
	}

	@Override
	public boolean act() {
		if(time > 0) {
			owner.setAttacker(poisoner);
			owner.changeHealth(-damage);
			say("The poison burns! (" + damage + " damage)");
			time--;
			return false;
		}
		say("The poison has worn off");
		return true;
	}

	@Override
	public String describe() {
		return "poisoned";
	}

	public Deity getPoisoner() {
		return poisoner;
	}
}
