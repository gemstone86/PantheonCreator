package behaviours;

import java.util.LinkedList;

import gods.deity;
import main.runtime;

public class createConcept extends behaviour{

	private deity owner;
	private runtime context;
	
	public createConcept(deity deity, runtime context) {
		owner = deity;
		this.context = context;
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean act() {
		// TODO Auto-generated method stub
		System.out.println("\t\t"+owner.getName() + " creates a concept");
		System.out.println(context);
		return true;
	}

}
