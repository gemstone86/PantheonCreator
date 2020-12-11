package main;

import java.util.LinkedList;

import gods.deity;

public class runtime {

	static LinkedList<deity> listOfDeities;
	static LinkedList<deity> nextGeneration;
	
	boolean debug = false;
	
	
	public runtime() {
		newPantheon();

		String[] ureDomains = {"creation", "order", "good"};
		String[] iraDomains = {"destruction", "chaos", "evil"};
		
		addDeity(new deity("Ure", ureDomains, 20, 1, 3));
		addDeity(new deity("Ira", iraDomains, 20, 3, 1));
	}
	public runtime(boolean debug) {
		newPantheon();

		String[] ureDomains = {"creation", "order", "good"};
		String[] iraDomains = {"destruction", "chaos", "evil"};
		
		addDeity(new deity("Ure", ureDomains, 20, 1, 3));
		addDeity(new deity("Ira", iraDomains, 20, 3, 1));
	}
	
	public static void newPantheon() {
		listOfDeities = new LinkedList<deity>();
		nextGeneration = new LinkedList<deity>();
	}
	
	public static void addDeity(deity deity) {
		listOfDeities.add(deity);
	}
	
	public static void removeDeity(int index) {
		listOfDeities.remove(index);
	}
	
	public static LinkedList<deity> getListOfDeities(){
		return listOfDeities;
	}
	
	public static int getRandom(int start, int end) {
		return (int) ((Math.random() * (end - start)) + start);
	}
}
