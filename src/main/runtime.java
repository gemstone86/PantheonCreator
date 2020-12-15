package main;

import java.util.LinkedList;

import cosmos.cosmos;
import gods.deity;

public class runtime {

	private LinkedList<deity> listOfDeities;
	private LinkedList<deity> nextGeneration;
	private cosmos theCosmos = new cosmos(this);

	private nameGenerator nameGen;
	boolean debug = false;

	public runtime() {
		newPantheon();

		this.nameGen = new nameGenerator(this);
		
		LinkedList<String> ureDomains = new LinkedList<String>();
		LinkedList<String> iraDomains = new LinkedList<String>();

		ureDomains.add(nameGen.randomDomain()); ureDomains.add(nameGen.randomDomain()); ureDomains.add(nameGen.randomDomain());
		iraDomains.add(nameGen.randomDomain()); iraDomains.add(nameGen.randomDomain()); iraDomains.add(nameGen.randomDomain());

		addDeity(new deity("Ure", 20, 1, 3, this));
		addDeity(new deity("Ira", 20, 3, 1, this));
		
		for(int i = 0; i<3; i++) {
			listOfDeities.get(0).addDomain(ureDomains.get(i));
			listOfDeities.get(1).addDomain(iraDomains.get(i));
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
			listOfDeities.get(0).addDomain(ureDomains.get(i));
			listOfDeities.get(1).addDomain(iraDomains.get(i));
		}
	}

	public void newPantheon() {
		listOfDeities = new LinkedList<deity>();
		nextGeneration = new LinkedList<deity>();
	}

	public void addDeity(deity deity) {
		listOfDeities.add(deity);
	}

	public void removeDeity(int index) {
		listOfDeities.remove(index);
	}
	public void removeDeity(deity deity) {
		listOfDeities.remove(deity);
	}

	public LinkedList<deity> getListOfDeities(){
		return listOfDeities;
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
		LinkedList<deity> parents = new LinkedList<deity>();
		parents.add(father);
		parents.add(mother);
		
		int sex = getRandom(1,3);
		int sexuality = 0;


		String newName ="";
		newName = nameGen.getRandomName(sex);
		
		if(sex == 1) {
			sexuality = generateSexuality(3,1);
		}
		else if(sex == 3) {
			
			sexuality = generateSexuality(1,3);
		}
		else if(sex == 2) {
			sexuality = getRandom(1,3);
			if(getRandom(1,2) == 1) {
				newName = nameGen.getRandomName(1);
			}
			else {
				newName = nameGen.getRandomName(3);
			}
			
		}
		int average = (father.getDvR()+mother.getDvR())/2;
		int newDvR = getRandom(average-5,average+2);

		deity newDeity = new deity(newName, newDvR, sex, sexuality, parents, this);
		
		nextGeneration.add(newDeity);
		
		father.addChild(newDeity);
		mother.addChild(newDeity);
		
	}

	public LinkedList<deity> getNextGen(){
		return nextGeneration;
	}
	public void resetNextGen() {
		nextGeneration = new LinkedList<deity>();
	}

	public String getRandomDomainFromPool() {
		return nameGen.getRandomDomainFromPool();
	}
	
	public String randomDomain() {
		return nameGen.randomDomain();
	}
	
	public String getRandomName() {
		return nameGen.getRandomName(1);
	}
	
	public String getRandomPlaneName() {
		return nameGen.getRandomPlaneName();
	}
	
	public cosmos getCosmos() {
		return theCosmos;
	}
}
