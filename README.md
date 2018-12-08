# AtomMC (Abandoned)
<a href="https://discord.gg/ddgXan7"><img src="https://img.shields.io/badge/chat-discord-blue.svg"></a>
<a href="https://gitlab.com/AtomMC/Atom/pipelines"><img src="https://img.shields.io/badge/build-download-green.svg"></a>

Atom is a Minecraft server core which is based on [MinecraftForge](https://github.com/MinecraftForge/MinecraftForge) and [CraftBukkit](https://hub.spigotmc.org/stash/projects/SPIGOT/repos/craftbukkit/browse) for 1.12.2 version of Minecraft.  
Also some parts of the code are taken from such project as [Spigot](https://hub.spigotmc.org/stash/projects/SPIGOT/repos/spigot/browse)
and its derivatives.  
Our main goals are:
1. Stable work with both Bukkit plugins and MinecraftForge mods support.
2. Highest performance possible.

## Installation
The Atom latest version can be dowloaded on the following page: https://gitlab.com/AtomMC/Atom/pipelines.  
Once the download is completed you have to follow the next steps:
1. Create a directory, where your future server will be located.
2. Put a .jar-file with a `-server.jar` prefix and a `libraries` folder from artifacts.zip to this directory.
3. Launch the core using `java -jar` command.  
    Example (You may add optional flags): `java -jar Atom-master-7e9d289-server.jar`

## Building
- `git clone https://gitlab.com/AtomMC/Atom.git`
- `gradlew build`

## Getting Help
Should you have any questions or need any assistance please do not hesitate to join our [Discord server](https://discord.gg/Fm5qQDV).

## Contributing
You're always more than welcome to send pull requests and raise issues.

## Support project
If you want to express your gratitude you may support AtomicInteger on [Patreon](https://www.patreon.com/AtomicInteger) or emeraldtnt on [PayPal](https://paypal.me/emeraldtnt).
