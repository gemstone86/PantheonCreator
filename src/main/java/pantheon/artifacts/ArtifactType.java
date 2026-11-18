package pantheon.artifacts;

public enum ArtifactType {
	RING("Ring"),
	SWORD("Sword"),
	SHIELD("Shield"),
	ARMOR("Armor"),
	STAFF("Staff"),
	BOOK("Book"),
	ORB("Orb"),
	OTHER("Other");

	private final String label;

	ArtifactType(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	/* For sentences: "a sword", "an orb", "a relic". */
	public String withArticle() {
		String word = this == OTHER ? "relic" : label.toLowerCase();
		return ("aeiou".indexOf(word.charAt(0)) >= 0 ? "an " : "a ") + word;
	}
}
