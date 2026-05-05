# DeluxeBedwars

A Bedwars minigame plugin for Spigot/Paper. Includes the core game
loop (teams, beds, generators, shop), party support, an in-game setup
flow for arenas, and a cosmetics system.

> Status: source-only / work in progress. There is currently no
> packaged release in this repository.

## Features

- Team-based Bedwars game loop with bed protection
- Party / queue command (`/bedwarsparty`)
- In-game arena setup (`/bedwarssetup`)
- Team management (`/bedwarsteam`)
- Cosmetics system: pets, kill effects, shouts, sprays, victory dances,
  bed-break effects, glyphs, projectile trails, etc.
- Citizens NPC integration

## Requirements

- Spigot / Paper
- Java 8 or newer
- Citizens (compile dependency)

## Commands

- `/bedwars` — main game command
- `/bedwarsadmin` — admin tools
- `/bedwarsparty` — party / queue management
- `/bedwarssetup` — arena setup
- `/bedwarsteam` — team management

## Build

Standard Maven build:

```
mvn clean package
```

## License

See `LICENSE`.
