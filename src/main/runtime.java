package main;

import java.util.LinkedList;

import gods.deity;

public class runtime {

	static LinkedList<deity> listOfDeities;
	static LinkedList<deity> nextGeneration;

	boolean debug = false;


	public runtime() {
		newPantheon();

		LinkedList<String> ureDomains = new LinkedList<String>();
		LinkedList<String> iraDomains = new LinkedList<String>();
		ureDomains.add(randomDomain()); ureDomains.add(randomDomain()); ureDomains.add(randomDomain());
		iraDomains.add(randomDomain()); iraDomains.add(randomDomain()); iraDomains.add(randomDomain());

		addDeity(new deity(getName(1), ureDomains, 20, 1, 3));
		addDeity(new deity(getName(3), iraDomains, 20, 3, 1));
	}
	public runtime(boolean debug) {
		newPantheon();

		LinkedList<String> ureDomains = new LinkedList<String>();
		LinkedList<String> iraDomains = new LinkedList<String>();
		ureDomains.add("Creation"); ureDomains.add("Order"); ureDomains.add("Good");
		iraDomains.add("Destruction"); iraDomains.add("Chaos"); iraDomains.add("Evil");

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
	public static void removeDeity(deity deity) {
		listOfDeities.remove(deity);
	}

	public static LinkedList<deity> getListOfDeities(){
		return listOfDeities;
	}

	public static int getRandom(int start, int end) {
		return (int) ((Math.random() * (end - start)) + start);
	}

	private static String[] listOfMaleNames = {"Rao", "Re", "Meln", "Varu", "Kil","gahn", "Torak", "Freden", "medri", "vorn", "var", "Galden",
			"Ilmater", "Helm", "Bvaal", "Nern", "Hur", "Her'u", "Sardor", "Akkad", "Mystr", "vreten", "vadir", "Turh", "Oda", "Zaes", 
			"kvanir", "kurdor", "gherem", "Ure"};

	private static String[] listOfFemaleNames = {"Basti", "Sekashi", "Sunra", "Vira", "friah", "tira", "Sia", "Ceira", "Curai","Sori", "Zora",
			"Kitara", "Helena", "Herai", "Shira", "Isa", "Misani", "Bika", "Miranda", "Si", "Shu", "Iduna", "Hethia", "Bikra", "Zia", 
			"Mitani", "Kara", "Sonia", "Ira"};

	private static String[] listOfDomains = {"Good", "Chaos", "Evil", "Order", "Creation", "Destruction", "Storms", "Health", "Disease", "Anger", 
			"Air", "Fire", "Earth", "Water", "Ocean", "Nature", "Animals", "Law", "War", "Peace", "Justice", "Sun", "Moon", "Death", "Fertility",
			"Harvest", "Beauty", "Luck", "Wealth","Magi", "Travel", "Mountains"};
	
	private static LinkedList<String> worldConcepts = new LinkedList<String>();
	
	
	public static LinkedList<String> getListOfConcept(){
		return worldConcepts;
	}
	
	public static String randomDomain() {
		return listOfDomains[getRandom(0,listOfDomains.length)];
	}
	
	public static void resetGeneration() {
		nextGeneration = new LinkedList<deity>();
	}

	public static int generateSexuality(int mostCommon, int leastCommon) {
		int rnd = getRandom(1,100);
		if(rnd < 6) {
			return leastCommon;
		}
		else if(rnd >5 & rnd < 11) {
			return 2;
		}
		return mostCommon;
	}

	public static String getName(int sex){
		if(sex == 1) {
			return listOfMaleNames[getRandom(0,listOfMaleNames.length)];

		}
		else if(sex == 3) {
			return listOfFemaleNames[getRandom(0,listOfFemaleNames.length)];
		}
		else {
			if(getRandom(1,2) == 1) {
				return listOfMaleNames[getRandom(0,listOfMaleNames.length)];
			}
			else {
				return listOfFemaleNames[getRandom(0,listOfFemaleNames.length)];
			}
		}
	}

	public static void createChild(deity father, deity mother) {
		LinkedList<deity> parents = new LinkedList<deity>();
		parents.add(father);
		parents.add(mother);
		
		int sex = getRandom(1,3);
		int sexuality = 0;


		String newName ="";
		if(sex == 1) {
			newName = getName(1);
			sexuality = generateSexuality(3,1);
		}
		else if(sex == 3) {
			newName = getName(3);
			sexuality = generateSexuality(1,3);
		}
		else if(sex == 2) {
			if(getRandom(1,2) == 1) {
				newName = getName(1);
			}
			else {
				newName = getName(3);
			}
			sexuality = getRandom(1,3);
		}
		int average = (father.getDvR()+mother.getDvR())/2;
		int newDvR = getRandom(average-5,average+2);

		deity newDeity = new deity(newName, newDvR, sex, sexuality, parents);
		
		nextGeneration.add(newDeity);
		
		father.addChild(newDeity);
		mother.addChild(newDeity);
		
	}

	public static LinkedList<deity> getNextGen(){
		return nextGeneration;
	}
	public static void resetNextGen() {
		nextGeneration = new LinkedList<deity>();
	}
}
