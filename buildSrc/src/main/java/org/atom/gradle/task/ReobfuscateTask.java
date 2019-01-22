package org.atom.gradle.task;

import com.google.common.io.Files;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.md_5.specialsource.Jar;
import net.md_5.specialsource.JarMapping;
import net.md_5.specialsource.JarRemapper;
import net.md_5.specialsource.provider.ClassLoaderProvider;
import net.md_5.specialsource.provider.JarProvider;
import net.md_5.specialsource.provider.JointProvider;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class ReobfuscateTask extends DefaultTask {

    @Getter
    @Setter
    @InputFile
    private File inputJar;

    @Getter
    @Setter
    @InputFile
    private File srg;

    @Getter
    @Setter
    private FileCollection classpath;

    @Getter
    @Setter
    private File outputJar = new File(getProject().getBuildDir(), "localCache/reobfuscated.jar");

    @TaskAction
    void doTask() throws IOException {
        obfuscate(inputJar, classpath, srg);
    }

    private void obfuscate(File inJar, FileCollection classpath, File srg) throws IOException {
        // load mapping
        JarMapping mapping = new JarMapping();
        mapping.loadMappings(Files.newReader(srg, Charset.defaultCharset()), null, null, false);
        // make remapper
        JarRemapper remapper = new JarRemapper(null, mapping);
        // load jar
        URLClassLoader classLoader = null;
        try (Jar input = Jar.init(inJar)) {
            // ensure that inheritance provider is used
            JointProvider inheritanceProviders = new JointProvider();
            inheritanceProviders.add(new JarProvider(input));
            if (classpath != null && !classpath.isEmpty())
                inheritanceProviders.add(new ClassLoaderProvider(classLoader = new URLClassLoader(toUrls(classpath))));
            mapping.setFallbackInheritanceProvider(inheritanceProviders);

            if (!outputJar.getParentFile().exists()) //Needed because SS doesn't create it.
                outputJar.getParentFile().mkdirs();

            remapper.remapJar(input, outputJar);
        } finally {
            if (classLoader != null)
                classLoader.close();
        }
    }

    private static URL[] toUrls(@NonNull FileCollection collection) throws MalformedURLException {
        ArrayList<URL> urls = new ArrayList<>(collection.getFiles().size());
        for (File file : collection.getFiles())
            urls.add(file.toURI().toURL());
        return urls.toArray(new URL[collection.getFiles().size()]);
    }
}
