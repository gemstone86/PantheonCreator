/*
 * Pantheon Creator browser UI.
 * Creates a simulation through the REST API, listens to its event stream,
 * and draws the gods as a force-directed graph with D3.
 */
(() => {
	'use strict';

	const API = 'api/simulations';
	const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
	const $ = (id) => document.getElementById(id);

	const state = {
		simId: null,
		source: null,
		info: null,
		snapshot: null,
		nodes: new Map(),        // deity id -> graph node { id, view, x, y }
		names: new Map(),        // deity id -> name, including gods born mid-generation
		selectedId: null,
		allDomains: [],          // every domain seen so far, in a stable order
		creators: new Map(),     // domain -> name of the god who created it
		serverLost: false,
	};

	// ---------- small DOM helpers ----------

	function el(tag, attrs = {}, ...children) {
		const node = document.createElement(tag);
		for (const [key, value] of Object.entries(attrs)) {
			if (value === undefined || value === null || value === false) continue;
			if (key === 'class') node.className = value;
			else if (key.startsWith('on')) node.addEventListener(key.slice(2), value);
			else node.setAttribute(key, value === true ? '' : value);
		}
		for (const child of children.flat()) {
			if (child === null || child === undefined || child === false) continue;
			node.append(child instanceof Node ? child : document.createTextNode(String(child)));
		}
		return node;
	}

	const nameOf = (id) => state.names.get(id) || 'an unknown god';
	const godName = (id) => el('span', { class: 'name' }, nameOf(id));
	const sexClass = (sex) => (sex === 1 ? 'male' : sex === 3 ? 'female' : 'other');
	const radius = (d) => 5 + Math.sqrt(Math.max(d.view.divineRank, 0)) * 2.2;

	function listOfNames(ids) {
		const parts = [];
		ids.forEach((id, i) => {
			if (i > 0) parts.push(i === ids.length - 1 ? ' and ' : ', ');
			parts.push(godName(id));
		});
		return parts;
	}

	// ---------- the graph ----------

	const svg = d3.select('#graph');
	const zoomLayer = svg.append('g');
	const linkLayer = zoomLayer.append('g');
	const effectLayer = zoomLayer.append('g');
	const nodeLayer = zoomLayer.append('g');

	svg.call(d3.zoom().scaleExtent([0.2, 4]).on('zoom', (event) => zoomLayer.attr('transform', event.transform)));
	svg.on('click', (event) => {
		if (event.target === svg.node()) select(null);
	});

	const simulation = d3.forceSimulation()
		.force('charge', d3.forceManyBody().strength(-260))
		.force('link', d3.forceLink().id((d) => d.id))
		.force('collide', d3.forceCollide((d) => radius(d) + 14))
		.force('x', d3.forceX().strength(0.045))
		.force('y', d3.forceY().strength(0.045))
		.on('tick', ticked);

	const drag = d3.drag()
		.on('start', (event, d) => {
			if (!event.active) simulation.alphaTarget(0.3).restart();
			d.fx = d.x;
			d.fy = d.y;
		})
		.on('drag', (event, d) => {
			d.fx = event.x;
			d.fy = event.y;
		})
		.on('end', (event, d) => {
			if (!event.active) simulation.alphaTarget(0);
			d.fx = null;
			d.fy = null;
		});

	function fitViewBox() {
		const { width, height } = $('graph').getBoundingClientRect();
		svg.attr('viewBox', [-width / 2, -height / 2, width, height]);
	}
	new ResizeObserver(fitViewBox).observe(document.querySelector('.heavens'));
	fitViewBox();

	function buildLinks(visibleIds) {
		const links = [];
		const nodes = [...state.nodes.values()].filter((n) => visibleIds.has(n.id));
		if ($('show-family').checked) {
			for (const n of nodes) {
				for (const parentId of n.view.parentIds) {
					if (visibleIds.has(parentId)) links.push({ key: `f:${parentId}:${n.id}`, source: parentId, target: n.id, kind: 'family' });
				}
			}
		}
		if ($('show-mates').checked) {
			for (const n of nodes) {
				for (const mateId of n.view.mateIds) {
					if (n.id < mateId && visibleIds.has(mateId)) links.push({ key: `m:${n.id}:${mateId}`, source: n.id, target: mateId, kind: 'mate' });
				}
			}
		}
		if ($('show-feelings').checked) {
			for (const n of nodes) {
				for (const r of n.view.relations) {
					if (Math.abs(r.value) >= 10 && visibleIds.has(r.targetId)) {
						links.push({
							key: `r:${n.id}:${r.targetId}`, source: n.id, target: r.targetId,
							kind: r.value > 0 ? 'friendly' : 'hostile', value: r.value,
						});
					}
				}
			}
		}
		return links;
	}

	function render() {
		if (!state.snapshot) return;
		const showFallen = $('show-fallen').checked;
		const visible = [...state.nodes.values()].filter((n) => showFallen || n.view.alive);
		const visibleIds = new Set(visible.map((n) => n.id));
		const links = buildLinks(visibleIds);

		const linkSel = linkLayer.selectAll('line').data(links, (d) => d.key);
		linkSel.exit().remove();
		linkSel.enter().append('line')
			.merge(linkSel)
			.attr('class', (d) => `link ${d.kind}`)
			.attr('stroke-width', (d) => (d.value === undefined ? null : Math.min(0.6 + Math.abs(d.value) / 25, 3.5)))
			.attr('stroke-opacity', (d) => (d.value === undefined ? null : Math.min(0.3 + Math.abs(d.value) / 90, 0.9)));

		const nodeSel = nodeLayer.selectAll('g.node').data(visible, (d) => d.id);
		nodeSel.exit().remove();

		const entering = nodeSel.enter().append('g')
			.call(drag)
			.on('click', (event, d) => {
				event.stopPropagation();
				select(d.id);
			});
		entering.append('circle').attr('class', 'health-track');
		entering.append('circle').attr('class', 'health').attr('transform', 'rotate(-90)');
		entering.append('circle').attr('class', 'body').attr('r', reducedMotion ? radius : 0);
		entering.append('text');
		entering.append('title');
		if (!reducedMotion) {
			entering.select('.body').transition().duration(700).ease(d3.easeBackOut).attr('r', radius);
		}

		const all = entering.merge(nodeSel);
		all.attr('class', (d) => `node ${sexClass(d.view.sex)}${d.view.alive ? '' : ' dead'}${d.id === state.selectedId ? ' selected' : ''}`);
		nodeSel.select('.body').attr('r', radius);
		all.select('.health-track').attr('r', (d) => radius(d) + 4);
		all.select('.health')
			.attr('r', (d) => radius(d) + 4)
			.attr('stroke-dasharray', (d) => {
				const circumference = 2 * Math.PI * (radius(d) + 4);
				const fraction = Math.max(0, Math.min(1, d.view.health / Math.max(d.view.maxHealth, 1)));
				return `${circumference * fraction} ${circumference}`;
			});
		all.select('text').attr('y', (d) => radius(d) + 17).text((d) => d.view.name);
		all.select('title').text((d) => `${d.view.name}, divine rank ${d.view.divineRank}${d.view.alive ? '' : ' (fallen)'}`);

		simulation.nodes(visible);
		simulation.force('link')
			.links(links)
			.distance((l) => (l.kind === 'family' ? 60 : l.kind === 'mate' ? 45 : 150))
			.strength((l) => (l.kind === 'family' ? 0.5 : l.kind === 'mate' ? 0.6 : 0.03));
		simulation.alpha(0.5).restart();
	}

	function ticked() {
		linkLayer.selectAll('line')
			.attr('x1', (d) => d.source.x)
			.attr('y1', (d) => d.source.y)
			.attr('x2', (d) => d.target.x)
			.attr('y2', (d) => d.target.y);
		nodeLayer.selectAll('g.node').attr('transform', (d) => `translate(${d.x},${d.y})`);
	}

	function flashFight(attackerId, defenderId) {
		const a = state.nodes.get(attackerId);
		const b = state.nodes.get(defenderId);
		if (reducedMotion || !a || !b || a.x === undefined || b.x === undefined) return;
		effectLayer.append('line')
			.attr('class', 'flash')
			.attr('x1', a.x).attr('y1', a.y).attr('x2', b.x).attr('y2', b.y)
			.transition().duration(1100).ease(d3.easeCubicIn)
			.attr('stroke-opacity', 0)
			.remove();
	}

	// ---------- applying state ----------

	function applySnapshot(snapshot) {
		state.snapshot = snapshot;
		const seen = new Set();
		for (const view of snapshot.deities) {
			state.names.set(view.id, view.name);
			let node = state.nodes.get(view.id);
			if (!node) {
				// newborns appear next to a parent, so they visibly come from them
				const parent = view.parentIds.map((id) => state.nodes.get(id)).find((p) => p && p.x !== undefined);
				const spread = parent ? 24 : 240;
				node = {
					id: view.id,
					x: (parent ? parent.x : 0) + (Math.random() - 0.5) * spread,
					y: (parent ? parent.y : 0) + (Math.random() - 0.5) * spread,
				};
				state.nodes.set(view.id, node);
			}
			node.view = view;
			seen.add(view.id);
		}
		for (const id of [...state.nodes.keys()]) {
			if (!seen.has(id)) state.nodes.delete(id);
		}
		for (const domain of [...snapshot.domainPool, ...snapshot.createdConcepts]) {
			if (!state.allDomains.includes(domain)) state.allDomains.push(domain);
		}

		render();
		renderPlanes();
		renderDomains();
		renderDetails();
		renderGeneration();
	}

	function renderGeneration() {
		const s = state.snapshot;
		if (!s) return;
		const text = s.finished
			? `Finished at generation ${s.generation}`
			: `Generation ${s.generation} of ${s.maxGenerations}`;
		$('generation').replaceChildren(text, el('small', {}, `Seed ${s.seed}`));
	}

	function renderPlanes() {
		const planes = state.snapshot.planes;
		const container = $('planes');
		if (planes.length === 0) {
			container.replaceChildren(el('p', { class: 'empty' }, 'No god has started shaping a plane yet.'));
			return;
		}
		container.replaceChildren(...planes.map((plane) => el('article', { class: `plane${plane.done ? ' done' : ''}` },
			el('h3', {}, plane.name, el('small', {}, plane.done ? 'Complete' : `${plane.progress}%`)),
			el('div', { class: 'progress', role: 'progressbar', 'aria-valuenow': plane.progress, 'aria-valuemin': 0, 'aria-valuemax': 100, 'aria-label': `${plane.name} progress` },
				el('span', { style: `width:${plane.progress}%` })),
			el('p', {}, plane.done ? 'Shaped by ' : 'Being shaped by ', ...listOfNames(plane.builderIds)),
			plane.concepts.length ? el('ul', { class: 'chips' }, plane.concepts.map((c) => el('li', {}, c))) : null,
		)));
	}

	function renderDomains() {
		const s = state.snapshot;
		const created = new Set(s.createdConcepts);
		const pool = new Set(s.domainPool);
		$('domains').replaceChildren(...state.allDomains.map((domain) => {
			const status = created.has(domain) ? 'created' : pool.has(domain) ? '' : 'shaping';
			const title = status === 'created'
				? `Created by ${state.creators.get(domain) || 'a god'}`
				: status === 'shaping' ? 'A god is trying to create this' : 'Not yet created';
			return el('li', { class: status, title }, domain);
		}));
	}

	function select(id) {
		state.selectedId = id;
		nodeLayer.selectAll('g.node').classed('selected', (d) => d.id === id);
		renderDetails();
	}

	function renderDetails() {
		const panel = $('details');
		const view = state.selectedId && state.nodes.get(state.selectedId)?.view;
		if (!view) {
			panel.replaceChildren(el('p', { class: 'empty' }, 'Click a god in the graph to see their domains, family and feelings.'));
			return;
		}
		const nameButtons = (ids) => (ids.length === 0 ? 'None' : ids.flatMap((id, i) => [
			i > 0 ? ', ' : null,
			el('button', { type: 'button', class: 'link-button', onclick: () => select(id) }, nameOf(id)),
		]));
		const children = state.snapshot.deities.filter((d) => d.parentIds.includes(view.id)).map((d) => d.id);
		const feelings = [...view.relations]
			.sort((a, b) => Math.abs(b.value) - Math.abs(a.value))
			.slice(0, 6)
			.map((r) => el('li', {}, `${r.value >= 0 ? 'Likes' : 'Dislikes'} ${nameOf(r.targetId)} (${r.value > 0 ? '+' : ''}${r.value})`));

		let fate = null;
		if (!view.alive) {
			fate = view.causeOfDeath === 'slain'
				? `Slain in generation ${view.diedIn}${view.killerId ? ` by ${nameOf(view.killerId)}` : ''}`
				: `Died of old age in generation ${view.diedIn}`;
		}

		panel.replaceChildren(
			el('h2', {}, view.name),
			el('p', { class: 'who' }, `${view.gender}, ${view.sexuality.toLowerCase()}`),
			el('dl', {},
				el('dt', {}, 'Divine rank'), el('dd', {}, view.divineRank),
				el('dt', {}, view.alive ? 'Health' : 'Fate'), el('dd', {}, view.alive
					? [`${Math.max(view.health, 0)} of ${view.maxHealth}`,
						el('div', { class: 'health-bar' }, el('span', { style: `width:${Math.max(0, Math.min(100, (100 * view.health) / view.maxHealth))}%` }))]
					: fate),
				el('dt', {}, 'Age'), el('dd', {}, view.age),
				el('dt', {}, 'Born'), el('dd', {}, view.parentIds.length === 0 && view.bornIn === 0 ? 'One of the first gods' : `Generation ${view.bornIn}`),
				el('dt', {}, 'Parents'), el('dd', {}, view.parentIds.length ? nameButtons(view.parentIds) : 'None'),
				el('dt', {}, 'Mates'), el('dd', {}, nameButtons(view.mateIds)),
				el('dt', {}, 'Children'), el('dd', {}, nameButtons(children)),
				el('dt', {}, 'Domains'), el('dd', {}, el('ul', { class: 'chips' }, view.domains.map((d) => el('li', {}, d)))),
				el('dt', {}, 'Feelings'), el('dd', {}, feelings.length ? el('ul', { class: 'chips' }, feelings) : 'Knows no one yet'),
			),
		);
	}

	// ---------- chronicle ----------

	const chronicle = $('chronicle');
	const MAX_ENTRIES = 800;

	// Follow new entries unless the reader scrolls up to read older ones.
	// Only an upward scroll stops following: panels resizing around the chronicle must not.
	let followChronicle = true;
	let lastScrollTop = 0;
	chronicle.addEventListener('scroll', () => {
		const atBottom = chronicle.scrollHeight - chronicle.scrollTop - chronicle.clientHeight < 40;
		if (atBottom) followChronicle = true;
		else if (chronicle.scrollTop < lastScrollTop - 4) followChronicle = false;
		lastScrollTop = chronicle.scrollTop;
	});

	// a generation's heading is only written once something happens in it
	let pendingGeneration = null;

	function chronicleEntry(cls, ...parts) {
		chronicle.querySelector('.empty')?.remove();
		if (pendingGeneration !== null) {
			const generation = pendingGeneration;
			pendingGeneration = null;
			chronicleEntry('gen', `Generation ${generation}`);
		}
		chronicle.append(el('li', { class: cls }, ...parts));
		while (chronicle.children.length > MAX_ENTRIES) chronicle.firstElementChild.remove();
		if (followChronicle) {
			chronicle.scrollTop = chronicle.scrollHeight;
			lastScrollTop = chronicle.scrollTop;
		}
	}

	function resetChronicle() {
		followChronicle = true;
		lastScrollTop = 0;
		pendingGeneration = null;
		chronicle.replaceChildren(el('li', { class: 'empty' }, 'Press Play to let the gods act, or play one generation at a time.'));
	}

	const handlers = {
		Info: (info) => updateInfo(info),
		Snapshot: (snapshot) => applySnapshot(snapshot),
		GenerationStarted: (e) => {
			pendingGeneration = e.generation;
		},
		GenerationEnded: (e) => {
			applySnapshot(e.snapshot);
			if (e.snapshot.finished) {
				pendingGeneration = null;
				chronicleEntry('gen', `The simulation has finished after ${e.generation} generations`);
			}
		},
		Narration: (e) => {
			// depth 0 lines are the console's status lines, which the graph already shows
			if (!$('show-thoughts').checked || !e.deityId || e.depth === 0) return;
			chronicleEntry('thought', godName(e.deityId), `: ${e.text}`);
		},
		DeityBorn: (e) => {
			state.names.set(e.deity.id, e.deity.name);
			if (e.deity.parentIds.length === 0) chronicleEntry('', godName(e.deity.id), ' appeared in the empty world.');
			else chronicleEntry('', godName(e.deity.id), ' was born to ', ...listOfNames(e.deity.parentIds), '.');
		},
		DeityDied: (e) => {
			if (e.cause === 'slain' && e.killerId) chronicleEntry('death', godName(e.deityId), ' was slain by ', godName(e.killerId), '.');
			else if (e.cause === 'slain') chronicleEntry('death', godName(e.deityId), ' was slain.');
			else chronicleEntry('death', godName(e.deityId), ' died of old age.');
		},
		MateFound: (e) => chronicleEntry('', godName(e.deityId), ' and ', godName(e.mateId), ' became mates.'),
		RivalryDeclared: (e) => chronicleEntry('fight', godName(e.deityId), ' swore enmity against ', godName(e.rivalId), '.'),
		FightResolved: (e) => {
			flashFight(e.attackerId, e.defenderId);
			const won = e.winnerId === e.attackerId;
			chronicleEntry('fight', godName(e.attackerId), ' attacked ', godName(e.defenderId), won ? ' and won.' : ' and lost.');
		},
		ConceptCreated: (e) => {
			state.creators.set(e.concept, nameOf(e.deityId));
			chronicleEntry('', godName(e.deityId), ` created ${e.concept}.`);
		},
		PlaneStarted: (e) => chronicleEntry('', godName(e.creatorId), ` began shaping the plane ${e.plane}.`),
		PlaneFinished: (e) => chronicleEntry('', `The plane ${e.plane} was completed by `, godName(e.finisherId), '.'),
		Error: (message) => chronicleEntry('error', message),
	};

	// ---------- talking to the server ----------

	function connect(id) {
		const source = new EventSource(`${API}/${id}/events`);
		for (const [type, handler] of Object.entries(handlers)) {
			source.addEventListener(type, (event) => handler(JSON.parse(event.data)));
		}
		source.onopen = () => {
			state.serverLost = false;
		};
		source.onerror = () => {
			if (!state.serverLost && source.readyState !== EventSource.OPEN) {
				state.serverLost = true;
				chronicleEntry('error', 'Lost the connection to the server. Check that run.ps1 is still running; the page reconnects by itself.');
			}
		};
		state.source = source;
	}

	async function call(method, path, body) {
		try {
			const response = await fetch(path, {
				method,
				headers: body ? { 'Content-Type': 'application/json' } : undefined,
				body: body ? JSON.stringify(body) : undefined,
			});
			if (!response.ok) {
				const problem = await response.json().catch(() => ({}));
				chronicleEntry('error', `The server refused that: ${problem.detail || problem.error || response.status}.`);
				return null;
			}
			return response.status === 204 ? {} : await response.json();
		} catch (error) {
			chronicleEntry('error', 'Couldn’t reach the server. Check that run.ps1 is still running.');
			return null;
		}
	}

	async function newPantheon(seed, generations) {
		if (state.source) state.source.close();
		if (state.simId) call('DELETE', `${API}/${state.simId}`);
		Object.assign(state, { simId: null, info: null, snapshot: null, selectedId: null, serverLost: false });
		state.nodes.clear();
		state.names.clear();
		state.creators.clear();
		state.allDomains = [];
		simulation.nodes([]);
		nodeLayer.selectAll('*').remove();
		linkLayer.selectAll('*').remove();
		resetChronicle();

		const info = await call('POST', API, { seed: seed || null, generations });
		if (!info) return;
		state.simId = info.id;
		updateInfo(info);
		history.replaceState(null, '', `?seed=${info.seed}&generations=${info.maxGenerations}`);
		connect(info.id);
	}

	function updateInfo(info) {
		state.info = info;
		const play = $('play');
		play.textContent = info.playing ? 'Pause' : 'Play';
		play.disabled = info.finished;
		$('step').disabled = info.finished || info.playing;
	}

	// ---------- controls ----------

	$('new-form').addEventListener('submit', (event) => {
		event.preventDefault();
		const seed = $('seed').value ? Number($('seed').value) : null;
		const generations = Number($('generations').value) || 50;
		newPantheon(seed, generations);
	});

	$('play').addEventListener('click', async () => {
		if (!state.simId) return;
		const info = state.info?.playing
			? await call('POST', `${API}/${state.simId}/pause`)
			: await call('POST', `${API}/${state.simId}/play?msPerGeneration=${$('speed').value}`);
		if (info) updateInfo(info);
	});

	$('step').addEventListener('click', async () => {
		if (!state.simId) return;
		const info = await call('POST', `${API}/${state.simId}/step`);
		if (info) updateInfo(info);
	});

	$('speed').addEventListener('change', async () => {
		if (!state.simId || !state.info?.playing) return;
		const info = await call('POST', `${API}/${state.simId}/play?msPerGeneration=${$('speed').value}`);
		if (info) updateInfo(info);
	});

	for (const id of ['show-family', 'show-mates', 'show-feelings', 'show-fallen']) {
		$(id).addEventListener('change', render);
	}

	window.addEventListener('beforeunload', () => {
		if (state.simId) navigator.sendBeacon?.(`${API}/${state.simId}/pause`);
	});

	// ---------- start ----------

	const params = new URLSearchParams(location.search);
	if (params.get('generations')) $('generations').value = params.get('generations');
	newPantheon(params.get('seed') ? Number(params.get('seed')) : null, Number($('generations').value) || 50);
})();
