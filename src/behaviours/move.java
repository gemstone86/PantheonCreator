package behaviours;

import cosmos.place;
import gods.deity;
import main.runtime;

public class move extends behaviour  {

	deity owner;
	runtime context;
	
	public move(deity owner, runtime context) {
		this.owner = owner;
		this.context = context;
	}
	
	@Override
	public boolean act() {
		System.out.println("\t\t"+owner.getName() + " tries to move");
		
		place moveFromHere;
		
		if(owner.getLocation() != null) {
			moveFromHere = owner.getLocation();
			if(moveFromHere.getNumberOfConnections() > 0) {
				
			}
			else if (context.getCosmos().getSizeOfCosmos() > 1){
				System.out.println("\t\tThere is nowhere to go! I will add a connection instead!");
				
			}
		}
		return true;
	}
}
