package pantheon.behaviours;

import pantheon.gods.Deity;
import pantheon.sim.SimEvent;
import pantheon.sim.Simulation;

/*
 * Fights the target once per generation. Keeps fighting after a win until the target dies; stops after a loss.
 */
public class Fight extends Behaviour {

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
		int random = context.getRandom(0, owner.getDvR() + target.getDvR());
		if(random < owner.getDvR()) {
			say("I won the fight against " + target.getName() + "!");
			int damageToWinner = context.getRandom(0, target.getDvR() / 2);
			int damageToLoser = context.getRandom(1, owner.getDvR());
			owner.changeHealth(-damageToWinner);
			owner.setAttacker(target);
			target.changeHealth(-damageToLoser);
			target.setAttacker(owner);
			context.emit(new SimEvent.FightResolved(context.getGeneration(), owner.getId(), target.getId(), owner.getId(),
					damageToWinner, damageToLoser));
			target.checkDeath();
			return !target.isAlive();
		}
		else {
			say("I... lost the fight against " + target.getName() + "!");
			int damageToWinner = context.getRandom(0, owner.getDvR() / 2);
			int damageToLoser = context.getRandom(1, target.getDvR());
			target.changeHealth(-damageToWinner);
			target.setAttacker(owner);
			owner.changeHealth(-damageToLoser);
			owner.setAttacker(target);
			context.emit(new SimEvent.FightResolved(context.getGeneration(), owner.getId(), target.getId(), target.getId(),
					damageToWinner, damageToLoser));
			target.checkDeath();
			return true;
		}
	}
}
