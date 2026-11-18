package pantheon.artifacts;

import pantheon.gods.Deity;
import pantheon.sim.Snapshot;

/*
 * An object of power crafted by a deity. Its power is the divine rank invested in it,
 * kept in tenths so that investments of 0.5 to 1 point add up exactly.
 */
public class Artifact {

	private final String name;
	private final ArtifactType type;
	private final Deity creator;
	private final int startedIn;
	private Integer finishedIn;
	private int powerTenths = 0;
	private boolean done = false;

	public Artifact(String name, ArtifactType type, Deity creator, int startedIn) {
		this.name = name;
		this.type = type;
		this.creator = creator;
		this.startedIn = startedIn;
	}

	public void invest(int tenths) {
		powerTenths += tenths;
	}

	public void finish(int generation) {
		done = true;
		finishedIn = generation;
	}

	/* Divine rank the creator loses on finishing it: half the power invested, rounded up. */
	public int rankCost() {
		return (powerTenths + 19) / 20;
	}

	public double getPower() {
		return powerTenths / 10.0;
	}

	public int getPowerTenths() {
		return powerTenths;
	}

	public String getName() {
		return name;
	}

	public ArtifactType getType() {
		return type;
	}

	public Deity getCreator() {
		return creator;
	}

	public boolean isDone() {
		return done;
	}

	public Snapshot.ArtifactView toView() {
		return new Snapshot.ArtifactView(name, type.getLabel(), getPower(), creator.getId(), done, startedIn, finishedIn);
	}
}
