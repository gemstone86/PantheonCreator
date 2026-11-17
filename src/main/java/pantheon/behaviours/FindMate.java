package pantheon.behaviours;

import java.util.LinkedList;

import pantheon.gods.Deity;
import pantheon.sim.SimEvent;
import pantheon.sim.Simulation;

public class FindMate extends Behaviour {

	public FindMate(Deity owner, Simulation context) {
		super(owner, context);
	}

	@Override
	public boolean act() {
		say(owner.getName() + " tries to find a mate");

		if(!owner.checkIfMateFound()) {
			//build list of potential mates in this generation
			LinkedList<Deity> listOfPotentialMates = new LinkedList<Deity>();
			for(Deity candidate:context.getListOfDeities()) {
				if(candidate != owner && candidate.isAlive() && !candidate.checkIfMateFound()
						&& candidate.checkCompatibility(owner.getGender())) {
					listOfPotentialMates.add(candidate);
				}
			}
			if(listOfPotentialMates.size() > 0) {
				Deity mate = listOfPotentialMates.get(context.getRandom(0, listOfPotentialMates.size()));

				//seduce potential mate
				if(mate.seduce(owner)) {
					mate.setMateFound(true, owner);
					owner.setMateFound(true, mate);
					say(mate.getName() + " has agreed to be my mate!");
					context.emit(new SimEvent.MateFound(context.getGeneration(), owner.getId(), mate.getId()));

					context.createChild(owner, mate);
					return true;
				}
			}
		}
		return false;
	}
}
