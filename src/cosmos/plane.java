package cosmos;

import java.util.LinkedList;

import gods.deity;
import main.runtime;

public class plane extends place{

	int build = 0;
	
	boolean done = false;
	LinkedList<deity> builders = new LinkedList<deity>();
	runtime context;
	LinkedList<String> concepts = new LinkedList<String>();
	
	public plane(runtime context) {
		this.context = context;
		// TODO Auto-generated constructor stub
	}
	
	public boolean construct(int points) {
		build += points;
		if(build > 99) {
			done = true;
			return true;
		}
		return false;
	}
	public void startConstruction(deity builder){
		builders.add(builder);
	}
	
	public String toString() {
		String line = "";
		if(concepts.size() < 1) {
			return this.getName();
		}
		else {
			for(int i = 0; i<concepts.size();i++) {
				if(i==0) {
					line +=concepts.get(i);
				}
				else{
					line += ", "+concepts.get(i);
				}
			}
		}
		return this.getName() + " (" + line +")";
	}

	public boolean addConcept(String randString) {
		// TODO Auto-generated method stub
		
		if(concepts.contains(randString)) {
			return false;
		}
		concepts.add(randString);
		
		return true;
		
	}

}
