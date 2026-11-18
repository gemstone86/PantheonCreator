package pantheon.behaviours;

import pantheon.artifacts.Artifact;
import pantheon.gods.Deity;
import pantheon.sim.SimEvent;
import pantheon.sim.Simulation;

/*
 * Crafts an artifact. Each generation the god invests 0.5 to 1 point of divine rank into it,
 * and after each investment there is a DONE_CHANCE percent chance that it is finished.
 * When it is finished the god loses half the power invested, rounded up.
 */
public class CraftArtifact extends Behaviour {

	public static final int DONE_CHANCE = 25;

	private Artifact artifact;

	public CraftArtifact(Deity owner, Simulation context) {
		super(owner, context);
	}

	@Override
	public boolean act() {
		if(artifact == null) {
			artifact = context.getCosmos().startArtifact(owner);
			say("I will craft " + artifact.getName() + ", " + artifact.getType().withArticle());
			context.emit(new SimEvent.ArtifactStarted(context.getGeneration(), owner.getId(), artifact.getName(), artifact.getType().getLabel()));
		}

		int investment = context.getRandom(5, 11);
		artifact.invest(investment);
		say("I pour " + (investment / 10.0) + " of my power into " + artifact.getName() + " (now " + artifact.getPower() + ")");

		if(context.getRandom(0, 100) < DONE_CHANCE) {
			int cost = artifact.rankCost();
			artifact.finish(context.getGeneration());
			owner.setDvr(Math.max(0, owner.getDvR() - cost));
			say("I have finished " + artifact.getName() + "! It cost me " + cost + " divine rank");
			context.emit(new SimEvent.ArtifactCrafted(context.getGeneration(), owner.getId(), artifact.getName(),
					artifact.getType().getLabel(), artifact.getPower(), cost));
			return true;
		}
		return false;
	}

	public Artifact getArtifact() {
		return artifact;
	}
}
