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
	
	private static String[] listOfMaleNames = {"Rao", "Re", "Meln", "Varu", "Kil","gahn", "Torak", "Freden", "medri", "vorn", "var", "Galden",
			"Ilmater", "Helm", "Bvaal", "Nern", "Hur", "Her'u", "Sardor", "Akkad", "Mystr", "vreten", "vadir", "Turh", "Oda", "Zaes", 
			"kvanir", "kurdor", "gherem"};
	
	private static String[] listOfFemaleNames = {"Basti", "Sekashi", "Sunra", "Vira", "friah", "tira", "Sia", "Ceira", "Curai","Sori", "Zora",
			"Kitara", "Helena", "Herai", "Shira", "Isa", "Misani", "Bika", "Miranda", "Si", "Shu", "Iduna", "Hethia", "Bikra", "Zia", 
			"Mitani", "Kara", "Sonia"};
	
	public String getName() {
		return listOfMaleNames.get;
	}
	
	public static void resetGeneration() {
		nextGeneration = new LinkedList<deity>();
	}
	public static void createChild(deity father, deity mother) {
		LinkedList<deity> parents = new LinkedList<deity>();
		parents.add(father);
		parents.add(mother);
		int sex = getRandom(1,3);
		String newName;
		if(sex == 1) {
			newName = listOfMaleNames[getRandom(0,listOfMaleNames.length)];
		}
		else if(sex == 3) {
			newName = listOfFemaleNames[getRandom(0,listOfFemaleNames.length)];
		}
		else if(sex == 2) {
			if(getRandom(1,2) == 1) {
				newName = listOfMaleNames[getRandom(0,listOfMaleNames.length)];
			}
			else {
				newName = listOfFemaleNames[getRandom(0,listOfFemaleNames.length)];
			}
			
		}
		int average = (father.getDvR()+mother.getDvR())/2;
		
		int newDvR = getRandom(average-5,average+2);
		String name, int DvR, int Sex, int Sexuality, LinkedList<deity> parents
		nextGeneration.add(new deity(newName, newDvR, getRandom(1,3), parents, nextGeneration, nextGeneration));
	}
}
