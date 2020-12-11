
package behaviours;

import java.util.LinkedList;

import gods.*;
import main.runtime;

public abstract class behaviour {

	private deity owner;
	
	abstract public void act(runtime runtime);
	
}
