package traits;

import behaviours.*;
import gods.*;
import main.runtime;

public class trait {

	private deity owner;
//	private runtime context;
	
	public boolean act() {
		return true;
	}
	
//	public runtime getContext() {
//		return context;
//	}
	
//	public void setContext(runtime context) {
//		this.context = context;
//	}
//	
	public deity getOwner() {
		return owner;
	}
	
	public behaviour motivate() {
		return null;
	}
}
