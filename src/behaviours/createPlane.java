package behaviours;

import cosmos.place;
import gods.deity;
import main.runtime;

public class createPlane extends behaviour {

	private runtime context;
	private deity owner;
	private place plane;
	
	public createPlane(deity owner,runtime context) {
		this.context = context;
		this.owner = owner;
	}
	
	@Override
	public boolean act() {
		
		
		return false;
	}
	
	
}
