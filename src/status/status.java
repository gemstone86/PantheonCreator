
package status;

import gods.*;
import main.runtime;

public class status {

	private deity owner;
	private runtime context;
	private boolean end;
	
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
	
	public boolean resolve() {
		return true;
	}
}
