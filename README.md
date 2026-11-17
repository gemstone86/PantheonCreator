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
- Gods mostly socialise with, and court, gods **on the same plane**: 80% of the time when anyone suitable is there (`SAME_PLACE_PREFERENCE` in `Simulation.java`).
- A god who wants to fight someone on another plane **travels there first**.
- Gods don't risk dying of old age before 20; after that the risk grows by half a percentage point a year (`OLD_AGE_START` and `OLD_AGE_RISK_PER_YEAR` in `Deity.java`).

## How it fits together

```
src/main/java/pantheon/
  sim/          Simulation (step(), snapshots), events, random names and domains
  gods/         Deity and Relation
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
