package pantheon.behaviours;

import pantheon.gods.Deity;
import pantheon.sim.SimEvent;
import pantheon.sim.Simulation;
import pantheon.status.Poisoned;

/*
 * Fights the target once per generation. Keeps fighting after a win until the target dies; stops after a loss.
 */
public class Fight extends Behaviour {

	/* Chance in percent that the winner of a fight also poisons the loser. */
	public static final int POISON_CHANCE = 10;

	private final Deity target;

	public Fight(Deity owner, Simulation context, Deity target) {
		super(owner, context);
		this.target = target;
	}

	@Override
	public boolean act() {
		if(!target.isAlive() || !owner.isAlive()) {
			say("There's no one left to fight");
			return true;
		}
		if(!owner.isNear(target)) {
			//go after the target first; the fight happens next turn if they are still there
			say("I'm going after " + target.getName() + " in " + target.getLocation().getName() + "!");
			owner.moveTo(target.getLocation());
			return false;
		}
		int random = context.getRandom(0, owner.getDvR() + target.getDvR() + 1);
		if(random < owner.getDvR()) {
			say("I won the fight against " + target.getName() + "!");
			int damageToWinner = context.getRandom(1, 11) + 1;
			int damageToLoser = context.getRandom(1, 20);
			owner.changeHealth(-damageToWinner);
			owner.setAttacker(target);
			target.changeHealth(-damageToLoser);
			target.setAttacker(owner);
			context.emit(new SimEvent.FightResolved(context.getGeneration(), owner.getId(), target.getId(), owner.getId(),
					damageToWinner, damageToLoser));
			poisonMaybe(owner, target);
			target.checkDeath();
			return !target.isAlive();
		}
		else {
			say("I... lost the fight against " + target.getName() + "!");
			int damageToWinner = context.getRandom(1, 20);
			int damageToLoser = context.getRandom(1, 10) + 1;
			target.changeHealth(-damageToWinner);
			target.setAttacker(owner);
			owner.changeHealth(-damageToLoser);
			owner.setAttacker(target);
			context.emit(new SimEvent.FightResolved(context.getGeneration(), owner.getId(), target.getId(), target.getId(),
					damageToWinner, damageToLoser));
			poisonMaybe(target, owner);
			target.checkDeath();
			return true;
		}
	}

	private void poisonMaybe(Deity winner, Deity loser) {
		if(loser.isAlive() && loser.getHealth() > 0 && context.getRandom(0, 100) < POISON_CHANCE) {
			loser.addStatus(new Poisoned(loser, winner, context, context.getRandom(2, 7), context.getRandom(2, 5)));
			context.narrate(winner, 1, "I have poisoned " + loser.getName() + "!");
			context.emit(new SimEvent.Poisoned(context.getGeneration(), loser.getId(), winner.getId()));
		}
	}
}
