package pantheon.behaviours;

import pantheon.gods.Deity;
import pantheon.sim.SimEvent;
import pantheon.sim.Simulation;

/*
 * A god who has turned to crime. Each generation there is a RISE_CHANCE percent chance that their
 * crime score (0 to 100) rises by RISE. Once it is above FLEE_ABOVE, their crimes catch up with them:
 * they flee to the Void, their crime score starts over, and this behaviour ends.
 */
public class Crime extends Behaviour {

	public static final int RISE_CHANCE = 25;
	public static final int RISE = 10;
	public static final int FLEE_ABOVE = 75;

	public Crime(Deity owner, Simulation context) {
		super(owner, context);
	}

	@Override
	public boolean act() {
		if(context.getRandom(0, 100) < RISE_CHANCE) {
			owner.addCrime(RISE);
			say("I have committed a crime... (crime " + owner.getCrime() + ")");
			context.emit(new SimEvent.CrimeCommitted(context.getGeneration(), owner.getId(), owner.getCrime()));
		}
		if(owner.getCrime() > FLEE_ABOVE) {
			say("My crimes have caught up with me!");
			owner.fleeToVoid(SimEvent.DeityMoved.FLED_CRIMES);
			owner.resetCrime();
			return true;
		}
		return false;
	}
}
