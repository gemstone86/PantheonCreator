package cosmos;

import java.util.LinkedList;

import gods.deity;
import main.runtime;

public class plane extends place{

	int build = 0;
	
	boolean done = false;
	LinkedList<deity> builders = new LinkedList<deity>();
	runtime context;
	
	public plane(runtime context) {
		this.context = context;
		// TODO Auto-generated constructor stub
	}
	
	public boolean construct(int points) {
		build += points;
		if(build > 100) {
			done = true;
			return true;
		}
		return false;
	}
	public void startConstruction(deity builder){
		builders.add(builder);
	}

}
