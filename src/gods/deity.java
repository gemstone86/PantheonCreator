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
	private LinkedList<deity> rivals = new LinkedList<deity>();
	private LinkedList<relation> relations = new LinkedList<relation>();
	private LinkedList<deity> contacts = new LinkedList<deity>();
	
	private deity last_attacker;
	
	private int health;
	
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

	public deity(String name, int DvR, int Sex, int sexuality, runtime context) {

		
		this.context = context;
		
		
		
		this.name = name;
		this.divineRank = DvR;
		this.gender = Sex;
		this.sexuality = sexuality;

		behaviours = new LinkedList<behaviour>();

		behaviours.add(new findMotivation(this, context));
		
		baseWillingness = random(30,70);
		willingness = baseWillingness;
		this.health = context.getRandom(1, 100) + DvR;
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
	}

	private void inheritDomain(deity parent) {
		if(parent.getDomains().size() > 1) {
			LinkedList<String> inheritance = new LinkedList<String>();

			for(String element:parent.getDomains()) {
				inheritance.add(element);
			}

			int numOfInheritance = random(1,3);

			for(int index = 0; index < numOfInheritance; index++) {
				int rnd = random(0,inheritance.size());
				addDomain(inheritance.get(rnd));
				inheritance.remove(rnd);
			}
		}
		else if(parent.getDomains().size() == 1) {
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
		int randomDomains = random(1,3);
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
		if(!alive) {
			return;
		}
		System.out.println("\t"+this.getName() + " (Age: "+age+", DvR " + getDvR()+", " + getHealth() + " health)" + " (" + getSexualityNoun(gender, sexuality) + " " +getGenderPronoun(getGender()) +") " + " "+this.getDomains().toString() + " acts");
		//Act on a snapshot so behaviours added this turn start next turn,
		//and removing a finished behaviour doesn't skip the one after it.
		LinkedList<behaviour> currentBehaviours = new LinkedList<behaviour>(behaviours);
		for(behaviour action:currentBehaviours) {
			boolean satisfied = action.act();
			if(satisfied) {
				behaviours.remove(action);
			}
			checkDeath();
			if(!this.alive) {
				break;
			}
		}
		age++;
		last_attacker = null;
	}

	/*
	 * Checks whether this deity has run out of health and, if so, marks it as slain.
	 */
	public void checkDeath() {
		if(this.alive && this.health <= 0) {
			this.alive = false;
			this.removeAllBehaviours();
			if(last_attacker != null) {
				System.out.println("\t\t"+this.getName() + " was slain by " + last_attacker.getName() + "!");
			}
			else {
				System.out.println("\t\t"+this.getName() + " was slain!");
			}
			context.toInactive(this);
		}
	}

	public boolean isAlive() {
		return alive;
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
		if(sexuality == 2) {
			if(suitorGender == 1 || suitorGender == 3) {
				return true;
			}
			return suitorGender == 2 && random(1,101) <= 50;
		}
		return sexuality == suitorGender;
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

	/*
	 * Uses the runtime's seeded generator so that a run can be reproduced from its seed.
	 */
	public int random(int start, int end) {
		return context.getRandom(start, end);
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
		if(!alive) {
			return false;
		}
		mateFound = false;

		//check if deity has died
		if(random(1,100)<age) {
			this.addBehaviour(new die(this, context));
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
			returnString += "\n\t\tParent:" + parent.getName() + " ("+parent.getStatus() + ")";
		}
		return returnString;
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

	public relation getRelationTo(deity other) {
		for(relation contact:relations) {
			if(contact.getTarget() == other) {
				return contact;
			}
		}
		return null;
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
}
