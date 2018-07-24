package org.atom.runner;

import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.common.launcher.FMLServerTweaker;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerScheduler;
import org.junit.runners.model.TestClass;
import org.spongepowered.asm.launch.MixinTweaker;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MinecraftServerRunner extends BlockJUnit4ClassRunner {

    public MinecraftServerRunner(Class<?> classToRun) throws InitializationError {
        super(classToRun);
    }

    static {
        Launch.main(new String[]{
                "--tweakClass", FMLServerTweaker.class.getCanonicalName(),
                "--tweakClass", MixinTweaker.class.getCanonicalName(),
                "--mixin", "mixins.atom.test.json",
                "nogui",
        });
        LaunchClassLoader lcl = Launch.classLoader;
        lcl.addClassLoaderExclusion("org.junit.");
    }

    @Override
    public void run(final RunNotifier notifier) {
        final List<Runnable> runnableTasks = new ArrayList<>();

        setScheduler(new RunnerScheduler() {
            @Override
            public void schedule(Runnable childStatement) {
                runnableTasks.add(childStatement);
            }

            @Override
            public void finished() {

            }
        });

        super.run(notifier);

        if (runnableTasks.isEmpty())
            return;

        addTask(() -> {
            for (Runnable runnable : runnableTasks)
                runnable.run();
        });

        addTask(() -> { });
        addTask(() -> { }); // awaits previous task
    }

    private void addTask(Runnable task) {
        try {
            LaunchClassLoader lcl = Launch.classLoader;
            Class<?> mod = lcl.findClass("org.atom.runner.TestRunnerMod");
            Method addTaskMethod = mod.getDeclaredMethod("addTask", Runnable.class);
            addTaskMethod.invoke(null, task);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected TestClass createTestClass(Class<?> testClass) {
        try {
            return super.createTestClass(Launch.classLoader.findClass(testClass.getName()));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
