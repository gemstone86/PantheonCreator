
package main;

import java.util.LinkedList;
import gods.*;

import gods.deity;

public class pantheonCreator {

	public static void main(String[] args) {
		//Handle in values
		for(String inArg:args) {
			switch(inArg) {
			case "g":
				try{
					
				}
				catch(Exception e) {
					System.out.println("Expected value after generation parameter");
				}
			}
		}
		
		//start program
		
		pantheonCreator program = new pantheonCreator();
		
		program.run();
	}

	
	public void run() {
		runtime runtime = new runtime();
		
		for(int i = 0; i<5; i++) {
			System.out.println("----" + "Generation " + i +"----");
			for(deity deity:runtime.getListOfDeities()) {
				deity.deityActs(runtime);
			}
			for(deity deity:runtime.getListOfDeities()) {
				deity.resetMe();
			}
			for(deity newDeity:runtime.getNextGen()) {
				runtime.addDeity(newDeity);
			}
			runtime.resetNextGen();
			
		}
	}
	
}
