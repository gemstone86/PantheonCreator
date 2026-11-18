# Pantheon Creator

Simulates a pantheon of gods over many generations. Gods find mates and have children, create concepts like Storms or Justice, build planes of existence and travel between them, form friendships and rivalries, fight, and eventually die. Watch it happen live in your browser, or run it as text in a terminal.

## Run it

You need **Java 17 or newer**. On Windows you can install it with:

```powershell
winget install EclipseAdoptium.Temurin.21.JDK
```

Then, from the project folder in PowerShell:

```powershell
.\run.ps1
```

This builds the project, starts the server and opens http://localhost:8080 in your browser. The first build downloads Maven and the libraries, so it takes a few minutes; later starts are quick. Press Ctrl+C to stop.

If PowerShell refuses to run the script, run it once with:

```powershell
powershell -ExecutionPolicy Bypass -File .\run.ps1
```

### Options

| Command | What it does |
| --- | --- |
| `.\run.ps1 -Port 9000` | Uses another port |
| `.\run.ps1 -Seed 42 -Generations 80` | Opens the browser on a specific pantheon |
| `.\run.ps1 -NoBrowser` | Starts the server without opening the browser |
| `.\run.ps1 -SkipBuild` | Starts the last build without rebuilding |
| `.\run.ps1 -Cli -Seed 42 -Generations 40` | Runs the text version in the terminal |

On macOS or Linux, use the Maven wrapper directly: `./mvnw spring-boot:run`, or for the text version `./mvnw -q package -DskipTests && java -cp target/classes pantheon.PantheonCreator s 42 g 40`.

## Using the web app

- **New pantheon** starts a new simulation. Leave the seed empty for a random one; the same seed always gives the same pantheon.
- **Play** runs generations one after another at the chosen speed; **Next generation** runs a single one.
- **Add a god** brings a new random god into the Void straight away.
- **The map** shows the planes of existence. Gods start in the Void in the middle; finished planes appear around it as gold circles (dashed while still being shaped), with dotted paths between connected planes. Each god stands on the plane they are on, so you can watch them travel. The view zooms out as planes appear; **Show all planes** brings it back after you have panned or zoomed.
- **Gods** are circles: size is divine rank, colour is sex, and the gold ring is health. Lines show family, mates (gold, dashed), and strong feelings (green for likes, red for dislikes). Fights flash red. Tick **Fallen gods** to also show dead gods where they died.
- **Click a god** to see their domains, family and feelings.
- **Planes** shows each plane being built, who is building it and which concepts it holds.
- **Chronicle** lists births, deaths, fights and creations. Turn on **Inner thoughts** to see what each god is thinking.
- **Domains** along the bottom light up gold once a god has created them.

## Planes and travel

- Gods start in **the Void**, the space between the planes. From there they can enter any finished plane.
- The god who finishes a plane makes it their **home plane** and moves there.
- From a plane, gods follow its **paths** to other planes. If a plane has no paths, a god who wants to move opens one to another plane, or returns to the Void if there is no other plane.
- Gods very rarely go **back to the Void** on their own (2% of the times they move from a plane). They go there when **fleeing**: the loser of a fight flees to the Void 30% of the time, and a god whose crimes catch up with them flees there too. Gods don't follow anyone into the Void.
- **Crime:** now and then a god turns to crime. Their crime score (0–100) then has a 25% chance of rising by 10 each generation. Once it is above 75, they flee to the Void, their score starts over and they stop.
- **Supreme deity:** each generation there is a 1% chance that a supreme deity comes into being in the Void, at most one per pantheon. It has divine rank 25–30, cannot die (it can be hurt, but never below 1 health), and mostly stays in the Void: it rarely visits a plane, soon returns, and doesn't leave the Void to fight. In the browser it has a gold halo.
- Gods mostly socialise with, and court, gods **on the same plane**: 80% of the time when anyone suitable is there (`SAME_PLACE_PREFERENCE` in `Simulation.java`).
- A god who wants to fight someone on another plane **travels there first**.
- Gods don't risk dying of old age before 20; after that the risk grows by half a percentage point a year (`OLD_AGE_START` and `OLD_AGE_RISK_PER_YEAR` in `Deity.java`).

## Traits, temperament and statuses

- Every god has a **temperament** (aggression, lust and creativity, each starting between -10 and 10) and **two or three traits** that shift it:

  | Trait | Temperament | Now and then (30% a generation) |
  | --- | --- | --- |
  | Aggressive | +10 aggression, -5 creativity | looks for a rival |
  | Ambitious | +5 aggression, creativity and lust | tries to grow more powerful |
  | Creative | +10 creativity | tries to create a concept |
  | Lustful | +10 lust | looks for a mate |
  | Pacifist | -10 aggression | socialises |

  Aggressive and pacifist never go together. Gods with aggression above 10 also go looking for rivals whenever they aren't busy.
- **Mating:** a god courts once their lust is above -10; while waiting, lust builds by 1–2 a generation. Sometimes a god has a child alone, and always if they are the only god left. After a successful courtship the chance of a **pregnancy** is 100% minus 4% per living god, so there are none at all from 25 living gods. The child is born 1–9 generations later, if the parent is still alive. A god gives up after three failed attempts.
- **Poison:** a god who wins a fight poisons the loser 10% of the time, for 2–6 damage a generation over 2–4 generations. If the poison kills, the poisoner is blamed.
- In the browser, pregnant gods have a dot in the middle and poisoned gods a green health ring; click a god to see their traits, temperament and statuses.
- The balance values are constants: `LUST_NEEDED`, `PREGNANCY_CHANCE` and `PREGNANCY_DROP_PER_GOD` in `FindMate.java`, `AGGRESSION_FOR_TROUBLE` in `FindMotivation.java`, `URGE_CHANCE` in `Trait.java` and `POISON_CHANCE` in `Fight.java`. With these values a 50-generation pantheon has about 4 living gods on average and dies out in about 1 run in 6.

## How it fits together

```
src/main/java/pantheon/
  sim/          Simulation (step(), snapshots), events, random names and domains
  gods/         Deity and Relation
  traits/       Aggressive, Ambitious, Creative, Lustful, Pacifist
  status/       Pregnant and Poisoned
  behaviours/   What gods do each generation: find a mate, fight, create, ...
  cosmos/       The cosmos (the Void) and its planes
  web/          Spring Boot app: REST API and live event stream
  PantheonCreator.java   Text version
src/main/resources/static/   Browser UI (D3 graph)
```

The simulation knows nothing about the web. It reports everything that happens as events (`SimEvent`). The text version prints them; the web app streams them to the browser with Server-Sent Events.

### API

| Request | Purpose |
| --- | --- |
| `POST /api/simulations` with `{"seed": 42, "generations": 80}` | Create a simulation (both fields optional) |
| `GET /api/simulations/{id}` | Seed, generation, and whether it is playing |
| `GET /api/simulations/{id}/state` | The full current state |
| `POST /api/simulations/{id}/step` | Run one generation |
| `POST /api/simulations/{id}/play?msPerGeneration=1000` | Run continuously |
| `POST /api/simulations/{id}/pause` | Stop running |
| `POST /api/simulations/{id}/deities` | Add a random god to the Void (up to 200 living gods) |
| `GET /api/simulations/{id}/events` | Live event stream |
| `DELETE /api/simulations/{id}` | Remove a simulation |

## Tests

```powershell
.\mvnw.cmd test
```

D3 is included in `src/main/resources/static/vendor` under its ISC license, so the page works without internet access. Only the fonts come from Google Fonts; without internet the page falls back to standard fonts.
