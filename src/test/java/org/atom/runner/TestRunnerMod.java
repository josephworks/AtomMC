package org.atom.runner;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
@Mod(modid = "testrunnermod", name = "TestRunnerMod", version = "1.0.0", acceptableRemoteVersions = "*")
public class TestRunnerMod {
    private static ArrayBlockingQueue<Runnable> tasks = new ArrayBlockingQueue<>(1);

    public static void addTask(Runnable task) {
        try {
            tasks.put(task);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Mod.EventHandler
    public void serverStarted(FMLServerStartedEvent e) {
        try {
            tasks.take().run();
            while (true) {
                Runnable task = tasks.poll(2000, TimeUnit.MILLISECONDS);
                if (task == null)
                    break;
                task.run();
            }
        } catch (InterruptedException interrupt) {
            interrupt.printStackTrace();
        }
    }
}
