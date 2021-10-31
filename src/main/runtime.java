package main;

import java.util.LinkedList;

import cosmos.cosmos;
import gods.deity;

public class runtime {

	private LinkedList<deity> listOfActiveDeities;
	private LinkedList<deity> listOfInactiveDeities = new LinkedList<deity>();
	private LinkedList<deity> nextGeneration;
	private cosmos theCosmos = new cosmos(this);

	private randomGenerator rndGen;
	boolean debug = false;

	public runtime() {
		newPantheon();

		this.rndGen = new randomGenerator(this);
		
		LinkedList<String> ureDomains = new LinkedList<String>();
		LinkedList<String> iraDomains = new LinkedList<String>();

		ureDomains.add(rndGen.randomDomain()); ureDomains.add(rndGen.randomDomain()); ureDomains.add(rndGen.randomDomain());
		iraDomains.add(rndGen.randomDomain()); iraDomains.add(rndGen.randomDomain()); iraDomains.add(rndGen.randomDomain());

//		addDeity(new deity("Ure", 20, 1, 3, this));
//		addDeity(new deity("Ira", 20, 3, 1, this));
		
		for(int i = 0; i<3; i++) {
//			listOfActiveDeities.get(0).addDomain(ureDomains.get(i));
//			listOfActiveDeities.get(1).addDomain(iraDomains.get(i));
		}
		
	}
	
	public runtime(boolean debug) {
		newPantheon();

		LinkedList<String> ureDomains = new LinkedList<String>();
		LinkedList<String> iraDomains = new LinkedList<String>();
		ureDomains.add("Creation"); ureDomains.add("Order"); ureDomains.add("Good");
		iraDomains.add("Destruction"); iraDomains.add("Chaos"); iraDomains.add("Evil");

		addDeity(new deity("Ure", 20, 1, 3, this));
		addDeity(new deity("Ira", 20, 3, 1, this));
		
		for(int i = 0; i<3; i++) {
			listOfActiveDeities.get(0).addDomain(ureDomains.get(i));
			listOfActiveDeities.get(1).addDomain(iraDomains.get(i));
		}
	}

	public void newPantheon() {
		listOfActiveDeities = new LinkedList<deity>();
		nextGeneration = new LinkedList<deity>();
	}

	public void addDeity(deity deity) {
		listOfActiveDeities.add(deity);
	}

	public void removeDeity(int index) {
		listOfActiveDeities.remove(index);
	}
	public void removeDeity(deity deity) {
		listOfActiveDeities.remove(deity);
	}

	public LinkedList<deity> getListOfDeities(){
		return listOfActiveDeities;
	}

	public int getRandom(int start, int end) {
		return (int) ((Math.random() * (end - start)) + start);
	}
	
	private LinkedList<String> worldConcepts = new LinkedList<String>();
	
	
	public LinkedList<String> getListOfConcept(){
		return worldConcepts;
	}
	
	public void resetGeneration() {
		nextGeneration = new LinkedList<deity>();
	}

	public int generateSexuality(int mostCommon, int leastCommon) {
		int rnd = getRandom(1,100);
		if(rnd < 6) {
			return leastCommon;
		}
		else if(rnd >5 & rnd < 11) {
			return 2;
		}
		return mostCommon;
	}

	public void createChild(deity father, deity mother) {
		System.out.println("Creating a new deity");
		LinkedList<deity> parents = new LinkedList<deity>();
		if(father !=null) parents.add(father);
		if(mother !=null) parents.add(mother);
		
		//Set sex and sexuality
		int sex = getRandom(1,3);
		int sexuality = 0;


		String newName ="";
		newName = rndGen.getRandomName(sex);
		
		if(sex == 1) {
			sexuality = generateSexuality(3,1);
		}
		else if(sex == 3) {
			
			sexuality = generateSexuality(1,3);
		}
		else if(sex == 2) {
			sexuality = getRandom(1,3);
			if(getRandom(1,2) == 1) {
				newName = rndGen.getRandomName(1);
			}
			else {
				newName = rndGen.getRandomName(3);
			}
			
		}
		
		//Set DvR
		int a = getRandom(15,25);
		if(father != null) {
			a = father.getDvR();
		}
		int b = getRandom(15,25);
		if(mother != null) {
			b = mother.getDvR();
		}
		
		int average = (a+b)/2;
		int newDvR = getRandom(average-5,average+2);
		if (newDvR < 0) {
			newDvR =0;
		}
		
		//generate the new deity
		deity newDeity = new deity(newName, newDvR, sex, sexuality, parents, this);
		
		nextGeneration.add(newDeity);
		
		if(father != null) {
			father.addChild(newDeity);
		}
		if(mother != null) {
			mother.addChild(newDeity);
		}
		
	}

	public LinkedList<deity> getNextGen(){
		return nextGeneration;
	}
	public void resetNextGen() {
		nextGeneration = new LinkedList<deity>();
	}

	public String getRandomDomainFromPool() {
		return rndGen.getRandomDomainFromPool();
	}
	
	public String randomDomain() {
		return rndGen.randomDomain();
	}
	
	public String getRandomName() {
		return rndGen.getRandomName(1);
	}
	
	public String getRandomPlaneName() {
		return rndGen.getRandomPlaneName();
	}
	
	public cosmos getCosmos() {
		return theCosmos;
	}

	public boolean getDebug() {
		// TODO Auto-generated method stub
		return debug;
	}

	public LinkedList<deity> getListofInactiveDeities() {
		// TODO Auto-generated method stub
		return listOfInactiveDeities;
	}

	public deity getRandomDeity() {
		return listOfActiveDeities.get(getRandom(0,listOfActiveDeities.size()));
	}

	public void generateSpontaneousDeity() {
		// TODO Auto-generated method stub
		
	}
}
