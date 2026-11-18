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
 * if there are none, the god opens a path to another plane (and travels it next time).
 * Gods very rarely go back to the Void on their own: only VOID_RETURN_CHANCE percent of the time.
 * (They also go there when fleeing a fight or their crimes; see Fight and Crime.)
 * A supreme deity mostly stays in the Void and only now and then visits a plane.
 */
public class Move extends Behaviour {

	/* Chance in percent that a god on a plane who wants to move goes back to the Void. */
	public static final int VOID_RETURN_CHANCE = 2;
	/* Chance in percent that a supreme deity in the Void who wants to move visits a plane. */
	public static final int SUPREME_WANDER_CHANCE = 15;

	public Move(Deity owner, Simulation context) {
		super(owner, context);
	}

	@Override
	public boolean act() {
		Cosmos cosmos = context.getCosmos();
		Place here = owner.getLocation();
		LinkedList<Plane> finished = cosmos.getFinishedPlanes();

		if(owner.isSupreme()) {
			if(here != cosmos) {
				owner.moveTo(cosmos);
			}
			else if(!finished.isEmpty() && context.getRandom(0, 100) < SUPREME_WANDER_CHANCE) {
				owner.moveTo(finished.get(context.getRandom(0, finished.size())));
			}
			else {
				say("I am content in the Void");
			}
			return true;
		}

		if(here == cosmos) {
			if(finished.isEmpty()) {
				say("There is nowhere to go yet; no plane has been finished");
				return true;
			}
			owner.moveTo(finished.get(context.getRandom(0, finished.size())));
			return true;
		}

		if(context.getRandom(0, 100) < VOID_RETURN_CHANCE) {
			say("I long for the Void");
			owner.moveTo(cosmos);
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
			say("There is nowhere to go from " + here.getName() + ", so I will stay");
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
