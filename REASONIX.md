# RarityGlow — Working Knowledge

## Stack
- **Language:** Java 25, targeting `--release 25`
- **Framework:** Fabric mod (Minecraft 26.1, Fabric Loader 0.18.5)
- **Build:** Gradle 9.4, Fabric Loom 1.15-SNAPSHOT
- **Config:** Cloth Config 26.1.154 via AutoConfig (TOML serializer)
- **UI (optional):** ModMenu 18.0.0-alpha.8
- **CI:** GitHub Actions (`./gradlew build` on push/PR, JDK 25 Microsoft)

## Layout
- `src/main/java/kitejs/` — main mod (`RarityGlow.java` client initializer), config classes, mixin, utils
- `src/main/resources/` — `fabric.mod.json`, `mixins.json`, assets (icon, lang)
- `build.gradle` + `gradle.properties` — build config; all dependency versions in `gradle.properties`

## Commands
| Command           | What it does                           |
|-------------------|----------------------------------------|
| `./gradlew build` | Compile + produce JAR in `build/libs/` |

No dedicated test/lint/format scripts defined.

## Conventions
- **Client-only mod** — implements `ClientModInitializer`; no server entrypoint
- **Package:** `kitejs`; mod id `rarityglow`
- **Config:** `@Config(name = "rarityglow")` with `Toml4jConfigSerializer`; save listener calls `GlowColorCache.updateFromConfig()`
- **Mixin injection points:** `EntityRenderer.extractRenderState()` TAIL (outline glow), `ItemEntity.tick()` TAIL (particle beam)
- **Mixin naming:** `modid$methodName` (e.g. `rarityglow$spawnBeamParticles`)
- **Color storage:** RGB as `"R,G,B"` CSV string in config; parsed to `0xAARRGGBB` ints in `GlowColorCache`
- **Thread safety:** Config + color cache fields are `volatile` (accessed from render thread)
- **Switch expressions** used for rarity branching (Java 21+ feature enabled by mixins compat)
- **Particle beam:** `ItemEntityMixin` → `ParticleGenerator.generateParticles()` spawns `DustParticleOptions(color, 1.2f)` at random Y within beam height; per-rarity `beamEnabled` toggle independent from glow `enabled`
- **Config structure:** `enable` (master switch) → per-rarity `enabled` (glow) + `beamEnabled` (beam) + `rgb` (color); global `BeamSettings` (particleCount, beamHeight, beamOffset)
- **ItemRarityHelper methods:** `getGlowColorIfEnabled()` (respects per-rarity `enabled`), `isBeamEnabled()` (respects per-rarity `beamEnabled`), `getBeamColor()` (ignores glow toggle, returns color if master switch on)

## Watch out for
- `mixins.json` `compatibilityLevel` is `JAVA_21` — do NOT raise to 25, Mixin doesn't support it
- Config is loaded once during `onInitializeClient` — any save listener callback *must* re-call `GlowColorCache.updateFromConfig()` or the render thread reads stale colors
- New rarity levels (beyond Common/Uncommon/Rare/Epic) must be added in 5 places: `RarityGlowConfig`, `GlowColorCache`, `ItemRarityHelper.getGlowColorIfEnabled()`, `ItemRarityHelper.isBeamEnabled()`, `ItemRarityHelper.getBeamColor()`
- Glow and beam toggles are independent — turning off glow for a rarity (`enabled=false`) does NOT disable the beam for that rarity (checked via `beamEnabled`)
- `run/` directory is in `.gitignore` — local dev data, do not commit
