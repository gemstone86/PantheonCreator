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
	const regionLayer = zoomLayer.append('g');
	const pathLayer = zoomLayer.append('g');
	const linkLayer = zoomLayer.append('g');
	const effectLayer = zoomLayer.append('g');
	const nodeLayer = zoomLayer.append('g');

	// the view follows the planes as they appear, until the user pans or zooms themselves
	let userZoomed = false;
	let lastFitKey = '';
	let currentScale = 1;
	const zoom = d3.zoom().scaleExtent([0.1, 4]).on('zoom', (event) => {
		zoomLayer.attr('transform', event.transform);
		if (event.sourceEvent) userZoomed = true;
		currentScale = event.transform.k;
		applyLabelScale();
	});

	// keep names readable when zoomed out to see many planes
	function applyLabelScale() {
		const grow = Math.min(2.4, Math.max(1, 1 / currentScale));
		nodeLayer.selectAll('g.node text').style('font-size', `${12 * grow}px`)
			.attr('y', (d) => radius(d) + 6 + 11 * grow);
		regionLayer.selectAll('g.region text').style('font-size', `${16 * grow}px`)
			.attr('y', (d) => -d.r - 6 - 6 * grow);
	}
	svg.call(zoom);

	// ---------- places: the Void in the middle, planes on a spiral around it ----------

	const VOID = 'The Void';
	const GOLDEN_ANGLE = Math.PI * (3 - Math.sqrt(5));
	let places = new Map();   // place name -> { name, x, y, r, plane }

	function planeAnchor(index) {
		// each plane keeps its spot as more are added
		const distance = 270 + 100 * Math.sqrt(index);
		const angle = index * GOLDEN_ANGLE - Math.PI / 2;
		return { x: distance * Math.cos(angle), y: distance * Math.sin(angle) };
	}

	function buildPlaces() {
		const living = new Map();
		for (const n of state.nodes.values()) {
			if (n.view.alive) living.set(n.view.location, (living.get(n.view.location) || 0) + 1);
		}
		const regionRadius = (name) => 70 + 28 * Math.sqrt(living.get(name) || 0);
		places = new Map();
		places.set(VOID, { name: VOID, x: 0, y: 0, r: regionRadius(VOID) + 20, plane: null });
		state.snapshot.planes.forEach((plane, i) => {
			places.set(plane.name, { name: plane.name, ...planeAnchor(i), r: regionRadius(plane.name), plane });
		});
	}

	const anchorOf = (d) => places.get(d.view.location) || places.get(VOID);

	// pushes any god that has strayed outside its place back inside the circle
	function containWithinPlace() {
		let nodes = [];
		const force = (alpha) => {
			for (const d of nodes) {
				const place = anchorOf(d);
				if (!place) continue;
				const dx = d.x - place.x;
				const dy = d.y - place.y;
				const distance = Math.hypot(dx, dy) || 1;
				const limit = Math.max(place.r - radius(d) - 10, 8);
				if (distance > limit) {
					const k = ((distance - limit) / distance) * Math.min(1, alpha * 4);
					d.vx -= dx * k;
					d.vy -= dy * k;
				}
			}
		};
		force.initialize = (n) => { nodes = n; };
		return force;
	}

	function renderRegions() {
		const list = [...places.values()];
		const regions = regionLayer.selectAll('g.region').data(list, (d) => d.name);
		regions.exit().remove();
		const entering = regions.enter().append('g');
		entering.append('circle').attr('r', 0);
		entering.append('text');
		const all = entering.merge(regions)
			.attr('class', (d) => `region${d.plane === null ? ' void' : d.plane.done ? '' : ' building'}`)
			.attr('transform', (d) => `translate(${d.x},${d.y})`);
		all.select('circle').transition().duration(reducedMotion ? 0 : 600).attr('r', (d) => d.r);
		all.select('text')
			.attr('y', (d) => -d.r - 12)
			.text((d) => (d.plane && !d.plane.done ? `${d.name}, ${d.plane.progress}%` : d.name));

		// paths between planes, drawn from edge to edge
		const paths = [];
		const seen = new Set();
		for (const plane of state.snapshot.planes) {
			for (const other of plane.connections) {
				const key = [plane.name, other].sort().join('|');
				if (seen.has(key) || !places.has(other)) continue;
				seen.add(key);
				paths.push({ key, a: places.get(plane.name), b: places.get(other) });
			}
		}
		const edge = (from, to) => {
			const dx = to.x - from.x;
			const dy = to.y - from.y;
			const length = Math.hypot(dx, dy) || 1;
			return { x: from.x + (dx / length) * from.r, y: from.y + (dy / length) * from.r };
		};
		const lines = pathLayer.selectAll('line').data(paths, (d) => d.key);
		lines.exit().remove();
		lines.enter().append('line').attr('class', 'plane-path')
			.merge(lines)
			.attr('x1', (d) => edge(d.a, d.b).x).attr('y1', (d) => edge(d.a, d.b).y)
			.attr('x2', (d) => edge(d.b, d.a).x).attr('y2', (d) => edge(d.b, d.a).y);
	}

	function fitToView(force = false) {
		if (places.size === 0) return;
		const { width, height } = $('graph').getBoundingClientRect();
		if (!width || !height) return;
		let minX = Infinity, minY = Infinity, maxX = -Infinity, maxY = -Infinity;
		for (const p of places.values()) {
			minX = Math.min(minX, p.x - p.r - 30);
			maxX = Math.max(maxX, p.x + p.r + 30);
			minY = Math.min(minY, p.y - p.r - 45);
			maxY = Math.max(maxY, p.y + p.r + 30);
		}
		const key = [minX, minY, maxX, maxY, width, height].map(Math.round).join(',');
		if (!force && key === lastFitKey) return;
		lastFitKey = key;
		const scale = Math.min(1.3, 0.92 * Math.min(width / (maxX - minX), height / (maxY - minY)));
		const transform = d3.zoomIdentity.scale(scale).translate(-(minX + maxX) / 2, -(minY + maxY) / 2);
		(reducedMotion ? svg : svg.transition().duration(700)).call(zoom.transform, transform);
	}

	$('fit').addEventListener('click', () => {
		userZoomed = false;
		fitToView(true);
	});
	svg.on('click', (event) => {
		if (event.target === svg.node()) select(null);
	});

	const simulation = d3.forceSimulation()
		.force('charge', d3.forceManyBody().strength(-120).distanceMax(160))
		.force('link', d3.forceLink().id((d) => d.id))
		.force('collide', d3.forceCollide((d) => radius(d) + 14))
		.force('x', d3.forceX())
		.force('y', d3.forceY())
		.force('contain', containWithinPlace())
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
		if (!userZoomed) fitToView();
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
		buildPlaces();
		renderRegions();
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
		entering.append('circle').attr('class', 'supreme-halo');
		entering.append('circle').attr('class', 'health-track');
		entering.append('circle').attr('class', 'health').attr('transform', 'rotate(-90)');
		entering.append('circle').attr('class', 'body').attr('r', reducedMotion ? radius : 0);
		entering.append('circle').attr('class', 'pregnant-mark').attr('r', 3.5);
		entering.append('text');
		entering.append('title');
		if (!reducedMotion) {
			entering.select('.body').transition().duration(700).ease(d3.easeBackOut).attr('r', radius);
		}

		const all = entering.merge(nodeSel);
		const hasStatus = (d, name) => d.view.statuses.some((s) => s.startsWith(name));
		all.attr('class', (d) => `node ${sexClass(d.view.sex)}${d.view.alive ? '' : ' dead'}${d.id === state.selectedId ? ' selected' : ''}`
			+ `${hasStatus(d, 'pregnant') ? ' pregnant' : ''}${hasStatus(d, 'poisoned') ? ' poisoned' : ''}${d.view.supreme ? ' supreme' : ''}`);
		all.select('.supreme-halo').attr('r', (d) => radius(d) + 14);
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
		all.select('title').text((d) => [`${d.view.name}, ${d.view.supreme ? 'supreme deity, ' : ''}divine rank ${d.view.divineRank}${d.view.alive ? '' : ' (fallen)'}`,
			d.view.traits.join(', '), ...d.view.statuses].filter(Boolean).join('\n'));

		simulation.nodes(visible);
		simulation.force('link')
			.links(links)
			.distance((l) => (l.kind === 'family' ? 60 : l.kind === 'mate' ? 45 : 150))
			.strength(0);   // lines only show relationships; where a god stands is decided by their plane
		// pull every god towards the plane they are on; this is what makes travel visible
		const pull = (d) => (d.view.alive ? 0.2 : 0.08);
		simulation.force('x').x((d) => anchorOf(d).x).strength(pull);
		simulation.force('y').y((d) => anchorOf(d).y).strength(pull);
		simulation.alpha(0.6).restart();
		applyLabelScale();
		if (!userZoomed) fitToView();
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
				const spread = parent ? 24 : 80;
				const home = places.get(view.location) || { x: 0, y: 0 };
				node = {
					id: view.id,
					x: (parent ? parent.x : home.x) + (Math.random() - 0.5) * spread,
					y: (parent ? parent.y : home.y) + (Math.random() - 0.5) * spread,
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
		const livingIn = (place) => state.snapshot.deities.filter((d) => d.alive && d.location === place).map((d) => d.id);
		const inVoid = livingIn(VOID);
		const voidNote = el('p', { class: 'void-note' }, inVoid.length ? ['In the Void between the planes: ', ...listOfNames(inVoid)] : 'Nobody is in the Void between the planes.');
		if (planes.length === 0) {
			container.replaceChildren(voidNote, el('p', { class: 'empty' }, 'No god has started shaping a plane yet.'));
			return;
		}
		container.replaceChildren(voidNote, ...planes.map((plane) => el('article', { class: `plane${plane.done ? ' done' : ''}` },
			el('h3', {}, plane.name, el('small', {}, plane.done ? 'Complete' : `${plane.progress}%`)),
			el('div', { class: 'progress', role: 'progressbar', 'aria-valuenow': plane.progress, 'aria-valuemin': 0, 'aria-valuemax': 100, 'aria-label': `${plane.name} progress` },
				el('span', { style: `width:${plane.progress}%` })),
			el('p', {}, plane.done ? 'Shaped by ' : 'Being shaped by ', ...listOfNames(plane.builderIds)),
			plane.done ? el('p', {}, livingIn(plane.name).length ? ['Here now: ', ...listOfNames(livingIn(plane.name))] : 'Nobody is here right now') : null,
			plane.connections.length ? el('p', {}, `Paths to ${plane.connections.join(', ')}`) : null,
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
			el('p', { class: 'who' }, `${view.supreme ? 'Supreme deity, ' : ''}${view.supreme ? view.gender.toLowerCase() : view.gender}, ${view.sexuality.toLowerCase()}`),
			el('dl', {},
				el('dt', {}, 'Traits'), el('dd', {}, view.traits.length ? el('ul', { class: 'chips' }, view.traits.map((t) => el('li', {}, t))) : 'None'),
				el('dt', {}, 'Temperament'), el('dd', {}, `Aggression ${view.aggression}, lust ${view.lust}, creativity ${view.creativity}`),
				view.statuses.length ? el('dt', {}, 'Right now') : null,
				view.statuses.length ? el('dd', {}, el('ul', { class: 'chips' }, view.statuses.map((s) => el('li', { class: `status ${s.split(',')[0]}` }, s)))) : null,
				el('dt', {}, 'Divine rank'), el('dd', {}, view.divineRank),
				view.crime > 0 ? el('dt', {}, 'Crime') : null,
				view.crime > 0 ? el('dd', {}, `${view.crime} of 100`,
					el('div', { class: 'health-bar crime-bar' }, el('span', { style: `width:${view.crime}%` }))) : null,
				el('dt', {}, view.alive ? 'Health' : 'Fate'), el('dd', {}, view.alive
					? [`${Math.max(view.health, 0)} of ${view.maxHealth}`,
						el('div', { class: 'health-bar' }, el('span', { style: `width:${Math.max(0, Math.min(100, (100 * view.health) / view.maxHealth))}%` }))]
					: fate),
				el('dt', {}, 'Age'), el('dd', {}, view.age),
				el('dt', {}, 'Born'), el('dd', {}, view.parentIds.length === 0 && view.bornIn === 0 ? 'One of the first gods' : `Generation ${view.bornIn}`),
				el('dt', {}, view.alive ? 'Location' : 'Died in'), el('dd', {}, view.location || VOID),
				el('dt', {}, 'Home plane'), el('dd', {}, view.homePlane || 'None'),
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
			if (e.deity.supreme) chronicleEntry('supreme', godName(e.deity.id), `, a supreme deity, came into being in ${e.deity.location}.`);
			else if (e.deity.parentIds.length === 0) chronicleEntry('', godName(e.deity.id), ` appeared in ${e.deity.location}.`);
			else chronicleEntry('', godName(e.deity.id), ' was born to ', ...listOfNames(e.deity.parentIds), '.');
		},
		DeityDied: (e) => {
			if (e.cause === 'slain' && e.killerId) chronicleEntry('death', godName(e.deityId), ' was slain by ', godName(e.killerId), '.');
			else if (e.cause === 'slain') chronicleEntry('death', godName(e.deityId), ' was slain.');
			else chronicleEntry('death', godName(e.deityId), ' died of old age.');
		},
		MateFound: (e) => chronicleEntry('', godName(e.deityId), ' and ', godName(e.mateId), ' became mates.'),
		Conceived: (e) => (e.partnerId === e.deityId
			? chronicleEntry('', godName(e.deityId), ' is expecting a child on their own.')
			: chronicleEntry('', godName(e.deityId), ' is expecting a child with ', godName(e.partnerId), '.')),
		Poisoned: (e) => chronicleEntry('fight', godName(e.poisonerId), ' poisoned ', godName(e.deityId), '.'),
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
		DeityMoved: (e) => {
			if (e.reason === 'fled a fight') chronicleEntry('fight', godName(e.deityId), ` lost a fight and fled from ${e.from} to the Void.`);
			else if (e.reason === 'fled their crimes') chronicleEntry('fight', godName(e.deityId), ` fled from ${e.from} to the Void to escape their crimes.`);
			else chronicleEntry('', godName(e.deityId), ` travelled from ${e.from} to ${e.to}.`);
		},
		CrimeCommitted: (e) => chronicleEntry('fight', godName(e.deityId), ` committed a crime (crime ${e.crime} of 100).`),
		PlacesConnected: (e) => chronicleEntry('', godName(e.deityId), ` opened a path between ${e.placeA} and ${e.placeB}.`),
		WorldChanged: (e) => applySnapshot(e.snapshot),
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
				chronicleEntry('error', `The server refused that: ${problem.message || problem.detail || problem.error || response.status}.`);
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
		userZoomed = false;
		lastFitKey = '';
		regionLayer.selectAll('*').remove();
		pathLayer.selectAll('*').remove();
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

	$('add-god').addEventListener('click', async () => {
		if (!state.simId) return;
		const info = await call('POST', `${API}/${state.simId}/deities`);
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
