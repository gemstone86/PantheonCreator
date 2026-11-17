package pantheon.behaviours;

import pantheon.gods.Deity;
import pantheon.sim.SimEvent;
import pantheon.sim.Simulation;

public class CreateConcept extends Behaviour {

	private String myDomain = "";
	private boolean domainSet = false;

	public CreateConcept(Deity owner, Simulation context) {
		super(owner, context);
	}

	@Override
	public boolean act() {
		if(!domainSet) {
			myDomain = context.getRandomDomainFromPool();
			if(myDomain.equals("empty")) {
				say("There's nothing left to create");
				return true;
			}
			domainSet = true;
			say("I want to create " + myDomain + "!");
		}
		else {
			say("I'm still trying to create " + myDomain + "!");
		}

		if(context.getRandom(1, 100) < 50) {
			say("I have created " + myDomain + "!");
			owner.addDomain(myDomain);
			context.getCosmos().createConcept(myDomain);
			context.emit(new SimEvent.ConceptCreated(context.getGeneration(), owner.getId(), myDomain));
			return true;
		}
		return false;
	}
}
