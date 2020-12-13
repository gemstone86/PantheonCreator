
package behaviours;

import gods.*;
import main.runtime;

public abstract class behaviour {

	private deity owner;
	
	abstract public boolean act(runtime runtime);
	
}
