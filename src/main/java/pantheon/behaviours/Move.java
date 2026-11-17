package pantheon.behaviours;

import java.util.LinkedList;

import pantheon.cosmos.Cosmos;
import pantheon.cosmos.Place;
import pantheon.cosmos.Plane;
import pantheon.gods.Deity;
import pantheon.sim.SimEvent;
import pantheon.sim.Simulation;

/*
 * Travels to another place.
 * From the Void a god can enter any finished plane. From a plane a god follows the plane's connections;
 * if there are none, the god opens a path to another plane (and travels it next time),
 * or returns to the Void if there is no other plane.
 */
public class Move extends Behaviour {

	public Move(Deity owner, Simulation context) {
		super(owner, context);
	}

	@Override
	public boolean act() {
		Cosmos cosmos = context.getCosmos();
		Place here = owner.getLocation();
		LinkedList<Plane> finished = cosmos.getFinishedPlanes();

		if(here == cosmos) {
			if(finished.isEmpty()) {
				say("There is nowhere to go yet; no plane has been finished");
				return true;
			}
			owner.moveTo(finished.get(context.getRandom(0, finished.size())));
			return true;
		}

		LinkedList<Place> connections = here.getConnections();
		if(!connections.isEmpty()) {
			owner.moveTo(connections.get(context.getRandom(0, connections.size())));
			return true;
		}

		LinkedList<Plane> others = new LinkedList<Plane>(finished);
		others.remove(here);
		if(others.isEmpty()) {
			say("There is nowhere to go from " + here.getName() + "; I will return to the Void");
			owner.moveTo(cosmos);
			return true;
		}

		Plane destination = others.get(context.getRandom(0, others.size()));
		if(cosmos.connect(here, destination)) {
			say("There is nowhere to go! I have opened a path from " + here.getName() + " to " + destination.getName());
			context.emit(new SimEvent.PlacesConnected(context.getGeneration(), owner.getId(), here.getName(), destination.getName()));
		}
		//travel the new path next turn
		return false;
	}
}
