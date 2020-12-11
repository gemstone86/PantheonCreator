package behaviours;

import java.util.LinkedList;

import gods.deity;
import main.runtime;

public class createConcept extends behaviour{

	private deity owner;
	
	public createConcept(deity deity) {
		this.owner = deity;
	}

	@Override
	public void act(runtime runtime) {
		// TODO Auto-generated method stub
		System.out.println("\t\t"+owner.getName() + " creates a concept");
		
	}

}
