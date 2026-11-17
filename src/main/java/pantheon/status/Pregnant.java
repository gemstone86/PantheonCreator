package pantheon.status;

import pantheon.gods.Deity;
import pantheon.sim.Simulation;

/*
 * Carrying a child. The child is born after 1 to 9 generations, if the parent is still alive.
 */
public class Pregnant extends Status {

	private final Deity partner;
	private int timeLeft;

	/* partner is the other parent; it may be the owner itself for a god who had a child alone. */
	public Pregnant(Deity owner, Deity partner, Simulation context) {
		super(owner, context);
		this.partner = partner;
		this.timeLeft = context.getRandom(1, 10);
	}

	@Override
	public boolean act() {
		if(timeLeft <= 0) {
			say("I have given birth!");
			context.createChild(owner, partner == owner ? null : partner);
			return true;
		}
		say("I am still pregnant! I am due in " + timeLeft + "!");
		timeLeft--;
		return false;
	}

	@Override
	public String describe() {
		int due = timeLeft + 1;
		return "pregnant, due in " + due + (due == 1 ? " generation" : " generations");
	}

	public Deity getPartner() {
		return partner;
	}
}
