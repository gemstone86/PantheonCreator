package pantheon.gods;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import pantheon.behaviours.*;
import pantheon.cosmos.Place;
import pantheon.sim.SimEvent;
import pantheon.sim.Simulation;
import pantheon.sim.Snapshot;

public class Deity {

	private final String id;
	private final int bornIn;
	private Integer diedIn;
	private String causeOfDeath;
	private Deity killer;
	private int maxHealth;
	private LinkedList<Deity> partners = new LinkedList<Deity>();

	private boolean alive = true;
	private String name;
	private LinkedList<String> domains = new LinkedList<String>();
	private int divineRank;
	private LinkedList<Deity> parents = new LinkedList<Deity>();
	private LinkedList<Deity> children = new LinkedList<Deity>();
	private LinkedList<Deity> rivals = new LinkedList<Deity>();
	private LinkedList<Relation> relations = new LinkedList<Relation>();
	private LinkedList<Deity> contacts = new LinkedList<Deity>();
	
	private Deity last_attacker;
	
	private int health;
	
	private LinkedList<Behaviour> behaviours;
	private boolean mateFound = false;
	private int willingness;
	private int baseWillingness;
	private Deity mate;
	private int age = 0;
	private Simulation context;
	
	private int orderChaos;
	private int goodEvil;
	
	private Place homeplane;
	private Place location;

	private int sexuality;
	private int gender;

	public Deity(String name, int DvR, int Sex, int sexuality, Simulation context) {
		this.context = context;
		this.id = context.nextDeityId();
		this.bornIn = context.getGeneration();
		this.location = context.getCosmos();
		this.name = name;
		this.divineRank = DvR;
		this.gender = Sex;
		this.sexuality = sexuality;

		behaviours = new LinkedList<Behaviour>();

		behaviours.add(new FindMotivation(this, context));
		
		baseWillingness = random(30,70);
		willingness = baseWillingness;
		this.health = context.getRandom(1, 100) + DvR;
		this.maxHealth = health;
	}

	public Deity (String name, int DvR, int Sex, int Sexuality, LinkedList<Deity> parents, Simulation context) {
		this.context = context;
		this.id = context.nextDeityId();
		this.bornIn = context.getGeneration();
		this.location = context.getCosmos();
		this.name = name;
		this.divineRank = DvR;
		this.gender = Sex;
		this.sexuality = Sexuality;
		
		behaviours = new LinkedList<Behaviour>();
		
		behaviours.add(new FindMotivation(this, context));

		baseWillingness = random(30,70);
		willingness = baseWillingness;
		
		this.parents = parents;

		this.health = context.getRandom(1, 100) + DvR;
		this.maxHealth = health;
		
		//add code to fetch 1-2 domain from father, 1-2 domain from mother and then 1-2 random domains.
		setUpinheritDomains();
		inheritAlignment();
	}

	private void inheritDomain(Deity parent) {
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

	public LinkedList<Deity> getParents(){
		return parents;
	}

	private void setUpinheritDomains() {
		for(Deity parent:parents) {
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

	public void addBehaviour(Behaviour behaviour) {
		this.behaviours.add(behaviour);
	}
	
	public void removeAllBehaviours() {
		this.behaviours = new LinkedList<Behaviour>();
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
		context.narrate(this, 0, this.getName() + " (Age: "+age+", DvR " + getDvR()+", " + getHealth() + " health)" + " (" + getSexualityNoun(gender, sexuality) + " " +getGenderPronoun(getGender()) +") " + " "+this.getDomains().toString() + " acts");
		//Act on a snapshot so behaviours added this turn start next turn,
		//and removing a finished behaviour doesn't skip the one after it.
		LinkedList<Behaviour> currentBehaviours = new LinkedList<Behaviour>(behaviours);
		for(Behaviour action:currentBehaviours) {
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
			if(last_attacker != null) {
				context.narrate(this, 1, this.getName() + " was slain by " + last_attacker.getName() + "!");
			}
			else {
				context.narrate(this, 1, this.getName() + " was slain!");
			}
			die("slain", last_attacker);
		}
	}

	/*
	 * Marks this deity as dead, records why, and moves it out of the active pantheon at the end of the generation.
	 */
	public void die(String cause, Deity killer) {
		if(!alive) {
			return;
		}
		this.alive = false;
		this.removeAllBehaviours();
		this.diedIn = context.getGeneration();
		this.causeOfDeath = cause;
		this.killer = killer;
		context.toInactive(this);
		context.emit(new SimEvent.DeityDied(context.getGeneration(), id, cause, killer == null ? null : killer.getId()));
	}

	public boolean isAlive() {
		return alive;
	}

	public int getHealth() {
		return health;
	}
	
	public Deity getLast_attacker() {
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
	public void setMateFound(boolean set, Deity seducer) {
		mateFound = set;
		mate = seducer;
		if(seducer != null && !partners.contains(seducer)) {
			partners.add(seducer);
		}
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

	public LinkedList<Deity> getChildren(){
		return children;
	}

	public int getDvR() {
		return divineRank;
	}

	/*
	 * being seduced by another deity
	 */
	public boolean seduce(Deity seducer) {
		int modifier = 0;
		//if you have parents check if 
		if(parents != null) {
			for(Deity parent:parents) {
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

	public void addChild(Deity newDeity) {
		children.add(newDeity);
	}

	/* Gods don't age at all before this age. */
	public static final int OLD_AGE_START = 20;
	/* After OLD_AGE_START, the chance of dying each generation grows by this many per mille (tenths of a percent) per year. */
	public static final int OLD_AGE_RISK_PER_YEAR = 5;

	/*
	 * Rolls for death by old age. The chance is zero until OLD_AGE_START and then grows steadily,
	 * e.g. with the defaults 5% at age 30 and 10% at age 40.
	 */
	private boolean diesOfOldAge() {
		int yearsOld = age - OLD_AGE_START;
		if(yearsOld <= 0) {
			return false;
		}
		return random(0, 1000) < yearsOld * OLD_AGE_RISK_PER_YEAR;
	}

	public boolean update() {
		if(!alive) {
			return false;
		}
		mateFound = false;

		//check if deity has died of old age
		if(diesOfOldAge()) {
			this.addBehaviour(new Die(this, context));
			return true;
		}

		return false;
	}

	public LinkedList<Behaviour> getBehaviour(){
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
		for(Deity parent:getParents()) {
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

	public Relation getRelationTo(Deity other) {
		for(Relation contact:relations) {
			if(contact.getTarget() == other) {
				return contact;
			}
		}
		return null;
	}

	public void addRelation(Relation target) {
		if(contacts.contains(target.getTarget())) {
			return;
		}
		contacts.add(target.getTarget());
		relations.add(target);
		
	}

	public LinkedList<Relation> getRelations() {
		// TODO Auto-generated method stub
		return relations;
	}

	public boolean doYouKnowMe(Deity target) {
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
	
	public Place getLocation() {
		return location;
	}

	public Place getHomePlane() {
		return homeplane;
	}

	public void setHomePlane(Place plane) {
		this.homeplane = plane;
	}

	/* True if both deities are in the same place. */
	public boolean isNear(Deity other) {
		return other != null && other.location == this.location;
	}

	/*
	 * Travels to another place and reports it.
	 */
	public void moveTo(Place destination) {
		if(destination == null || destination == location) {
			return;
		}
		Place from = location;
		location = destination;
		context.narrate(this, 1, "I have travelled from " + from.getName() + " to " + destination.getName());
		context.emit(new SimEvent.DeityMoved(context.getGeneration(), id, from.getName(), destination.getName()));
	}

	/* Places a newborn where it was born, without reporting a journey. */
	public void setBirthplace(Place place) {
		this.location = place;
	}

	public void setAttacker(Deity attacker) {
		last_attacker = attacker;
	}

	public String getId() {
		return id;
	}

	public int getAge() {
		return age;
	}

	/*
	 * A plain-data copy of this deity for snapshots and events.
	 */
	public Snapshot.DeityView toView() {
		List<String> parentIds = new ArrayList<String>();
		for(Deity parent:parents) {
			parentIds.add(parent.getId());
		}
		List<String> mateIds = new ArrayList<String>();
		for(Deity partner:partners) {
			mateIds.add(partner.getId());
		}
		List<Snapshot.RelationView> relationViews = new ArrayList<Snapshot.RelationView>();
		for(Relation relation:relations) {
			if(relation.getTarget() != null) {
				relationViews.add(new Snapshot.RelationView(relation.getTarget().getId(), relation.getRelation()));
			}
		}
		return new Snapshot.DeityView(id, name, alive, gender, getGenderPronoun(gender), getSexualityNoun(gender, sexuality),
				divineRank, health, Math.max(maxHealth, health), age, bornIn, diedIn, causeOfDeath,
				killer == null ? null : killer.getId(),
				location == null ? null : location.getName(),
				homeplane == null ? null : homeplane.getName(),
				List.copyOf(domains), parentIds, mateIds, relationViews);
	}
}
