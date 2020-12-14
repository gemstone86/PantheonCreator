package behaviours;

import gods.deity;
import main.runtime;

public class createConcept extends behaviour{

	private deity owner;
	private runtime context;
	
	private String myDomain = "";
	
	private boolean domainSet = false;
	
	public createConcept(deity deity, runtime context) {
		owner = deity;
		this.context = context;
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean act() {
		// TODO Auto-generated method stub
		System.out.println("\t\t"+owner.getName() + " creates a concept");
		
		if(!domainSet) {
			myDomain = context.randomDomain();
			if(myDomain == "empty") {
			System.out.println("\t\t\tThere's nothing left to create");
			return true;
			}
			else {
			
			System.out.println("\t\t\tI want to create " + myDomain +"!");
			}
		}
		else {
			System.out.println("\t\t\tI'm still trying to create "+myDomain +"!");
		}
		
		if(context.getRandom(1, 100) < 50) {
			System.out.println("\t\t\tI have created "+myDomain+"!");
			context.getCosmos().createConcept(myDomain);
			return true;
		}
		return false;
	}
}
