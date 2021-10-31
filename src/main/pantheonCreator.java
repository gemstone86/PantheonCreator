
package main;

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

		int generations = 20;
		
		for(int i = 0; i<generations+1; i++) {
			System.out.println("----" + "Generation " + i +"----");
			for(deity deity:runtime.getListOfDeities()) {
				deity.deityActs();
			}
			for(int j = 0; j<runtime.getListOfDeities().size();j++) {
				runtime.getListOfDeities().get(j).update();
			}
			if(runtime.getListOfDeities().size() <1) {
				int generate = runtime.getRandom(1, 100);
				if(generate > 74) {
					runtime.createChild(null,null);
					System.out.println("WORLD IS EMPTY");
				}
			}
			for(deity newDeity:runtime.getNextGen()) {
				runtime.addDeity(newDeity);
			}
			runtime.resetNextGen();
		}
		System.out.println("---Final Pantheon---");
		for(deity deity:runtime.getListOfDeities()) {
			System.out.println(deity.finalToString());
		}
		System.out.println("---Cosmos---");
		System.out.println(runtime.getCosmos().toString());
	}
}
