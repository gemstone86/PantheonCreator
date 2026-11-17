
package main;


import java.util.LinkedList;

import gods.deity;

public class pantheonCreator {

	private int generations = 20;
	private long seed = 1;

	public static void main(String[] args) {
		pantheonCreator program = new pantheonCreator();

		//Handle in values: "g <number>" sets the number of generations, "s <number>" sets the seed
		for(int i = 0; i < args.length; i++) {
			switch(args[i]) {
			case "g":
				try {
					program.generations = Integer.parseInt(args[++i]);
				}
				catch(Exception e) {
					System.out.println("Expected a number after the generation parameter (g)");
					return;
				}
				break;
			case "s":
				try {
					program.seed = Long.parseLong(args[++i]);
				}
				catch(Exception e) {
					System.out.println("Expected a number after the seed parameter (s)");
					return;
				}
				break;
			default:
				System.out.println("Unknown parameter: " + args[i]);
				System.out.println("Usage: pantheonCreator [g <generations>] [s <seed>]");
				return;
			}
		}

		//start program
		program.run();
	}


	public void run() {
		runtime runtime = new runtime(seed);
		System.out.println("Seed: " + runtime.getSeed());
		
		for(int i = 0; i<generations+1; i++) {
			System.out.println("----" + "Generation " + i + " (" + runtime.getListOfDeities().size() + ")" +"----");
			if(runtime.getListOfDeities().size() > 0) {
				try {
					for(deity deity:new LinkedList<deity>(runtime.getListOfDeities())) {
						deity.deityActs();
					}

					for(deity deity:new LinkedList<deity>(runtime.getListOfDeities())) {
						deity.update();
					}
				}
				catch(RuntimeException e) {
					System.out.println("The simulation crashed. Seed: " + runtime.getSeed());
					throw e;
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
			System.out.println(deity.finalToString());
		}
		System.out.println("---Fallen Gods---");
		for(deity deity:runtime.getListofInactiveDeities()) {
			System.out.println(deity.finalToString());
		}
		System.out.println("---Cosmos---");
		System.out.println(runtime.getCosmos().toString());
	}
}
