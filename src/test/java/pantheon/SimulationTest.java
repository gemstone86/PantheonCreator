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
