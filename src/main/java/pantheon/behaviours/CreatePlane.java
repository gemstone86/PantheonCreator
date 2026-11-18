package pantheon.behaviours;

import pantheon.cosmos.Cosmos;
import pantheon.cosmos.Plane;
import pantheon.gods.Deity;
import pantheon.sim.SimEvent;
import pantheon.sim.Simulation;

public class CreatePlane extends Behaviour {

	private Plane plane;

	public CreatePlane(Deity owner, Simulation context) {
		super(owner, context);
	}

	@Override
	public boolean act() {
		Cosmos cosmos = context.getCosmos();
		if(plane == null) {
			if(cosmos.getConstructionSize() > 0 && context.getRandom(1, 50) > 10) {
				plane = cosmos.getPlanesUnderConstruction().getFirst();
				say("I should work together with other gods on " + plane.getName());
			}
			else {
				plane = cosmos.startBuildingPlane(owner);
				say("I want to make something of my own: " + plane.getName() + "!");
				context.emit(new SimEvent.PlaneStarted(context.getGeneration(), plane.getName(), owner.getId()));
			}
			plane.startConstruction(owner);
		}
		if(plane.isDone()) {
			say(plane.getName() + " was finished by someone else");
			return true;
		}
		say("I'm working on " + plane.getName());

		if(context.getRandom(1, 100) > 75 && owner.getDomains().size() > 0) {
			int randIndex = context.getRandom(0, owner.getDomains().size());
			String randString = owner.getDomains().get(randIndex);
			if(plane.addConcept(randString)) {
				say("I have added " + randString + " to " + plane.getName() + "!");
			}
			else {
				say(randString + " was already part of " + plane.getName());
			}
		}

		if(plane.construct(context.getRandom(1, 5 + owner.getDvR() + 1))) {
			cosmos.finishPlane(plane);
			say("I'm done with " + plane.getName() + "!");
			context.emit(new SimEvent.PlaneFinished(context.getGeneration(), plane.getName(), owner.getId()));
			//the god who finishes a plane makes it their home and settles there; a supreme deity stays in the Void
			if(!owner.isSupreme()) {
				if(owner.getHomePlane() == null) {
					owner.setHomePlane(plane);
				}
				owner.moveTo(plane);
			}
			return true;
		}
		say(plane.getName() + " is at " + plane.getProgress() + "%");
		return false;
	}
}
