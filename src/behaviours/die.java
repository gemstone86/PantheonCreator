package behaviours;


import java.util.LinkedList;

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
		if(!owner.getStatus2()) {
			owner.setStatus(false);

			//context.getListOfDeities().remove(owner);
			//context.getListofInactiveDeities().add(owner);
			context.toInactive(owner);

			System.out.println("\t"+owner.getName() + " has died");
		}
		//LinkedList<String> domains = owner.getDomains();

		//		for(int i = 0; i<domains.size();i++) {
		//			context.returnDomainsToPool(domains.get(i));
//		}
		return true;
		
	}
}
