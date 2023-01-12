package traits;

import behaviours.*;
import gods.*;
import main.runtime;

public abstract class trait {

	private deity owner;

	public deity getOwner() {
		return owner;
	}
	
	public abstract void motivate();
	
	public abstract boolean removeTrait();

	public abstract String toString();
}
