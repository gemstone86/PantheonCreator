package pantheon.sim;

/*
 * Everything that happens in a simulation is reported as one of these events.
 * The console prints them as text; the web app streams them to the browser.
 */
public sealed interface SimEvent {

	int generation();

	/* A generation is about to be played. */
	record GenerationStarted(int generation, int activeDeities) implements SimEvent {}

	/* A generation has been played; carries the full state afterwards. */
	record GenerationEnded(int generation, Snapshot snapshot) implements SimEvent {}

	/* Free-text commentary, e.g. "I want to find a mate". deityId is null for world-level messages. depth is used for indentation. */
	record Narration(int generation, String deityId, int depth, String text) implements SimEvent {}

	record DeityBorn(int generation, Snapshot.DeityView deity) implements SimEvent {}

	/* cause is "old age" or "slain"; killerId is null unless slain by another deity. */
	record DeityDied(int generation, String deityId, String cause, String killerId) implements SimEvent {}

	record MateFound(int generation, String deityId, String mateId) implements SimEvent {}

	record RivalryDeclared(int generation, String deityId, String rivalId) implements SimEvent {}

	record FightResolved(int generation, String attackerId, String defenderId, String winnerId,
			int damageToWinner, int damageToLoser) implements SimEvent {}

	record ConceptCreated(int generation, String deityId, String concept) implements SimEvent {}

	record PlaneStarted(int generation, String plane, String creatorId) implements SimEvent {}

	record PlaneFinished(int generation, String plane, String finisherId) implements SimEvent {}

	/* A deity became pregnant. partnerId is the other parent, or the deity itself for a child conceived alone. */
	record Conceived(int generation, String deityId, String partnerId) implements SimEvent {}

	/* A deity was poisoned by another. */
	record Poisoned(int generation, String deityId, String poisonerId) implements SimEvent {}

	/*
	 * A deity travelled from one place to another. Places are plane names or Cosmos.VOID.
	 * reason is null for an ordinary journey, or FLED_FIGHT / FLED_CRIMES when fleeing to the Void.
	 */
	record DeityMoved(int generation, String deityId, String from, String to, String reason) implements SimEvent {
		public static final String FLED_FIGHT = "fled a fight";
		public static final String FLED_CRIMES = "fled their crimes";
	}

	/* A deity's crime score went up. */
	record CrimeCommitted(int generation, String deityId, int crime) implements SimEvent {}

	/* A deity opened a path between two places. */
	record PlacesConnected(int generation, String deityId, String placeA, String placeB) implements SimEvent {}

	/* The world was changed from outside the normal flow of generations, e.g. a god was added. */
	record WorldChanged(int generation, Snapshot snapshot) implements SimEvent {}
}
