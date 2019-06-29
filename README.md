<img src="https://assets.gitlab-static.net/uploads/-/system/project/avatar/6581187/atom_logo1.png" width="400" height="400">

# [Atom](https://gitlab.com/divinecode/atom/Atom)
<a href="http://files.minecraftforge.net/maven/net/minecraftforge/forge/index_1.12.2.html"><img src="https://img.shields.io/badge/Forge-1.12.2--14.23.5.2836-brightgreen.svg?colorB=26303d"></a>

Atom is a Minecraft server core created by AtomicInteger which is based on [MinecraftForge](https://github.com/MinecraftForge/MinecraftForge) and [CraftBukkit](https://hub.spigotmc.org/stash/projects/SPIGOT/repos/craftbukkit/browse) for 1.12.2 version of Minecraft.  
Also some parts of the code are taken from such project as [Spigot](https://hub.spigotmc.org/stash/projects/SPIGOT/repos/spigot/browse)
and its derivatives.

The Original code is from https://gitlab.com/divinecode/atom/Atom

Our main goals are:
1. Stable work with both Bukkit plugins and MinecraftForge mods support.
2. Highest performance possible.

## Installation
The Atom latest version can be dowloaded on the following page: https://ci.openprocesses.ml/job/jospehworks/job/AtomMC/.  
Once the download is completed you have to follow the next steps:
1. Create a directory, where your future server will be located.
2. Put a jar file which ends with `-server.jar` and a `libraries` folder from artifacts.zip to this directory.
3. Launch the core using `java -jar` command.  
    Example (You may add optional flags): `java -jar Atom-*-*-server.jar`

## Building
- `git clone https://github.com/josephworks/AtomMC.git`
- `git submodule update --init --recursive`
- `gradlew build`

## Getting Help
Should you have any questions or need any assistance please do not hesitate to create an issue.

## Contributing
You're always more than welcome to send pull requests and raise issues.

