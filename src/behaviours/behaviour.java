
package behaviours;

import gods.*;
import main.runtime;

public class behaviour {

	private deity owner;
	protected runtime context;
	
	public boolean act() {
		return true;
	}
	
	public runtime getContext() {
		return context;
	}
	
	public void setContext(runtime context) {
		this.context = context;
	}
	
	public deity getOwner() {
		return owner;
	}
}
