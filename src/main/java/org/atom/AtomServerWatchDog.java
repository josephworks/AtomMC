package org.atom;

import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;

public class AtomServerWatchDog extends Thread {
    private static AtomServerWatchDog instance;
    private DedicatedServer dedicatedServer;
    private static long lastTickTime;

    public static void startWatchDog(DedicatedServer dedicatedServer) throws IllegalAccessException {
        new AtomServerWatchDog(dedicatedServer).start();
    }

    private AtomServerWatchDog(DedicatedServer dedicatedServer) throws IllegalAccessException {
        super("Atom Server WatchDog");
        if (instance != null) {
            throw new IllegalAccessException();
        }
        this.dedicatedServer = dedicatedServer;
        AtomServerWatchDog.instance = this;
        AtomServerWatchDog.lastTickTime = System.currentTimeMillis();
    }

    public static void updateTickTime() {
        AtomServerWatchDog.lastTickTime = System.currentTimeMillis();
    }

    @Override
    public void run() {
        while (dedicatedServer.isServerRunning() && !dedicatedServer.isServerStopped()) {
            if ((System.currentTimeMillis() - AtomServerWatchDog.lastTickTime) > dedicatedServer.getMaxTickTime()) {
                Logger logger = DedicatedServer.getLOGGER();
                logger.error("Server has stopped responding over {} seconds", dedicatedServer.getMaxTickTime() / 1000);
                logger.info("Main server thread information");
                ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
                ThreadInfo serverThread = threadMXBean.getThreadInfo(dedicatedServer.getServerThread().getId());
                for (StackTraceElement stackTraceElement : serverThread.getStackTrace()) {
                    logger.info(stackTraceElement.toString());
                }
                logger.info("All Thread Information");
                threadMXBean.dumpAllThreads(true, true);

                logger.info("Plugin Info");
                Arrays.stream(Bukkit.getPluginManager().getPlugins())
                        .filter(Plugin::isEnabled)
                        .forEach(plugin -> {
                            logger.info("------------------------------------");
                            logger.info("Plugin Name: " + plugin.getName());
                            logger.info("Plugin Main Class:" + plugin.getDescription().getMain());
                        });
                logger.info("------------------------------------");
                logger.info("Mod Info");
                Loader.instance().getActiveModList().forEach(modContainer -> {
                    logger.info("------------------------------------");
                    logger.info("Mod Name: " + modContainer.getName());
                    logger.info("ModId: " + modContainer.getModId());
                    logger.info("Mod Version: " + modContainer.getVersion());
                });
                logger.info("------------------------------------");
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    interrupt();
                }

            }
        }
    }
}
