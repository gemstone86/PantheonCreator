package behaviours;

import gods.deity;
import gods.relation;
import main.runtime;

public class grow extends behaviour{

	private deity owner;
	private runtime context;
	private boolean end = false;
	
	public grow(deity owner, runtime context) {
		this.owner = owner;
		this.context = context;
	}

	@Override
	public boolean act() {
		int random = context.getRandom(1, 100);
		int moreRandom = context.getRandom(1, 100);
		
		if(random < 20) {
			System.out.println("\t\tI have become more powerful...");
			owner.setDvr(owner.getDvR()+1);
			
			if(moreRandom < 20) {
				System.out.println("\t\t... and I want to test my strength!");
				owner.addBehaviour(new findRival(owner, context));
			}
			else if(moreRandom > 19 && moreRandom <81) {
				System.out.println("\t\t...and that is good");
			}
			else{
				System.out.println("\t\t...and I shall use my new strength!");
				owner.addBehaviour(new findMotivation(owner, context));
			}
			
			return true;
		}
		else if(random >19 && random < 81) {
			System.out.println("\t\tI need to grow more powerful!");
			return false;
		}
		else {
			System.out.println("\t\tI can't grow more powerful...");
			return true;
		}
	}
}