package gods;

import java.util.LinkedList;

import behaviours.*;
import cosmos.place;
import main.runtime;

public class deity {

	
	private boolean alive = true;
	private String name;
	private LinkedList<String> domains = new LinkedList<String>();
	private int divineRank;
	private LinkedList<deity> parents = new LinkedList<deity>();
	private LinkedList<deity> children = new LinkedList<deity>();
	private LinkedList<behaviour> behaviours;
	private boolean mateFound = false;
	private int willingness;
	private int baseWillingness;
	private deity mate;
	private int age = 0;
	private runtime context;
	
	private int orderChaos;
	private int goodEvil;
	
	private place homeplane;

	private int sexuality;
	private int gender;

	private int random = (int) Math.random();

	public deity(String name, int DvR, int Sex, int sexuality, runtime context) {

		
		this.context = context;
		
		
		this.name = name;
		this.domains = domains;
		this.divineRank = DvR;
		this.gender = Sex;
		this.sexuality = sexuality;

		behaviours = new LinkedList<behaviour>();

		behaviours.add(new findMotivation(this, context));
		
		baseWillingness = random(30,70);
		willingness = baseWillingness;
	}

	public deity (String name, int DvR, int Sex, int Sexuality, LinkedList<deity> parents, runtime context) {
		this.context = context;
		this.name = name;
		this.divineRank = DvR;
		this.gender = Sex;
		this.sexuality = Sexuality;

		behaviours = new LinkedList<behaviour>();
		
		behaviours.add(new findMotivation(this, context));

		baseWillingness = random(30,70);
		willingness = baseWillingness;
		
		this.parents = parents;

		//add code to fetch 1-2 domain from father, 1-2 domain from mother and then 1-2 random domains.
		setUpinheritDomains();
		inheritAlignment();
	}

	private void inheritDomain(deity parent) {
		if(parent.getDomains().size() > 1) {
			LinkedList<String> inheritance = new LinkedList<String>();

			for(String element:parent.getDomains()) {
				inheritance.add(element);
			}

			int numOfInheritance = random(1,2);

			for(int index = 0; index < numOfInheritance; index++) {
				int rnd = random(1,inheritance.size());
				addDomain(inheritance.get(rnd));
				inheritance.remove(rnd);
			}
		}
		else {
			addDomain(parent.getDomains().element());
		}
	}

	public LinkedList<String> getDomains(){
		return domains;
	}

	public LinkedList<deity> getParents(){
		return parents;
	}

	private void setUpinheritDomains() {
		for(deity parent:parents) {
			inheritDomain(parent);
		}
		//get one or two additional domains.
		int randomDomains = random(1,2);
		for(int i = 0; i<randomDomains;i++) {
			addDomain(context.randomDomain());
		}
	}
	
	private void inheritAlignment() {
		
	}

	public void addBehaviour(behaviour behaviour) {
		this.behaviours.add(behaviour);
	}

	public String getGenderPronoun(int sex) {
		if(sex == 1) {
			return "Male";
		}
		else if (sex == 3) {
			return "Female";
		}
		else {
			return "Asexual";
		}
	}

	public String getSexualityNoun(int sex, int sexuality) {
		if(sex == sexuality && sex != 2) {
			return "Homosexual";
		}
		if(sex == 1 && sexuality == 3 || sex == 3 && sexuality ==1 ) {
			return "Straight";
		}
		if(sex == 2) {
			return "Pansexual";
		}
		return "Bisexual";
	}

	public void deityActs() {
		System.out.println("\t"+this.getName() + " (Age: "+age+", DvR " + getDvR()+")" + " (" + getSexualityNoun(gender, sexuality) + " " +getGenderPronoun(getGender()) +") " + " "+this.getDomains().toString() + " acts");
		for(int i = 0; i<behaviours.size(); i++) {
			behaviour action = behaviours.get(i);
			boolean satisfied = action.act();
			if(satisfied) {
				behaviours.remove(i);
			}
		}
		age++;
	}

	public int getGender() {
		return gender;
	}

	public String name() {
		return name;
	}

	public int getSexuality() {
		return sexuality;
	}

	public boolean checkCompatibility(int suitorGender) {
		if((sexuality == 2) & suitorGender == (1|3) | sexuality == suitorGender) {
			return true;
		}
		else if(sexuality == 2 & suitorGender == 2 & (random(1,100) < 51)) {
			return true;
		}
		return false;
	}

	public boolean checkIfMateFound() {
		return mateFound;
	}
	public void setMateFound(boolean set, deity seducer) {
		mateFound = set;
		mate = seducer;
	}
	public void resetMate() {
		mateFound = false;
		mate = null;
	}

	public int random(int start, int end) {
		return (int) ((Math.random() * (end - start)) + start);
	}

	public String getName() {
		return name;
	}

	public LinkedList<deity> getChildren(){
		return children;
	}

	public int getDvR() {
		return divineRank;
	}

	public boolean seduce(deity seducer) {
		int modifier = 0;
		if(parents != null) {
			for(deity parent:parents) {

				if(parent == seducer) {
					modifier +=20;
				}
				if(parent.getChildren().contains(seducer)) {
					modifier +=5;
				}
			}
		}

		if((random(1,100)+modifier) <= willingness) {
			willingness = baseWillingness;
			return true;
		}
		willingness+=2;
		return false;
	}

	public void addChild(deity newDeity) {
		children.add(newDeity);
	}

	public boolean update() {
		mateFound = false;

		if(random(1,100)<age) {
			System.out.println("-----------------------------------"+this.getName() + " has died");
			alive = false;
			return true;
		}

		return false;
	}

	public LinkedList<behaviour> getBehaviour(){
		return behaviours;
	}
	
	public void addDomain(String domain) {
		for(int i = 0; i<domains.size();i++) {
			if(domain.equals(domains.get(i))){
				return;
			}
		}
		domains.add(domain);
	}
	
	public String toString(){
		String status = "";
		
		if(!alive) {
			status = "Dead ";
		}
		
		return ("\t"+this.getName() + " (DvR " + this.getDvR()+")" + " (" +status+this.getGenderPronoun(this.getGender()) +") " + " "+this.getDomains().toString());
	}
	
	public String finalToString() {
		String status = "";
		
		if(!alive) {
			status = "Dead ";
		}
		
		String returnString = "\t"+this.getName() + " (DvR " + this.getDvR()+")" + " (" +status+this.getGenderPronoun(this.getGender()) +") " + " "+this.getDomains().toString();
		for(deity parent:getParents()) {
			returnString += "\n\t\tParent:" + parent.getName();
		}
		return returnString;
	}
}
