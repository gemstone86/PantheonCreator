
package main;

import java.util.LinkedList;

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

		int generations = 25;
		
		for(int i = 0; i<generations+1; i++) {
			System.out.println("----" + "Generation " + i + " (" + runtime.getListOfDeities().size() + ")" +"----");
			if(runtime.getListOfDeities().size() > 0) {
				try {
				for(deity deity:runtime.getListOfDeities()) {
					deity.deityActs();
				}
			
				for(int j = 0; j<runtime.getListOfDeities().size();j++) {
					runtime.getListOfDeities().get(j).update();
				}
				}
				catch(Error e) {
					System.out.println(runtime.seed);
				}
			}
			if(runtime.getListOfDeities().size() <1) {
				System.out.println("WORLD IS EMPTY");
				int generate = runtime.getRandom(1, 100);
				if(generate > 74) {
					runtime.createChild(null,null);
					System.out.println("WORLD IS NO LONGER EMPTY");
				}
			}
			for(deity newDeity:runtime.getNextGen()) {
				runtime.addDeity(newDeity);
			}
			for(deity newDeity:runtime.toInactive) {
				runtime.getListofInactiveDeities().add(newDeity);
				runtime.getListOfDeities().remove(newDeity);
			}
			runtime.toInactive = new LinkedList<deity>();
			System.out.println("Current domains: " +runtime.getConcepts());
			runtime.resetNextGen();
		}
		System.out.println("---Final Pantheon---");
		for(deity deity:runtime.getListOfDeities()) {
			System.out.println(deity.toString());
		}
		System.out.println("---Cosmos---");
		System.out.println(runtime.getCosmos().toString());
	}
}
