<img src="https://assets.gitlab-static.net/uploads/-/system/project/avatar/6581187/atom_logo1.png" width="400" height="400">

# [Atom](https://gitlab.com/divinecode/atom/Atom)
<a href="https://discord.gg/ddgXan7"><img src="https://img.shields.io/discord/452555382842458112.svg?colorB=7289DA&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAHYAAABWAgMAAABnZYq0AAAACVBMVEUAAB38%2FPz%2F%2F%2F%2Bm8P%2F9AAAAAXRSTlMAQObYZgAAAAFiS0dEAIgFHUgAAAAJcEhZcwAACxMAAAsTAQCanBgAAAAHdElNRQfhBxwQJhxy2iqrAAABoElEQVRIx7WWzdGEIAyGgcMeKMESrMJ6rILZCiiBg4eYKr%2Fd1ZAfgXFm98sJfAyGNwno3G9sLucgYGpQ4OGVRxQTREMDZjF7ILSWjoiHo1n%2BE03Aw8p7CNY5IhkYd%2F%2F6MtO3f8BNhR1QWnarCH4tr6myl0cWgUVNcfMcXACP1hKrGMt8wcAyxide7Ymcgqale7hN6846uJCkQxw6GG7h2MH4Czz3cLqD1zHu0VOXMfZjHLoYvsdd0Q7ZvsOkafJ1P4QXxrWFd14wMc60h8JKCbyQvImzlFjyGoZTKzohwWR2UzSONHhYXBQOaKKsySsahwGGDnb%2FiYPJw22sCqzirSULYy1qtHhXGbtgrM0oagBV4XiTJok3GoLoDNH8ooTmBm7ZMsbpFzi2bgPGoXWXME6XT%2BRJ4GLddxJ4PpQy7tmfoU2HPN6cKg%2BledKHBKlF8oNSt5w5g5o8eXhu1IOlpl5kGerDxIVT%2BztzKepulD8utXqpChamkzzuo7xYGk%2FkpSYuviLXun5bzdRf0Krejzqyz7Z3p0I1v2d6HmA07dofmS48njAiuMgAAAAASUVORK5CYII%3D"></a>
<a href="http://files.minecraftforge.net/maven/net/minecraftforge/forge/index_1.12.2.html"><img src="https://img.shields.io/badge/Forge-1.12.2--14.23.5.2836-brightgreen.svg?colorB=26303d"></a>

Atom is a Minecraft server core created by AtomicInteger which is based on [MinecraftForge](https://github.com/MinecraftForge/MinecraftForge) and [CraftBukkit](https://hub.spigotmc.org/stash/projects/SPIGOT/repos/craftbukkit/browse) for 1.12.2 version of Minecraft.  
Also some parts of the code are taken from such project as [Spigot](https://hub.spigotmc.org/stash/projects/SPIGOT/repos/spigot/browse)
and its derivatives.

The Original code is from https://gitlab.com/divinecode/atom/Atom

Our main goals are:
1. Stable work with both Bukkit plugins and MinecraftForge mods support.
2. Highest performance possible.

## Installation
The Atom latest version can be dowloaded on the following page: https://gitlab.com/divinecode/atom/Atom/pipelines.  
Once the download is completed you have to follow the next steps:
1. Create a directory, where your future server will be located.
2. Put a jar file which ends with `-server.jar` and a `libraries` folder from artifacts.zip to this directory.
3. Launch the core using `java -jar` command.  
    Example (You may add optional flags): `java -jar Atom-*-*-server.jar`

## Building
- `git clone https://gitlab.com/divinecode/atom/Atom.git`
- `git submodule update --init --recursive`
- `gradlew build`

## Getting Help
Should you have any questions or need any assistance please do not hesitate to join our [Discord server](https://discord.gg/Fm5qQDV).

## Contributing
You're always more than welcome to send pull requests and raise issues.

