package main;

import java.util.LinkedList;

public class nameGenerator {

	private runtime context;
	private LinkedList<String> conceptPool = new LinkedList<String>();


	public nameGenerator(runtime context) {
		this.context = context;
		for(String in:listOfDomains) {
			conceptPool.add(in);
		}

	}

	private String[] malePrefix = {"Ra", "Re", "Set", "Ze", "Ga", "Fre", "Vo", "Ak", "Ur", "Kil", "Rak"};
	private String[] maleSuffix = {"", "o", "or", "us", "otor", "meron", "kad", "ir", "es", "dran", "'ur", "'u", "rh"};
	
	private String[] planePrefix = {"Mid", "Olym", "Nifel", "Had", "Ker"};
	private String[] planeSuffix = {"", "gard", "pus", "heim", "es", "as", "ren"};
	
	private String[] listOfMaleNames = {"Rao", "Re", "Meln", "Varu", "Kil","gahn", "Torak", "Freden", "medri", "vorn", "var", "Galden",
			"Ilmater", "Helm", "Bvaal", "Nern", "Hur", "Her'u", "Sardor", "Akkad", "Mystr", "vreten", "vadir", "Turh", "Oda", "Zaes", 
			"kvanir", "kurdor", "gherem", "Ure", "Hardran", "Rehdan","Normu"};

	private String[] listOfFemaleNames = {"Basti", "Sekashi", "Sunra", "Vira", "friah", "tira", "Sia", "Ceira", "Curai","Sori", "Zora",
			"Kitara", "Helena", "Herai", "Shira", "Isa", "Misani", "Bika", "Miranda", "Si", "Shu", "Iduna", "Hethia", "Bikra", "Zia", 
			"Mitani", "Kara", "Sonia", "Ira"};

	private String[] listOfDomains = {"Good", "Chaos", "Evil", "Order", "Creation", "Destruction", "Storms", "Health", "Disease", "Anger", 
			"Air", "Fire", "Earth", "Water", "Ocean", "Nature", "Animals", "Law", "War", "Peace", "Justice", "Sun", "Moon", "Death", "Fertility",
			"Harvest", "Beauty", "Luck", "Wealth","Magic", "Travel", "Mountains"};


	public String randomDomain() {
		return listOfDomains[context.getRandom(0,listOfDomains.length)];
	}


	public String getRandomName(int sex){
		if(sex == 1) {
//			return listOfMaleNames[context.getRandom(0,listOfMaleNames.length)];
			return malePrefix[context.getRandom(0,malePrefix.length)] + maleSuffix[context.getRandom(0, maleSuffix.length)];
		}
		else if(sex == 3) {
			return listOfFemaleNames[context.getRandom(0,listOfFemaleNames.length)];
		}
		else {
			if(context.getRandom(1,2) == 1) {
				return listOfMaleNames[context.getRandom(0,listOfMaleNames.length)];
			}
			else {
				return listOfFemaleNames[context.getRandom(0,listOfFemaleNames.length)];
			}
		}
	}

	public String getRandomPlaneName() {
		return planePrefix[context.getRandom(0,planePrefix.length)] + planeSuffix[context.getRandom(0,planeSuffix.length)];
	}

	public String getRandomDomainFromPool() {
		if(conceptPool.size()<1) {
			int rnd = context.getRandom(0, conceptPool.size());


			String randConcept = conceptPool.get(rnd);
			conceptPool.remove(rnd);

			return randConcept;
		}
		else return "empty";
	}

}
