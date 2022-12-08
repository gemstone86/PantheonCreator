package behaviours;

import cosmos.*;
import gods.deity;
import main.runtime;

public class createPlane extends behaviour {

	private runtime context;
	private deity owner;
	private plane plane;
	private cosmos cosmos;

	public createPlane(deity owner,runtime context) {
		this.context = context;
		this.owner = owner;
		this.cosmos = context.getCosmos();

	}

	@Override
	public boolean act() {
		if(plane == null) {
			if(context.getCosmos().getConstructionSize()>0) {
				if(context.getRandom(1, 50) > 10){
					System.out.println("\t\t\tI should work togheter with other gods");
					plane = cosmos.getPlanesUnderConstruction().getFirst();
				}
				else {
					System.out.println("\t\t\tI want to make something of my own!");
					plane = cosmos.startBuildingPlane();
				}
			}
			else {
				System.out.println("\t\t\tI want to make something of my own!");
				plane = cosmos.startBuildingPlane();
			}
		}
		System.out.println("\t\t\tI'm working on "+plane.getName());
		
		if(context.getRandom(1,100)>75 && owner.getDomains().size()>0) {
			int randIndex = context.getRandom(0, owner.getDomains().size());
			String randString = owner.getDomains().get(randIndex);
			System.out.println("\t\t\tI should add " + randString + " to the plane!");
			
			if(plane.addConcept(randString)) {
				System.out.println("\t\t\tIt's now a part of the plane!!");
			}
			else {
				System.out.println("\t\t\tIt was already added to the plane");
			}
		}
		
		if(plane.construct(context.getRandom(1, owner.getDvR()))) {
			System.out.println("\t\t\tI'm done with " + plane.getName());
			owner.setHomePlane(plane);
			owner.setLocation(plane);
			return true;
		}
		return false;
	}


}
