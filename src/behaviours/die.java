package behaviours;


import cosmos.*;
import gods.deity;
import main.runtime;

public class die extends behaviour {

	private runtime context;
	private deity owner;
	private plane plane;
	private cosmos cosmos;

	public die(deity owner,runtime context) {
		this.context = context;
		this.owner = owner;
		this.cosmos = context.getCosmos();

	}

	@Override
	public boolean act() {
		owner.setStatus(false);
		
		context.getListOfDeities().remove(owner);
		context.getListofInactiveDeities().add(owner);
		
		System.out.println("-----------------------------------"+owner.getName() + " has died");
		return true;
	}


}
