package gods;

import java.util.LinkedList;

import behaviours.*;
import cosmos.*;

import main.runtime;
import status.*;
import traits.*;

public class deity {

	
	private boolean alive = true;
	private String name;
	private LinkedList<String> domains = new LinkedList<String>();
	private int divineRank;
	private LinkedList<deity> parents = new LinkedList<deity>();
	private LinkedList<deity> children = new LinkedList<deity>();
	private LinkedList<deity> rivals = new LinkedList<deity>();
	private LinkedList<relation> relations = new LinkedList<relation>();
	private LinkedList<deity> contacts = new LinkedList<deity>();
	
	private deity last_attacker;
	
	private int health;
	
	private LinkedList<behaviour> behaviours;
	private LinkedList<status> status = new LinkedList<status>();
	
	private boolean mateFound = false;
	private int willingness;
	private int baseWillingness;
	private deity mate;
	private int age = 0;
	private runtime context;
	
	private LinkedList<trait> traits = new LinkedList<trait>();
	
	
	private int orderChaos;
	private int goodEvil;
	
	private place homeplane;
	private place location;
	
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
		this.health = context.getRandom(1, 100) + DvR;

		addTraits();
	}

	public void addTraits() {
		aggression = context.getRandom(-10, 10);
		lust = context.getRandom(-10, 10);
		creativity = context.getRandom(-10, 10);
		
		int nTraits = context.getRandom(1, 3);
		
		for(int i = 0; i<nTraits; i++) {
			this.traits.add(getNewTrait());
		}
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

		this.health = context.getRandom(1, 100) + DvR;
		
		//add code to fetch 1-2 domain from father, 1-2 domain from mother and then 1-2 random domains.
		setUpinheritDomains();
		inheritAlignment();
		
		addTraits();
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
	
	public void removeAllBehaviours() {
		this.behaviours = new LinkedList<behaviour>();
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
		System.out.println(this.toString() + " acts");
		//System.out.println("\t"+this.getName() + " (Age: "+age+", DvR " + getDvR()+", " + getHealth() + " health)" + " (" + getSexualityNoun(gender, sexuality) + " " +getGenderPronoun(getGender()) +", agg " + this.getAggression()+") " + this.getDomains().toString() + " acts");
//		System.out.println("\t\tTime to act!");
		for(trait i:traits) {
			i.motivate();
		}
		
		for(int i = 0; i<behaviours.size(); i++) {
			if(behaviours.get(i).act()) {
				behaviours.remove(i);
			}
		}
		if(status.size()>0) {System.out.println("\t\tChecking status effects!");}
		for(int i = 0; i<status.size();i++) {
			if(status.get(i).act()) {
				status.remove(i);
			}
		}
		if(this.health <= 0) {
			this.addBehaviour(new die(this, context));
	//		this.alive = false;
	//		this.removeAllBehaviours();
			if(getLast_attacker() != null) {
				System.out.println("\t\t"+this.getName() + " was slain by " + this.getLast_attacker().getName() + "!");
			}
		}
		age++;
		if(!this.alive) {
			this.removeAllBehaviours();
		}
		last_attacker = null;
	}

	public int getHealth() {
		return health;
	}
	
	public deity getLast_attacker() {
		return last_attacker;
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
	public deity getMate() {
		return mate;
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

	/*
	 * being seduced by another deity
	 */
	public boolean seduce(deity seducer) {
		int modifier = 0;
		//if you have parents check if 
		if(parents != null) {
			for(deity parent:parents) {
				//if the parent is the seducer, make it more difficult
				if(parent == seducer) {
					modifier +=20;
				}
				//If the seducer is my sibling, make it more difficult
				if(parent.getChildren().contains(seducer)) {
					modifier +=5;
				}
			}
		}

		//check if seduction is successfull
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

		//check if deity has died
		if(random(20,100)<age) {
			//this.addBehaviour(new die(this, context));
			System.out.println("\t" +getName() + " feels old");
			this.changeHealth(-context.getRandom(1, 20));
			if(health <= 0) {
				this.addBehaviour(new die(this, context));
			}
			
			return true;
		}

		return false;
	}

	public LinkedList<behaviour> getBehaviour(){
		return behaviours;
	}
	
	public boolean isAlive() {
		return alive;
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
		
		return ("\t"+this.getName() + " (Age: " + this.getAge() + ", DvR " + this.getDvR()+", "+this.getHealth() +" health)" + " (" +status+this.getGenderPronoun(this.getGender()) +") "  +"{" + this.getTraits().toString() + "}" + " (" + getSexualityNoun(gender, sexuality) + " " +getGenderPronoun(getGender()) +") " +this.getDomains().toString());
	}
	
	public int getAge() {
		return age;
	}
	
	public String finalToString() {
		String status = "";
		
		if(!alive) {
			status = "Dead ";
		}
		
		String returnString = "\t"+this.getName() + " (DvR " + this.getDvR()+")" + " (" + getSexualityNoun(gender, sexuality) + " " +status+this.getGenderPronoun(this.getGender()) +", agg " + this.getAggression() +") " + " "+this.getDomains().toString();
		
		returnString +="\n\t" + this.getTraits().toString();
		
		for(deity parent:getParents()) {
			returnString += "\n\t\tParent:" + parent.getName() + " ("+parent.getStatus() + ")";
		}
		return returnString;
	}

	public  LinkedList<trait> getTraits(){
		return traits;
	}
	
	private String getStatus() {
		if(!alive) {
			return "Dead";
		};
			return "Alive";
	}

	public void setStatus(boolean alive) {
		this.alive = alive;
	}

	public void addRelation(relation target) {
		if(contacts.contains(target.getTarget())) {
			return;
		}
		contacts.add(target.getTarget());
		relations.add(target);
		
	}

	public LinkedList<relation> getRelations() {
		// TODO Auto-generated method stub
		return relations;
	}

	public boolean doYouKnowMe(deity target) {
		if(this.contacts.contains(target)){
			return true;
		}
		return false;
	}

	public void setDvr(int i) {
		this.divineRank = i;
		
	}

	public void changeHealth(int i) {
		health += i;
	}
	
	public void setAttacker(deity attacker) {
		last_attacker = attacker;
	}

	public void setHomePlane(plane plane) {
		homeplane = plane;
	}
	public place getHomePlane() {
		return homeplane;
	}

	public void setLocation(plane plane) {
		location = plane;
	}

	public place getLocation() {
		return location;
	}

	public void addStatus(status newStatus) {
		status.add(newStatus);
		
	}

	private int aggression;
	
	public void setAggression(int i) {
		aggression += i;
		
	}

	public int getAggression() {
		return aggression;
	}

	private int lust;
	
	public void setLust(int i) {
		lust += i;
	}
	
	public int getLust() {
		return lust;
	}
	
	public trait getNewTrait() {
		int randomTrait = context.getRandom(76,125);
		
		return new ambitious(this,context);
		
//		if(isBetween(randomTrait,1,25)) { System.out.println("AGGRESSIVE " + randomTrait); return new aggressive(this, context); }
//		if(isBetween(randomTrait,26,50)) { System.out.println("LUSTFUL " + randomTrait); return new lustful(this, context); }
//		if(isBetween(randomTrait,51,75)) { System.out.println("PACIFIST " + randomTrait); return new pacifist(this, context); }
//		if(isBetween(randomTrait,76,100)) {System.out.println("CREATIVE " + randomTrait); return new creative(this,context);}
//		if(isBetween(randomTrait,101,125)) {System.out.println("AMBITIOUS " + randomTrait); return new ambitious(this,context);}
//		return null;
	}
	
	public boolean isBetween(int in, int low, int high) {
		if((in >= low) && (in <= high)) {
			return true;
		}
		return false;
	}

	private int creativity;
	
	public void setCreativity(int i) {
		creativity += i;
		
	}
}
