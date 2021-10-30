package behaviours;

import gods.deity;
import main.runtime;

public class findRival extends behaviour{

	private deity owner;
	private runtime context;

	public findRival(deity deity, runtime context) {
		owner = deity;
		this.context = context;
	}

	@Override
	public boolean act() {
		return true;
	}
}