package pantheon.sim;

import java.io.PrintStream;
import java.util.function.Consumer;

/*
 * Prints simulation events as text, like the original command-line program.
 */
public class ConsoleListener implements Consumer<SimEvent> {

	private final PrintStream out;

	public ConsoleListener(PrintStream out) {
		this.out = out;
	}

	@Override
	public void accept(SimEvent event) {
		if(event instanceof SimEvent.GenerationStarted e) {
			out.println("----Generation " + e.generation() + " (" + e.activeDeities() + ")----");
		}
		else if(event instanceof SimEvent.Narration e) {
			out.println(e.deityId() == null ? e.text() : "\t".repeat(e.depth() + 1) + e.text());
		}
		else if(event instanceof SimEvent.GenerationEnded e) {
			out.println("Current domains: " + e.snapshot().domainPool());
		}
		//the other, structured events are already described by the narration
	}
}
