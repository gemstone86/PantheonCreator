package pantheon.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import pantheon.sim.Snapshot;

/*
 * REST API for simulations, plus a Server-Sent Events stream of everything that happens.
 */
@RestController
@RequestMapping("/api/simulations")
public class SimulationController {

	/* Both fields are optional: a random seed and 50 generations are used if left out. */
	public record NewSimulation(Long seed, Integer generations) {}

	private final SimulationService service;

	public SimulationController(SimulationService service) {
		this.service = service;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public SimulationSession.Info create(@RequestBody(required = false) NewSimulation request) {
		NewSimulation req = request != null ? request : new NewSimulation(null, null);
		return service.create(req.seed(), req.generations()).info();
	}

	@GetMapping("/{id}")
	public SimulationSession.Info info(@PathVariable String id) {
		return service.get(id).info();
	}

	@GetMapping("/{id}/state")
	public Snapshot state(@PathVariable String id) {
		return service.get(id).snapshot();
	}

	@PostMapping("/{id}/step")
	public SimulationSession.Info step(@PathVariable String id) {
		return service.get(id).step();
	}

	@PostMapping("/{id}/play")
	public SimulationSession.Info play(@PathVariable String id, @RequestParam(defaultValue = "1000") long msPerGeneration) {
		return service.play(id, msPerGeneration);
	}

	@PostMapping("/{id}/pause")
	public SimulationSession.Info pause(@PathVariable String id) {
		return service.get(id).pause();
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable String id) {
		service.delete(id);
	}

	@GetMapping(path = "/{id}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter events(@PathVariable String id) {
		return service.get(id).subscribe();
	}
}
