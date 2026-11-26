# Model Browser × Easy Anvils Compat

Makes **[Model Browser](https://modrinth.com/mod/model-browser)** work inside **[Easy Anvils](https://modrinth.com/mod/easy-anvils)**' anvil, and adds an in-game filter for the browser's model list.

## Why
Easy Anvils replaces the vanilla anvil screen and result logic. That quietly breaks Model Browser:
- the search button and model panel never show up in the anvil
- renaming an item to a model id (e.g. `mypack:my_model`) only renames it — no model
- clearing the name doesn't restore the original look

This mod fixes all three, and keeps Easy Anvils' own name box, costs and features.

## Features
- 🔍 **Model Browser panel in Easy Anvils' anvil**: button, search, model grid and page arrows
- 🪄 **Rename to apply a model**: same rules as Model Browser, including head-wearable helmets that show the model instead of the armor, and the `-e`, `-g`, `-G` flags
- ↩️ **Reset**: leave the name empty to give an item back its original model, name, wear behavior and glint
- 🗂️ **Model filter menu** (keybind, unbound by default): every model grouped by mod. Hide or show a whole mod or single models, with search. Changes apply instantly.
- 📄 `config/mbeacompat-models.txt` lists every model id by mod, so you can find what a mystery item belongs to
- Centers Model Browser's page counter so long counts like `1/100` don't drift right

## Installation
| Side | Needed for |
|---|---|
| **Server** | Required: applying and resetting models in Easy Anvils' anvil |
| **Client** | Optional: the anvil panel, filter menu and page counter fix |

Players without it on their client can still join; they just type model ids by hand.

**Requires:** Fabric Loader, Fabric API, Easy Anvils, Model Browser. Minecraft 26.3.

## Config
`config/mbeacompat.properties` (per client):
```
hidden_namespaces=somemod,othermod
hidden_models=somemod:bow,somemod:crossbow
```
Edit it by hand or use the filter menu (Controls → Key Binds → Model Browser Compat). Press F3+T after hand edits.

## Building
Run `./gradlew build` (copy `gradle/`, `gradlew`, `gradlew.bat` from any 26.3 Fabric project if missing). The jar is in `build/libs/`.

## License
LGPL-3.0-only. See `COPYING` and `COPYING.LESSER`.

---
Not affiliated with Model Browser (deborn) or Easy Anvils (Fuzs). Contains no code from either mod.
