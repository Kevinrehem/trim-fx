# TrimFX (`trimfx`)

**TrimFX** brings meaningful progression and build diversity to Vanilla **Armor Trims** in **Minecraft 26.3** (**NeoForge 26.3.0.95**, **Java 25**).

Every trimmed armor piece worn in the 4 armor slots (`HEAD`, `CHEST`, `LEGS`, `FEET`) counts **independently** toward its **Trim Pattern Set** and its **Trim Material Set**, unlocking scaling bonuses at:
- **2 Pieces → Tier I**
- **3 Pieces → Tier II**
- **4 Pieces → Tier III (Full Set)**

---

## 💎 11 Trim Material Bonuses (Pure `AttributeModifier`s)

| Material | Attribute | Tier I (2 pcs) | Tier II (3 pcs) | Tier III (4 pcs) |
|----------|-----------|----------------|-----------------|------------------|
| **Iron** | `ARMOR` | `+1` | `+2` | `+3` |
| **Copper** | `MOVEMENT_SPEED` | `+4%` | `+8%` | `+12%` |
| **Gold** | `ATTACK_SPEED` | `+5%` | `+10%` | `+15%` |
| **Diamond** | `ARMOR_TOUGHNESS` | `+1.0` | `+2.0` | `+3.5` |
| **Netherite** | `KNOCKBACK_RESISTANCE` & `BURNING_TIME` | `+10%` KB / `-15%` Burn | `+20%` KB / `-30%` Burn | `+35%` KB / `-50%` Burn |
| **Redstone** | `BLOCK_BREAK_SPEED` | `+8%` | `+16%` | `+25%` |
| **Lapis Lazuli** | `OXYGEN_BONUS` & `WATER_MOVEMENT_EFFICIENCY` | `+1` Oxy / `+25%` Swim | `+2` Oxy / `+50%` Swim | `+3` Oxy / `+80%` Swim |
| **Emerald** | `LUCK` | `+1` | `+2` | `+3` |
| **Quartz** | `ATTACK_DAMAGE` | `+1.0` | `+2.0` | `+3.0` |
| **Amethyst** | `BLOCK_INTERACTION_RANGE` & `ENTITY_INTERACTION_RANGE` | `+0.5` Reach | `+1.0` Reach | `+1.5` Reach |
| **Resin** | `MAX_HEALTH` | `+2 HP` (1 Heart) | `+4 HP` (2 Hearts) | `+6 HP` (3 Hearts) |
| **Rose Quartz (Biomes O' Plenty)** | `MAX_ABSORPTION` & Melee Lifesteal | `+2` Max Absorption + `8%` Lifesteal | `+4` Max Absorption + `15%` Lifesteal | `+6` Max Absorption + `25%` Lifesteal |

---

## 🛡️ 18 Trim Pattern Set Bonuses (Thematic Abilities & Procs)

| Pattern | Tier I (2 pcs) | Tier II (3 pcs) | Tier III (4 pcs) |
|---------|----------------|-----------------|------------------|
| **Sentry** | `+10%` Ranged Damage | `+20%` Ranged Damage | `+35%` Ranged Damage + `Glowing` (5s) |
| **Vex** | `8%` Dodge Chance | `15%` Dodge Chance | `25%` Dodge Chance |
| **Wild** | `-50%` Poison + `Regen I` (3s) on Kill | `-75%` Poison + `Regen I` (5s) on Kill | Poison Immunity + `Regen I` (8s) on Kill |
| **Dune** | `-15%` Fire/Explosion + `Absorption I` (<40% HP) | `-25%` Fire/Explosion + `Absorption II` (<40% HP) | `-40%` Fire/Explosion + `Absorption III` (<40% HP) |
| **Coast** | `Water Breathing` + `+25%` Submerged Mining | `Water Breathing` + `+50%` Submerged Mining | `Water Breathing`, `+100%` Mining + `Dolphin's Grace` |
| **Tide** | Reflects `15%` Damage (Hydraulic Thorns) | Reflects `25%` Damage (Hydraulic Thorns) | Reflects `40%` Damage + `Conduit Power` in Water |
| **Flow** | `-40%` Fall Damage + `Jump Boost I` | `-70%` Fall Damage + `Jump Boost II` | `100%` Fall Immunity + `Jump Boost III` |
| **Bolt** | `10%` Chain Shock (`2 DMG`, 2x in Thunder) | `18%` Chain Shock (`4 DMG`, 2x in Thunder) | `30%` Chain Shock (`6 DMG`, 2x in Thunder) |
| **Ward** | `Darkness` Immunity + `-10%` Dark Damage | `Darkness` Immunity + `-18%` Dark Damage | `Darkness` Immunity, `-28%` Dark Damage + `Night Vision` |
| **Silence** | `+25%` Sneak Speed + `-20%` Mob Detection | `+50%` Sneak Speed + `-35%` Mob Detection | `+80%` Sneak Speed, `-50%` Detection + Sculk Silence |
| **Wayfinder** | `-25%` Sprint Exhaustion + `+0.5` Step Height | `-50%` Sprint Exhaustion + `+0.5` Step Height | `-75%` Sprint Exhaustion + `+0.5` Step Height |
| **Raiser** | `+15%` Healing + `+1` Food / `+0.5` Sat | `+30%` Healing + `+2` Food / `+1.0` Sat | `+50%` Healing + `+3` Food / `+1.5` Sat |
| **Shaper** | `15%` Durability Conservation Chance | `30%` Durability Conservation Chance | `45%` Durability Conservation Chance |
| **Host** | `+20%` XP Gained + `2.0m` Item/XP Magnet | `+40%` XP Gained + `3.5m` Item/XP Magnet | `+70%` XP Gained + `5.0m` Item/XP Magnet |
| **Snout** | Pacifies Piglins + `+12%` Nether/Piglin Damage | Pacifies Piglins + `+22%` Nether/Piglin Damage | Pacifies Piglins + `+35%` Nether/Piglin Damage |
| **Rib** | `-50%` Wither Damage + `Wither I` (3s) on Hit | `-80%` Wither Damage + `Wither I` (5s) on Hit | Wither Immunity, `Wither II` (6s) + Heals 1 Heart on Kill |
| **Eye** | Calms Endermen, No Pearl Fall DMG (`-25%` CD), `+10%` Crit | Calms Endermen, No Pearl Fall DMG (`-50%` CD), `+20%` Crit | Calms Endermen, No Pearl Fall DMG (`-75%` CD), `+35%` Crit |
| **Spire** | Levitation -> Slow Fall, Air-Sneak Glide, `12%` Levitate Hit | Levitation -> Slow Fall, Air-Sneak Glide, `20%` Levitate Hit | Levitation -> Slow Fall, Air-Sneak Glide, `30%` Levitate Hit |

---

## 🛠️ Build & Testing

- **Fast Domain Unit Tests (Maven + JUnit 5):**
  ```powershell
  mvn clean test -f pom-test.xml
  ```
- **Full NeoForge Build (Gradle):**
  ```powershell
  .\gradlew.bat compileJava test
  ```
