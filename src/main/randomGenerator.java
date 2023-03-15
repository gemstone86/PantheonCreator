package main;

import java.util.LinkedList;

public class randomGenerator {

	private runtime context;
	private LinkedList<String> conceptPool = new LinkedList<String>();


	public randomGenerator(runtime context) {
		this.context = context;
		for(String in:listOfDomains) {
			conceptPool.add(in);
		}
		if(context.getDebug()) {
			for(int i = 0; i<conceptPool.size();i++) {
				System.out.println(conceptPool.get(i));
			}
		}
	}
	

	

	private String[] malePrefix = {"Ra", "Re", "Set", "Ze", "Ga", "Fre", "Vo", "Ak", "Ur", "Kil", "Rak", "Mel", "Var", "Med", "Mys", "Il", "Sar", "Akor", "Heru", "Sob", "Tal"};
	private String[] maleMidfix = {"ys", "ul", "ve", "av", "ar", "vu", "el", "de", "ne", "ah",""};
	private String[] maleSuffix = {"", "o", "or", "us", "otor", "meron", "kad", "ir", "es", "dran", "'ur", "'u", "rh", "tr", "an", "mater","an","ak"};
	
	private String[] planePrefix = {"Mid", "Olym", "Nifel", "Had", "Ker", "Hel", "Har", "Rehn", "Elm", "Ereb", "Fae", "Kor", "Nem","Kem", "Mun"};
	private String[] planeSuffix = {"", "gard", "pus", "heim", "es", "as", "ren", "altor", "run", "karn", "rorn", "nae","dana","nar","vel"};
	
	private String[] femalePrefix = {"Ba", "He", "Is", "Shi", "Seka", "Vi", "Ti", "Cei", "Hel", "Shu", "Idu","Fre","Fr","He"};
	private String[] femaleSuffix = {"", "st", "sti", "is", "ra", "ah", "a", "ia", "'ia", "na", "ena", "ni", "anda", "n","igga","ja"};
	
//	private String[] listOfMaleNames = {"Rao", "Re", "Meln", "Varu", "Kil","gahn", "Torak", "Freden", "medri", "vorn", "var", "Galden",
//			"Ilmater", "Helm", "Bvaal", "Nern", "Hur", "Her'u", "Sardor", "Akkad", "Mystr", "vreten", "vadir", "Turh", "Oda", "Zaes", 
//			"kvanir", "kurdor", "gherem", "Ure", "Hardran", "Rehdan","Normu"};

	private String[] listOfFemaleNames = {"Basti", "Sekashi", "Sunra", "Vira", "friah", "tira", "Sia", "Ceira", "Curai","Sori", "Zora",
			"Kitara", "Helena", "Herai", "Shira", "Isa", "Misani", "Bika", "Miranda", "Si", "Shu", "Iduna", "Hethia", "Bikra", "Zia", 
			"Mitani", "Kara", "Sonia", "Ira", "Osora"};

	private String[] listOfDomains = {"Good", "Chaos", "Evil", "Order", "Creation", "Destruction", "Storms", "Health", "Disease", "Anger", 
			"Air", "Fire", "Earth", "Water", "Ocean", "Nature", "Animals", "Law", "War", "Peace", "Justice", "Sun", "Moon", "Death", "Fertility",
			"Harvest", "Beauty", "Luck", "Wealth","Magic", "Travel", "Mountains"};

	private LinkedList<String> listOfConcepts = new LinkedList<String>();
	

	public String randomDomain() {
		return listOfDomains[context.getRandom(0,listOfDomains.length)];
	}


	public String getRandomName(int sex){
		if(sex == 1) {
//			return listOfMaleNames[context.getRandom(0,listOfMaleNames.length)];
			int randomize = context.getRandom(1, 100);
			if(randomize < 20) {
				return malePrefix[context.getRandom(0,malePrefix.length)];
			}
			else if(randomize >19 && randomize < 81) {
				return malePrefix[context.getRandom(0,malePrefix.length)] + maleSuffix[context.getRandom(0, maleSuffix.length)];
			}
			else {
				return malePrefix[context.getRandom(0,malePrefix.length)] + maleMidfix[context.getRandom(0, maleMidfix.length)] + maleSuffix[context.getRandom(0, maleSuffix.length)];
			}
			
		}
		else if(sex == 3) {
			return femalePrefix[context.getRandom(0,femalePrefix.length)] + femaleSuffix[context.getRandom(0, femaleSuffix.length)];
		}
		else {
			if(context.getRandom(1,2) == 1) {
//				return listOfMaleNames[context.getRandom(0,listOfMaleNames.length)];
				return malePrefix[context.getRandom(0,malePrefix.length)] + maleSuffix[context.getRandom(0, maleSuffix.length)];
			}
			else {
//				return listOfFemaleNames[context.getRandom(0,listOfFemaleNames.length)];
				return femalePrefix[context.getRandom(0,femalePrefix.length)] + femaleSuffix[context.getRandom(0, femaleSuffix.length)];
			}
		}
	}

	public String getRandomPlaneName() {
		return planePrefix[context.getRandom(0,planePrefix.length)] + planeSuffix[context.getRandom(0,planeSuffix.length)];
	}

	public void returnDomainToPool(String returnedConcept) {
			conceptPool.add(returnedConcept);
	}
	
	public String getRandomDomainFromPool() {
		if(conceptPool.size()>0) {
			int rnd = context.getRandom(0, conceptPool.size());


			String randConcept = conceptPool.get(rnd);
			conceptPool.remove(rnd);

			return randConcept;
		}
		else return "empty";
	}
	
	public String getAvailableDomains() {
		String returnValue ="[";
		for(int i = 0; i<conceptPool.size(); i++)
			if(i==0) {
				returnValue += conceptPool.get(i);
			}
			else{
				returnValue += ", "+ conceptPool.get(i);
			}
		return (returnValue + "]");
	}

}
