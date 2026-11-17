package pantheon;

import pantheon.gods.Deity;
import pantheon.sim.ConsoleListener;
import pantheon.sim.Simulation;

/*
 * Command-line version: runs a whole simulation and prints it as text.
 * Usage: PantheonCreator [g <generations>] [s <seed>]
 */
public class PantheonCreator {

	private int generations = Simulation.DEFAULT_GENERATIONS;
	private long seed = 1;

	public static void main(String[] args) {
		PantheonCreator program = new PantheonCreator();

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
				System.out.println("Usage: PantheonCreator [g <generations>] [s <seed>]");
				return;
			}
		}

		program.run();
	}

	public void run() {
		Simulation simulation = new Simulation(seed, generations);
		simulation.addListener(new ConsoleListener(System.out));
		System.out.println("Seed: " + simulation.getSeed());

		try {
			while(!simulation.isFinished()) {
				simulation.step();
			}
		}
		catch(RuntimeException e) {
			System.out.println("The simulation crashed. Seed: " + simulation.getSeed());
			throw e;
		}

		System.out.println("---Final Pantheon---");
		for(Deity deity:simulation.getListOfDeities()) {
			System.out.println(deity.finalToString());
		}
		System.out.println("---Fallen Gods---");
		for(Deity deity:simulation.getListofInactiveDeities()) {
			System.out.println(deity.finalToString());
		}
		System.out.println("---Cosmos---");
		System.out.println(simulation.getCosmos().toString());
	}
}
