package behaviours;

import java.util.LinkedList;

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
		System.out.println("\t\t\tI'm now working on "+plane.getName());
		
		if(plane.construct(context.getRandom(1, 14))) {
			return true;
		}
		return false;
	}


}
