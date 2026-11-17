package pantheon.behaviours;

import java.util.LinkedList;

import pantheon.gods.Deity;
import pantheon.sim.SimEvent;
import pantheon.sim.Simulation;
import pantheon.status.Pregnant;

/*
 * Looks for a mate and tries to conceive a child.
 * A god only courts once their lust is above LUST_NEEDED; until then their desire slowly builds.
 * Gods usually court someone on their own plane. Now and then a god has a child alone,
 * and always does if they are the only god in the world.
 * The fuller the pantheon, the less likely a pregnancy (none at all from 25 living gods).
 */
public class FindMate extends Behaviour {

	/* Gods court once their lust is above this (dev used 10, which left pantheons dying out). */
	public static final int LUST_NEEDED = -10;
	/* Chance in percent of a pregnancy after a successful courtship, in an otherwise empty world... */
	public static final int PREGNANCY_CHANCE = 100;
	/* ...minus this much for every living god, so there are no pregnancies at all from 25 living gods. */
	public static final int PREGNANCY_DROP_PER_GOD = 4;
	private static final int MAX_FAILED_ATTEMPTS = 3;
	private int failedAttempts = 0;

	public FindMate(Deity owner, Simulation context) {
		super(owner, context);
	}

	@Override
	public boolean act() {
		if(owner.getLust() <= LUST_NEEDED) {
			//desire builds up while waiting
			owner.changeLust(context.getRandom(1, 3));
			return false;
		}
		if(owner.checkIfMateFound()) {
			return false;
		}
		say("I'm trying to find a mate");

		LinkedList<Deity> listOfPotentialMates = new LinkedList<Deity>();
		for(Deity candidate:context.getListOfDeities()) {
			if(candidate != owner && candidate.isAlive() && !candidate.checkIfMateFound()
					&& candidate.checkCompatibility(owner.getGender())) {
				listOfPotentialMates.add(candidate);
			}
		}
		//gods usually court someone on their own plane, if there is anyone suitable there
		LinkedList<Deity> nearbyMates = new LinkedList<Deity>();
		for(Deity candidate:listOfPotentialMates) {
			if(candidate.isNear(owner)) {
				nearbyMates.add(candidate);
			}
		}
		if(!nearbyMates.isEmpty() && context.getRandom(0, 100) < Simulation.SAME_PLACE_PREFERENCE) {
			listOfPotentialMates = nearbyMates;
		}
		//sometimes a god has a child alone, and always if they are the only god in the world
		if(context.getRandom(1, 20) < 3 || context.getListOfDeities().size() == 1) {
			listOfPotentialMates.add(owner);
		}

		if(listOfPotentialMates.isEmpty()) {
			return failedAttempt("There is nobody here for me");
		}

		Deity mate = listOfPotentialMates.get(context.getRandom(0, listOfPotentialMates.size()));
		if(!mate.seduce(owner)) {
			return failedAttempt("There has to be someone out there for me");
		}

		if(mate == owner) {
			owner.setMateFound(true, null);
			say("I will have a child on my own");
		}
		else {
			mate.setMateFound(true, owner);
			owner.setMateFound(true, mate);
			say(mate.getName() + " has agreed to be my mate!");
			context.emit(new SimEvent.MateFound(context.getGeneration(), owner.getId(), mate.getId()));
		}

		int check = context.getRandom(1, 100);
		if(check < PREGNANCY_CHANCE - PREGNANCY_DROP_PER_GOD * context.getListOfDeities().size()) {
			owner.addStatus(new Pregnant(owner, mate, context));
			owner.changeLust(-10);
			say("I am pregnant!");
			context.emit(new SimEvent.Conceived(context.getGeneration(), owner.getId(), mate.getId()));
			return true;
		}
		say("I am not pregnant yet");
		return false;
	}

	private boolean failedAttempt(String complaint) {
		failedAttempts++;
		if(failedAttempts >= MAX_FAILED_ATTEMPTS) {
			say("There are no potential mates, so I give up");
			return true;
		}
		say(complaint);
		return false;
	}
}
