# DragonGameCore
The core plugin for BlueDragon minigames.
## Requirements
To use this plugin, some other plugins may be required.

These plugins are REQUIRED for the plugin to run:
 - `DebugAPI` by skater77i ([GitHub](https://github.com/skater77i/BlueDragonDebug/releases))
 
These plugins add extra features to the plugin:
 - `PlaceholderAPI` by clip ([SpigotMC](https://www.spigotmc.org/resources/placeholderapi.6245/))
## Commands
See [plugin.yml](/src/plugin.yml) for more information.
- `/join`
- `/leave`
- `/joingui <game>`
- `/maps <game>`
- `/games`
- `/spec <player>`
- `/party ...`
- `/aaaddmap <game> <x1> <y1> <z1> <x2> <y2> <z2> <name>`
- `/aaremovemap <game> <map>`
- `/aastop [game]`
- `/aareload [game]`
- `/aadebug`
- `/aaspawnpoint <game> <map> <team>`
## Permissions
### Commands
### Bypasses
- `arcade.bypass.block.break`
  - Allows players to break blocks before the game starts.
- `arcade.bypass.block.place`
  - Allows players to place blocks before the game starts.
- `arcade.bypass.inventory`
  - Allows players to move items in their inventory during a game and before it starts.
- `arcade.bypass.commands`
  - Allows players to execute commands other than /leave during a game.
### Other
- `arcade.priorityqueue`
  - Gives a player access to priority queue
- `arcade.selectmap`
  - Gives a player access to the map selector
### Parties
- `arcade.party.private`
  - Allows the player to use private games
- `arcade.party.public`
  - Allows the player to open the party to public access.