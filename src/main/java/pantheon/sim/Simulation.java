package pantheon.sim;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import pantheon.cosmos.Cosmos;
import pantheon.cosmos.Plane;
import pantheon.gods.Deity;

/*
 * Holds the whole state of one pantheon and advances it one generation at a time with step().
 * Everything that happens is reported to listeners as SimEvents.
 * Not thread-safe: callers must make sure only one thread uses a Simulation at a time.
 */
public class Simulation {

	public static final int DEFAULT_GENERATIONS = 25;
	/* Chance in percent that a god socialises with, or courts, someone on the same plane rather than anyone at all. */
	public static final int SAME_PLACE_PREFERENCE = 80;

	private LinkedList<Deity> listOfActiveDeities;
	private LinkedList<Deity> listOfInactiveDeities = new LinkedList<Deity>();
	private LinkedList<Deity> nextGeneration;
	private LinkedList<Deity> toInactive = new LinkedList<Deity>();
	private Cosmos theCosmos = new Cosmos(this);

	private RandomGenerator rndGen;
	boolean debug = false;
	long seed = 1;
	Random generator = new Random(seed);

	private final int maxGenerations;
	private int generation = 0;
	private int deityCounter = 0;
	private final List<Consumer<SimEvent>> listeners = new CopyOnWriteArrayList<Consumer<SimEvent>>();

	public Simulation() {
		this(1, DEFAULT_GENERATIONS);
	}

	public Simulation(long seed) {
		this(seed, DEFAULT_GENERATIONS);
	}

	public Simulation(long seed, int maxGenerations) {
		this.seed = seed;
		this.generator = new Random(seed);
		this.maxGenerations = maxGenerations;
		newPantheon();

		this.rndGen = new RandomGenerator(this);

		LinkedList<String> ureDomains = new LinkedList<String>();
		LinkedList<String> iraDomains = new LinkedList<String>();
		LinkedList<String> chaDomains = new LinkedList<String>();

		ureDomains.add(rndGen.randomDomain()); ureDomains.add(rndGen.randomDomain()); ureDomains.add(rndGen.randomDomain());
		iraDomains.add(rndGen.randomDomain()); iraDomains.add(rndGen.randomDomain()); iraDomains.add(rndGen.randomDomain());
		chaDomains.add(rndGen.randomDomain()); chaDomains.add(rndGen.randomDomain()); chaDomains.add(rndGen.randomDomain());

		addDeity(new Deity("Ure", this.getRandom(19, 21), 1, 3, this));
		addDeity(new Deity("Ira", this.getRandom(19, 21), 3, 1, this));
		addDeity(new Deity("cha", this.getRandom(1, 20), getRandom(1,4), getRandom(1,4), this));

		for(int i = 0; i<3; i++) {
			listOfActiveDeities.get(0).addDomain(ureDomains.get(i));
			listOfActiveDeities.get(1).addDomain(iraDomains.get(i));
			listOfActiveDeities.get(2).addDomain(chaDomains.get(i));
		}
	}

	public Simulation(boolean debug) {
		this.debug = debug;
		this.maxGenerations = DEFAULT_GENERATIONS;
		newPantheon();

		this.rndGen = new RandomGenerator(this);

		LinkedList<String> ureDomains = new LinkedList<String>();
		LinkedList<String> iraDomains = new LinkedList<String>();
		ureDomains.add("Creation"); ureDomains.add("Order"); ureDomains.add("Good");
		iraDomains.add("Destruction"); iraDomains.add("Chaos"); iraDomains.add("Evil");

		addDeity(new Deity("Ure", 20, 1, 3, this));
		addDeity(new Deity("Ira", 20, 3, 1, this));

		for(int i = 0; i<3; i++) {
			listOfActiveDeities.get(0).addDomain(ureDomains.get(i));
			listOfActiveDeities.get(1).addDomain(iraDomains.get(i));
		}
	}

	/*
	 * Plays one generation: every living deity acts, ages and may die, new deities join,
	 * and dead ones leave the active pantheon. Does nothing once the simulation is finished.
	 */
	public void step() {
		if(isFinished()) {
			return;
		}
		generation++;
		emit(new SimEvent.GenerationStarted(generation, listOfActiveDeities.size()));

		if(listOfActiveDeities.size() > 0) {
			for(Deity deity:new LinkedList<Deity>(listOfActiveDeities)) {
				deity.deityActs();
			}
			for(Deity deity:new LinkedList<Deity>(listOfActiveDeities)) {
				deity.update();
			}
		}
		if(listOfActiveDeities.size() < 1) {
			narrate(null, 0, "WORLD IS EMPTY");
			int generate = getRandom(1, 100);
			if(generate > 74) {
				createChild(null,null);
				narrate(null, 0, "WORLD IS NO LONGER EMPTY");
			}
		}
		for(Deity newDeity:nextGeneration) {
			addDeity(newDeity);
		}
		for(Deity deadDeity:toInactive) {
			listOfInactiveDeities.add(deadDeity);
			listOfActiveDeities.remove(deadDeity);
		}
		toInactive = new LinkedList<Deity>();
		resetNextGen();

		emit(new SimEvent.GenerationEnded(generation, snapshot()));
	}

	public boolean isFinished() {
		return generation >= maxGenerations;
	}

	public int getGeneration() {
		return generation;
	}

	public int getMaxGenerations() {
		return maxGenerations;
	}

	public void addListener(Consumer<SimEvent> listener) {
		listeners.add(listener);
	}

	public void removeListener(Consumer<SimEvent> listener) {
		listeners.remove(listener);
	}

	public void emit(SimEvent event) {
		for(Consumer<SimEvent> listener:listeners) {
			listener.accept(event);
		}
	}

	/*
	 * Reports a line of commentary. who may be null for world-level messages; depth is how far it is indented.
	 */
	public void narrate(Deity who, int depth, String text) {
		emit(new SimEvent.Narration(generation, who == null ? null : who.getId(), depth, text));
	}

	public String nextDeityId() {
		deityCounter++;
		return "d" + deityCounter;
	}

	/*
	 * A plain-data copy of the current state, including dead deities.
	 */
	public Snapshot snapshot() {
		List<Snapshot.DeityView> deities = new ArrayList<Snapshot.DeityView>();
		for(Deity deity:listOfInactiveDeities) {
			deities.add(deity.toView());
		}
		for(Deity deity:listOfActiveDeities) {
			deities.add(deity.toView());
		}
		deities.sort((a, b) -> Integer.compare(Integer.parseInt(a.id().substring(1)), Integer.parseInt(b.id().substring(1))));

		List<Snapshot.PlaneView> planes = new ArrayList<Snapshot.PlaneView>();
		for(Plane plane:theCosmos.getPlanes()) {
			planes.add(plane.toView());
		}
		return new Snapshot(seed, generation, maxGenerations, isFinished(), deities, planes,
				List.copyOf(theCosmos.getAllConcepts()), rndGen.getDomainPool());
	}

	public void newPantheon() {
		listOfActiveDeities = new LinkedList<Deity>();
		nextGeneration = new LinkedList<Deity>();
	}

	public void addDeity(Deity deity) {
		listOfActiveDeities.add(deity);
	}

	public void removeDeity(int index) {
		listOfActiveDeities.remove(index);
	}
	public void removeDeity(Deity deity) {
		listOfActiveDeities.remove(deity);
	}

	public LinkedList<Deity> getListOfDeities(){
		return listOfActiveDeities;
	}

	/*
	 * Returns a random number from start (inclusive) up to end (exclusive).
	 * If the range is empty (end <= start), start is returned instead of throwing.
	 */
	public int getRandom(int start, int end) {
		if(end <= start) {
			return start;
		}
		return generator.nextInt(end - start) + start;
	}

	public long getSeed() {
		return seed;
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

	public void createChild(Deity father, Deity mother) {
		Deity newDeity = buildDeity(father, mother);
		nextGeneration.add(newDeity);

		if(father != null) {
			father.addChild(newDeity);
		}
		if(mother != null) {
			mother.addChild(newDeity);
		}
		narrate(father, 1, "A new deity is born: " + newDeity.getName());
		emit(new SimEvent.DeityBorn(generation, newDeity.toView()));
	}

	/*
	 * Adds a brand-new god with no parents straight into the pantheon, in the Void.
	 * Can be called between generations, e.g. from the web app.
	 */
	public Deity addRandomDeity() {
		Deity deity = buildDeity(null, null);
		addDeity(deity);
		narrate(null, 0, deity.getName() + " has appeared in " + deity.getLocation().getName());
		emit(new SimEvent.DeityBorn(generation, deity.toView()));
		emit(new SimEvent.WorldChanged(generation, snapshot()));
		return deity;
	}

	/*
	 * Picks someone for owner to deal with. With chanceSamePlace percent it is someone in the same place,
	 * if anyone else is there; otherwise anyone in the pantheon (which may be owner itself).
	 */
	public Deity getRandomDeityPreferringNear(Deity owner, int chanceSamePlace) {
		LinkedList<Deity> near = new LinkedList<Deity>();
		for(Deity deity:listOfActiveDeities) {
			if(deity != owner && deity.isAlive() && deity.isNear(owner)) {
				near.add(deity);
			}
		}
		if(!near.isEmpty() && getRandom(0, 100) < chanceSamePlace) {
			return near.get(getRandom(0, near.size()));
		}
		return getRandomDeity();
	}

	/*
	 * Creates a deity from two parents (either may be null), born where the first parent is.
	 */
	private Deity buildDeity(Deity father, Deity mother) {
		LinkedList<Deity> parents = new LinkedList<Deity>();
		if(father !=null) parents.add(father);
		if(mother !=null) parents.add(mother);

		//Set sex and sexuality
		int sex = getRandom(1,4);
		int sexuality = 0;

		String newName = rndGen.getRandomName(sex);

		if(sex == 1) {
			sexuality = generateSexuality(3,1);
		}
		else if(sex == 3) {
			sexuality = generateSexuality(1,3);
		}
		else if(sex == 2) {
			sexuality = getRandom(1,4);
			if(getRandom(1,3) == 1) {
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
		Deity newDeity = new Deity(newName, newDvR, sex, sexuality, parents, this);
		Deity parent = father != null ? father : mother;
		if(parent != null) {
			newDeity.setBirthplace(parent.getLocation());
		}
		return newDeity;
	}

	public LinkedList<Deity> getNextGen(){
		return nextGeneration;
	}
	public void resetNextGen() {
		nextGeneration = new LinkedList<Deity>();
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

	public Cosmos getCosmos() {
		return theCosmos;
	}

	public boolean getDebug() {
		return debug;
	}

	public LinkedList<Deity> getListofInactiveDeities() {
		return listOfInactiveDeities;
	}

	public Deity getRandomDeity() {
		return listOfActiveDeities.get(getRandom(0,listOfActiveDeities.size()));
	}

	public String getConcepts() {
		return rndGen.getAvailableDomains();
	}

	public void returnDomainsToPool(String string) {
		rndGen.returnDomainToPool(string);
	}

	/*
	 * Marks a deity to leave the active pantheon at the end of this generation.
	 */
	public void toInactive(Deity add) {
		if(!toInactive.contains(add)) {
			toInactive.add(add);
		}
	}
}
