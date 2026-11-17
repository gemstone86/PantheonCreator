package pantheon.sim;

import java.util.List;

/*
 * A plain-data copy of the whole simulation at one moment, safe to hand to other threads or serialize to JSON.
 */
public record Snapshot(
		long seed,
		int generation,
		int maxGenerations,
		boolean finished,
		List<DeityView> deities,
		List<PlaneView> planes,
		List<String> createdConcepts,
		List<String> domainPool) {

	public record DeityView(
			String id,
			String name,
			boolean alive,
			int sex,
			String gender,
			String sexuality,
			int divineRank,
			int health,
			int maxHealth,
			int age,
			int bornIn,
			Integer diedIn,
			String causeOfDeath,
			String killerId,
			List<String> domains,
			List<String> parentIds,
			List<String> mateIds,
			List<RelationView> relations) {}

	public record RelationView(String targetId, int value) {}

	public record PlaneView(
			String name,
			String creatorId,
			int progress,
			boolean done,
			List<String> concepts,
			List<String> builderIds) {}
}
