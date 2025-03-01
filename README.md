# LavaFishing

Paper plugin: A unique Minecraft experience that lets you fish in lava!

## Overview
LavaFishing is a plugin that introduces an innovative fishing experience in Minecraft.  
This project was born out of a failed [PR](https://github.com/PaperMC/Paper/pull/12147#event-16417159614) effort to Paper.  
Mojang, please allow us to fish in lava!  
I take performance very seriously, so if you encounter any performance issues, please feel free to contact us.

## Features
Simply lava fishing. No extra features beyond that.

## Supported Versions
- **Paper**: 1.21.4

## Installation
1. Download the latest `LavaFishing.jar` from the [GitHub Releases Page](https://github.com/NamiUni/LavaFishing/releases).
2. Place the downloaded jar file into your server’s `plugins` folder.
3. Restart or reload your server and ensure the plugin loads successfully.

## Usage
- **Starting to Fish**
  Use a fishing rod on lava to activate the lava fishing feature.  

## Configuration (config.yml)
On first run, a `config.yml` file will be automatically generated, allowing you to customize:
- **Fishing Particles**: Modify the particles generated while lava fishing.
- **Fishing Sounds**: Modify the sounds played during lava fishing.
- **Loot Table**: Change the loot table for catch fish.

*Refer to the comments in the generated `config.yml` for detailed configuration options.*

## Permissions
Control access to the lava fishing feature with the following permission:
- `lavafishing.command.reload` — Allows reloading the configuration via the reload command.

## Development and Contribution
LavaFishing is an open-source project.  
Bug reports, feature suggestions, and pull requests are welcome via [GitHub Issues](https://github.com/NamiUni/LavaFishing/issues).

### How to Contribute
1. Fork the repository.
2. Make changes or add new features on your branch.
3. Submit a pull request for review.

### Developer API
Bukkit events here:
[Developer Bukkit Events](https://github.com/NamiUni/LavaFishing/tree/master/src/main/java/com/github/namiuni/lavafishing/event)

## License
This project is licensed under the GPL-3.0 License. For details, see the [LICENSE](./LICENCE.txt) file.

## Author
- **NamiUni** — [GitHub Profile](https://github.com/NamiUni)

## Contact
Discord server will be ready in the near future. 
For now, please reach out via [GitHub Issues](https://github.com/NamiUni/LavaFishing/issues) or on Discord (@.namiu).

---

Enjoy your lava fishing adventure!
