package pantheon.web;

import java.util.Comparator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.annotation.PreDestroy;

/*
 * Keeps the running simulations in memory. The oldest ones are dropped when there are too many.
 */
@Service
public class SimulationService {

	static final int MAX_SESSIONS = 20;
	static final int MAX_GENERATIONS = 1000;

	private final Map<String, SimulationSession> sessions = new ConcurrentHashMap<String, SimulationSession>();
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

	public SimulationSession create(Long seed, Integer generations) {
		long actualSeed = seed != null ? seed : ThreadLocalRandom.current().nextLong(1, 1_000_000);
		int actualGenerations = generations != null ? generations : 50;
		if(actualGenerations < 1 || actualGenerations > MAX_GENERATIONS) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "generations must be between 1 and " + MAX_GENERATIONS);
		}
		evictOldSessions();
		SimulationSession session = new SimulationSession(UUID.randomUUID().toString(), actualSeed, actualGenerations);
		sessions.put(session.getId(), session);
		return session;
	}

	public SimulationSession get(String id) {
		SimulationSession session = sessions.get(id);
		if(session == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No simulation with id " + id);
		}
		return session;
	}

	public SimulationSession.Info play(String id, long msPerGeneration) {
		if(msPerGeneration < 50 || msPerGeneration > 10_000) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "msPerGeneration must be between 50 and 10000");
		}
		return get(id).play(scheduler, msPerGeneration);
	}

	public void delete(String id) {
		SimulationSession session = sessions.remove(id);
		if(session != null) {
			session.close();
		}
	}

	private void evictOldSessions() {
		while(sessions.size() >= MAX_SESSIONS) {
			sessions.values().stream()
					.min(Comparator.comparingLong(SimulationSession::getCreatedAt))
					.ifPresent(oldest -> delete(oldest.getId()));
		}
	}

	@PreDestroy
	void shutdown() {
		sessions.values().forEach(SimulationSession::close);
		scheduler.shutdownNow();
	}
}
