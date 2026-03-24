# discabilities

a paper 1.21 plugin where music discs give you unique abilities and passives. equip a disc to unlock its powers, then use right-click to activate its ability on a cooldown.

---

## how it works

- **right-click** a disc in your hand to equip it
- your equipped disc shows in the action bar hud
- each disc has a passive (always active) and an ability (right-click to use, has a cooldown)
- use `/withdraw` to unequip your disc and get it back as an item

---

## discs

| disc | passive | ability |
|------|---------|---------|
| 13 | take less damage, gain speed | summon wither skulls at nearby enemies |
| cat | deal wither + poison to enemies every 20 hits | unleash a burst of debuffs on nearby enemies |
| blocks | gain resistance | shield yourself and nearby allies with absorption |
| chirp | fire arrows back at attackers | launch a volley of 10 arrows in all directions |
| far | place a 2x2 cobweb on enemies every 20 hits | teleport forward |
| mall | gain extra health regen | call down lightning on nearby enemies |
| mellohi | gain regeneration over time | slow and weaken nearby enemies |
| stal | deal bonus melee damage | deal a powerful melee burst to your target |
| strad | auto-crit on hits | slam your target into the air with a crit strike |
| ward | gain fire resistance | ignite and slow nearby enemies |
| 11 | fire wither skulls back at attackers | launch charged wither skulls + heavy damage at nearby enemies |
| wait | deal bonus damage when at low health | drain health from nearby enemies |
| relic | drop extra loot on kills, mark bounty targets | summon a netherite aura that damages enemies |
| precipice | auto-fire arrows at nearby enemies | launch a barrage of piercing arrows |
| tears | reduce incoming damage | release a rain of damaging projectiles |
| 5 | gain speed based on health | fire a high-damage energy beam through enemies |
| lava chicken | permanent fire resistance, swim in lava | clear fire from allies and ignite enemies |
| creator | deal bonus damage at 64+ copper ingots | strike enemies with lightning at 64 copper threshold |
| creator music box | gain haste at 64+ copper ingots | trigger a lightning burst at copper threshold |
| chirp (alt) | fire arrows back at attackers when hit | launch arrow volley in all directions |
| disc 11 | wither skulls counter-attack | charged skull barrage + direct damage |

---

## commands

| command | description |
|---------|-------------|
| `/withdraw` | unequip your disc and return it to your inventory |
| `/trust <player>` | trust a player — they won't be affected by your abilities or damage |
| `/untrust <player>` | remove trust from a player |
| `/resetcooldowns` | reset all your disc cooldowns (admin) |
| `/getdisc <id>` | give yourself a specific disc (admin) |

---

## trust system

trusted players are completely protected from your disc effects — passive procs, ability damage, and melee buffs all skip them. great for playing with teammates on the same server.

```
/trust Notch
/untrust Notch
```

---

## installation

1. drop the `.jar` into your server's `plugins/` folder
2. restart the server
3. requires paper 1.21+

---

## building

```
mvn clean package
```

output jar will be in `target/`.
