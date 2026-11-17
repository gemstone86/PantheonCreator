package pantheon.web;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import pantheon.sim.SimEvent;
import pantheon.sim.Simulation;
import pantheon.sim.Snapshot;

/*
 * One running simulation plus the browsers watching it.
 * All access to the simulation goes through synchronized methods, because Simulation is not thread-safe.
 */
public class SimulationSession {

	/* Sent to the browser whenever play/pause state or speed changes. */
	public record Info(String id, long seed, int generation, int maxGenerations, boolean finished, boolean playing, long msPerGeneration) {}

	private final String id;
	private final Simulation simulation;
	private final long createdAt = System.currentTimeMillis();
	private final List<SseEmitter> emitters = new CopyOnWriteArrayList<SseEmitter>();
	private ScheduledFuture<?> player;
	//changes whenever playing starts or stops, so a tick that was already queued can tell it is stale
	private long playToken = 0;
	private long msPerGeneration = 1000;

	public SimulationSession(String id, long seed, int maxGenerations) {
		this.id = id;
		this.simulation = new Simulation(seed, maxGenerations);
		this.simulation.addListener(this::broadcast);
	}

	public String getId() {
		return id;
	}

	public long getCreatedAt() {
		return createdAt;
	}

	public synchronized Info info() {
		return new Info(id, simulation.getSeed(), simulation.getGeneration(), simulation.getMaxGenerations(),
				simulation.isFinished(), isPlaying(), msPerGeneration);
	}

	public synchronized Snapshot snapshot() {
		return simulation.snapshot();
	}

	public synchronized Info step() {
		simulation.step();
		if(simulation.isFinished()) {
			stopPlayer();
		}
		return info();
	}

	public synchronized Info play(ScheduledExecutorService scheduler, long msPerGeneration) {
		this.msPerGeneration = msPerGeneration;
		stopPlayer();
		if(!simulation.isFinished()) {
			long token = playToken;
			player = scheduler.scheduleWithFixedDelay(() -> playOneGeneration(token), 0, msPerGeneration, TimeUnit.MILLISECONDS);
		}
		Info info = info();
		send("Info", info);
		return info;
	}

	public synchronized Info pause() {
		stopPlayer();
		Info info = info();
		send("Info", info);
		return info;
	}

	private synchronized void playOneGeneration(long token) {
		if(token != playToken) {
			return;
		}
		try {
			step();
			if(simulation.isFinished()) {
				send("Info", info());
			}
		}
		catch(RuntimeException e) {
			stopPlayer();
			send("Error", "The simulation crashed (seed " + simulation.getSeed() + "): " + e);
		}
	}

	private boolean isPlaying() {
		return player != null && !player.isDone();
	}

	private void stopPlayer() {
		playToken++;
		if(player != null) {
			player.cancel(false);
			player = null;
		}
	}

	/*
	 * Registers a browser for live events, starting with the current info and state.
	 */
	public synchronized SseEmitter subscribe() {
		SseEmitter emitter = new SseEmitter(0L);
		emitter.onCompletion(() -> emitters.remove(emitter));
		emitter.onTimeout(() -> emitters.remove(emitter));
		emitter.onError(e -> emitters.remove(emitter));
		emitters.add(emitter);
		sendTo(emitter, "Info", info());
		sendTo(emitter, "Snapshot", simulation.snapshot());
		return emitter;
	}

	public synchronized void close() {
		stopPlayer();
		for(SseEmitter emitter:emitters) {
			emitter.complete();
		}
		emitters.clear();
	}

	private void broadcast(SimEvent event) {
		send(event.getClass().getSimpleName(), event);
	}

	private void send(String name, Object data) {
		for(SseEmitter emitter:emitters) {
			sendTo(emitter, name, data);
		}
	}

	private void sendTo(SseEmitter emitter, String name, Object data) {
		try {
			emitter.send(SseEmitter.event().name(name).data(data, MediaType.APPLICATION_JSON));
		}
		catch(IOException | IllegalStateException e) {
			emitters.remove(emitter);
		}
	}
}
