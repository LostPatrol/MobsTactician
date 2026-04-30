# Mobs Tactician

Minecraft NeoForge mod

Makes hostile mobs fight with more tactical and player-like behaviors.

> Experimental preview. Not designed for vanilla survival balance.

## Key Features

| Mob | Main Change | Effect |
| --- | --- | --- |
| Skeleton | Tactical ranged/melee AI | Blocks arrows, times shields, swaps weapons |
| Zombie | Wind-charge mace attack | Leaps into aerial mace smashes |
| Phantom | Rocket dive | Uses fireworks to accelerate during swoops |
| Creeper | Predictive swelling | Starts exploding before obvious melee rushes |

## Tactical Skeletons

Enhanced skeletons can spawn with enchanted bows, backup melee weapons, defensive blocks, and sometimes armor.

+ Holds a drawn bow when the target is blocking with a shield, then fires after the shield drops
+ Places a short dirt/cobblestone wall when a player is about to shoot it with a bow
+ Switches to a sword when the target gets close
+ Switches back to a bow when distance opens up
+ If comboed in melee, briefly forces ranged mode again
+ May drop weapons, armor, or remaining defensive blocks

Equipment tiers:

+ Iron tier: bow, iron sword, dirt blocks, possible iron armor
+ Diamond tier: stronger bow, diamond sword, cobblestone blocks, diamond armor
+ Netherite tier: stronger bow, netherite sword, more cobblestone blocks, netherite armor

## Tactical Zombies

Some zombies can spawn with a mace and wind charges.

+ Uses wind charges to jump toward the target
+ Attempts aerial mace smash attacks while falling
+ Iron, diamond, and netherite variants have different equipment strength
+ Netherite variants may gain stronger mace enchantments such as Wind Burst
+ Baby chicken jockeys are excluded
+ May drop leftover wind charges, mace, or armor

## Enhanced Phantoms

Enhanced phantoms carry a weapon and firework rockets.

+ Uses a firework rocket during swoop attacks
+ Rocket boost becomes stronger on higher difficulty
+ Held weapon and rocket are rendered on the phantom
+ Cats can still scare them away
+ May drop leftover rockets or their weapon

## Smarter Creepers

Creepers use a replacement swelling goal.

+ Starts swelling early if a player is looking at it, holding a melee weapon, and rushing into explosion range
+ Prediction is stronger on higher difficulty
+ If a targeted creeper would die from fall damage, it explodes instead

## Installation

Requirements:

+ Minecraft `1.21.11`
+ NeoForge `21.11.38-beta` or compatible
+ Java `21`

Put the built `.jar` file into the `mods` folder of a matching NeoForge instance.

## Build

```powershell
.\gradlew.bat build
```

The jar will be generated in `build/libs`.

## Notes

+ This mod intentionally makes several mobs unfair.
+ Recommended for challenge packs, custom maps, or short chaos-focused playthroughs.
+ Spawn rates and behavior toggles are not fully configurable yet.

## License

MIT License. See [LICENSE.txt](LICENSE.txt).
