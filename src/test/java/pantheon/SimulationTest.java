package pantheon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import pantheon.sim.SimEvent;
import pantheon.sim.Simulation;
import pantheon.sim.Snapshot;
import pantheon.web.SimulationSession;

class SimulationTest {

	private static Snapshot runToEnd(long seed, int generations) {
		Simulation simulation = new Simulation(seed, generations);
		while(!simulation.isFinished()) {
			simulation.step();
		}
		return simulation.snapshot();
	}

	@Test
	void manySeedsRunWithoutCrashing() {
		for(long seed = 1; seed <= 300; seed++) {
			long current = seed;
			assertThatCode(() -> runToEnd(current, 60)).as("seed %d", current).doesNotThrowAnyException();
		}
	}

	@Test
	void sameSeedGivesSameResult() {
		assertThat(runToEnd(42, 60)).isEqualTo(runToEnd(42, 60));
		assertThat(runToEnd(42, 60)).isNotEqualTo(runToEnd(43, 60));
	}

	@Test
	void reportsEveryGenerationAndStopsAtTheEnd() {
		Simulation simulation = new Simulation(7, 25);
		List<SimEvent> events = new ArrayList<SimEvent>();
		simulation.addListener(events::add);

		for(int i = 0; i < 40; i++) {
			simulation.step();
		}

		assertThat(simulation.isFinished()).isTrue();
		assertThat(events).filteredOn(e -> e instanceof SimEvent.GenerationEnded).hasSize(25);
		assertThat(simulation.snapshot().generation()).isEqualTo(25);
	}

	@Test
	void childrenAreBornWithEverySex() {
		List<Integer> sexes = new ArrayList<Integer>();
		for(long seed = 1; seed <= 100; seed++) {
			for(Snapshot.DeityView deity:runToEnd(seed, 60).deities()) {
				if(!deity.parentIds().isEmpty()) {
					sexes.add(deity.sex());
				}
			}
		}
		assertThat(sexes).contains(1, 2, 3);
	}

	@Test
	void nobodyDiesOfOldAgeWhileYoung() {
		for(long seed = 1; seed <= 100; seed++) {
			for(Snapshot.DeityView deity:runToEnd(seed, 80).deities()) {
				if("old age".equals(deity.causeOfDeath())) {
					assertThat(deity.diedIn() - deity.bornIn()).as("%s in seed %d", deity.name(), seed)
							.isGreaterThan(pantheon.gods.Deity.OLD_AGE_START);
				}
			}
		}
	}

	@Test
	void godsTravelBetweenFinishedPlanesOnly() {
		int moves = 0;
		for(long seed = 1; seed <= 100; seed++) {
			Simulation simulation = new Simulation(seed, 80);
			List<SimEvent.DeityMoved> moved = new ArrayList<SimEvent.DeityMoved>();
			simulation.addListener(e -> {
				if(e instanceof SimEvent.DeityMoved m) moved.add(m);
			});
			while(!simulation.isFinished()) {
				simulation.step();
			}
			Snapshot snapshot = simulation.snapshot();
			List<String> places = new ArrayList<String>();
			places.add(pantheon.cosmos.Cosmos.VOID);
			snapshot.planes().stream().filter(Snapshot.PlaneView::done).forEach(p -> places.add(p.name()));
			for(SimEvent.DeityMoved m:moved) {
				assertThat(places).contains(m.to());
				assertThat(m.to()).isNotEqualTo(m.from());
			}
			for(Snapshot.DeityView deity:snapshot.deities()) {
				assertThat(places).contains(deity.location());
			}
			moves += moved.size();
		}
		assertThat(moves).isGreaterThan(100);
	}

	@Test
	void addingAGodPutsItInTheVoidRightAway() {
		Simulation simulation = new Simulation(3, 10);
		int before = simulation.getListOfDeities().size();
		List<SimEvent> events = new ArrayList<SimEvent>();
		simulation.addListener(events::add);

		pantheon.gods.Deity added = simulation.addRandomDeity();

		assertThat(simulation.getListOfDeities()).hasSize(before + 1).contains(added);
		assertThat(added.getLocation().getName()).isEqualTo(pantheon.cosmos.Cosmos.VOID);
		assertThat(events).hasAtLeastOneElementOfType(SimEvent.DeityBorn.class)
				.hasAtLeastOneElementOfType(SimEvent.WorldChanged.class);
	}

	@Test
	void everyGodHasTwoOrThreeDifferentTraits() {
		for(long seed = 1; seed <= 50; seed++) {
			for(Snapshot.DeityView deity:runToEnd(seed, 40).deities()) {
				assertThat(deity.traits()).hasSizeBetween(2, 3).doesNotHaveDuplicates();
				assertThat(deity.traits().containsAll(List.of("aggressive", "pacifist"))).as(deity.name()).isFalse();
			}
		}
	}

	@Test
	void aPregnancyEndsInABirthWithBothParents() {
		Simulation simulation = new Simulation(11, 30);
		pantheon.gods.Deity mother = simulation.getListOfDeities().get(1);
		pantheon.gods.Deity father = simulation.getListOfDeities().get(0);
		mother.addStatus(new pantheon.status.Pregnant(mother, father, simulation));

		for(int i = 0; i < 12; i++) {
			simulation.step();
		}

		assertThat(simulation.snapshot().deities())
				.anySatisfy(child -> assertThat(child.parentIds()).containsExactly(mother.getId(), father.getId()));
	}

	@Test
	void poisonHurtsEachGenerationAndWearsOff() {
		Simulation simulation = new Simulation(2, 10);
		pantheon.gods.Deity victim = simulation.getListOfDeities().get(0);
		pantheon.gods.Deity poisoner = simulation.getListOfDeities().get(1);
		pantheon.status.Poisoned poison = new pantheon.status.Poisoned(victim, poisoner, simulation, 4, 2);
		int health = victim.getHealth();

		assertThat(poison.act()).isFalse();
		assertThat(poison.act()).isFalse();
		assertThat(poison.act()).isTrue();
		assertThat(victim.getHealth()).isEqualTo(health - 8);
		assertThat(victim.getLast_attacker()).isSameAs(poisoner);
	}

	@Test
	void childrenAreBornThroughPregnancy() {
		int born = 0;
		for(long seed = 1; seed <= 100; seed++) {
			for(Snapshot.DeityView deity:runToEnd(seed, 50).deities()) {
				if(!deity.parentIds().isEmpty()) {
					born++;
				}
			}
		}
		assertThat(born).isGreaterThan(300);
	}

	@Test
	void supremeDeitiesNeverDieAndStayWithinTheirRank() {
		int supremes = 0;
		for(long seed = 1; seed <= 150; seed++) {
			List<Snapshot.DeityView> found = runToEnd(seed, 100).deities().stream().filter(Snapshot.DeityView::supreme).toList();
			assertThat(found).as("seed %d", seed).hasSizeLessThanOrEqualTo(1);
			for(Snapshot.DeityView supreme:found) {
				assertThat(supreme.alive()).isTrue();
				assertThat(supreme.divineRank()).isBetween(Simulation.SUPREME_MIN_RANK, Simulation.SUPREME_MAX_RANK);
			}
			supremes += found.size();
		}
		assertThat(supremes).isGreaterThan(30);
	}

	@Test
	void aSupremeDeityCannotBeKilledOrMadeToFlee() {
		Simulation simulation = new Simulation(4, 10);
		pantheon.gods.Deity supreme = simulation.createSupremeDeity();
		supreme.changeHealth(-10_000);
		supreme.checkDeath();
		supreme.die("slain", null);
		supreme.fleeToVoid(SimEvent.DeityMoved.FLED_FIGHT);

		assertThat(supreme.isAlive()).isTrue();
		assertThat(supreme.getHealth()).isEqualTo(1);
		assertThat(supreme.getLocation().getName()).isEqualTo(pantheon.cosmos.Cosmos.VOID);
	}

	@Test
	void godsRarelyReturnToTheVoidWithoutAReason() {
		int ordinaryReturns = 0;
		int moves = 0;
		for(long seed = 1; seed <= 100; seed++) {
			Simulation simulation = new Simulation(seed, 80);
			List<SimEvent.DeityMoved> moved = new ArrayList<SimEvent.DeityMoved>();
			simulation.addListener(e -> {
				if(e instanceof SimEvent.DeityMoved m) moved.add(m);
			});
			while(!simulation.isFinished()) {
				simulation.step();
			}
			moves += moved.size();
			ordinaryReturns += moved.stream().filter(m -> m.to().equals(pantheon.cosmos.Cosmos.VOID) && m.reason() == null).count();
		}
		assertThat(ordinaryReturns).isLessThan(moves / 20);
	}

	@Test
	void crimeAboveTheLimitMakesAGodFleeToTheVoid() {
		Simulation simulation = new Simulation(6, 10);
		pantheon.gods.Deity criminal = simulation.getListOfDeities().get(0);
		pantheon.cosmos.Plane plane = simulation.getCosmos().startBuildingPlane(criminal);
		plane.construct(200);
		criminal.moveTo(plane);
		criminal.addCrime(80);
		List<SimEvent> events = new ArrayList<SimEvent>();
		simulation.addListener(events::add);

		boolean finished = new pantheon.behaviours.Crime(criminal, simulation).act();

		assertThat(finished).isTrue();
		assertThat(criminal.getLocation().getName()).isEqualTo(pantheon.cosmos.Cosmos.VOID);
		assertThat(criminal.getCrime()).isZero();
		assertThat(events).anySatisfy(e -> assertThat(e).isInstanceOfSatisfying(SimEvent.DeityMoved.class,
				m -> assertThat(m.reason()).isEqualTo(SimEvent.DeityMoved.FLED_CRIMES)));
	}

	@Test
	void sessionStepsUntilFinished() {
		SimulationSession session = new SimulationSession("test", 5, 3);
		session.step();
		session.step();
		SimulationSession.Info info = session.step();

		assertThat(info.finished()).isTrue();
		assertThat(info.generation()).isEqualTo(3);
		assertThat(session.snapshot().deities()).isNotEmpty();
	}
}
