package org.atom.remapper;

import net.md_5.specialsource.JarMapping;
import net.md_5.specialsource.JarRemapper;
import net.md_5.specialsource.provider.ClassLoaderProvider;
import net.md_5.specialsource.provider.InheritanceProvider;
import net.md_5.specialsource.provider.JointProvider;
import net.md_5.specialsource.repo.ClassRepo;
import net.md_5.specialsource.repo.RuntimeRepo;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.atom.AtomServerCore;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.util.HashMap;
import java.util.Map;

public class AtomURLClassLoader extends URLClassLoader {

    private JarMapping jarMapping;
    private JarRemapper remapper;
    private final Map<String, Class<?>> classes;

    public AtomURLClassLoader(final URL[] urls, final ClassLoader parent) {
        super(urls, parent);
        this.jarMapping = MappingLoader.loadMapping();
        final JointProvider provider = new JointProvider();
        provider.add((InheritanceProvider) new ClassInheritanceProvider());
        provider.add((InheritanceProvider) new ClassLoaderProvider((ClassLoader) this));
        this.jarMapping.setFallbackInheritanceProvider((InheritanceProvider) provider);
        this.remapper = new AtomRemapper(this.jarMapping);
        this.classes = new HashMap<String, Class<?>>();
    }

    public AtomURLClassLoader(final URL[] urls) {
        super(urls);
        this.jarMapping = MappingLoader.loadMapping();
        final JointProvider provider = new JointProvider();
        provider.add((InheritanceProvider) new ClassInheritanceProvider());
        provider.add((InheritanceProvider) new ClassLoaderProvider((ClassLoader) this));
        this.jarMapping.setFallbackInheritanceProvider((InheritanceProvider) provider);
        this.remapper = new AtomRemapper(this.jarMapping);
        this.classes = new HashMap<String, Class<?>>();
    }

    public AtomURLClassLoader(final URL[] urls, final ClassLoader parent, final URLStreamHandlerFactory factory) {
        super(urls, parent, factory);
        this.jarMapping = MappingLoader.loadMapping();
        final JointProvider provider = new JointProvider();
        provider.add((InheritanceProvider) new ClassInheritanceProvider());
        provider.add((InheritanceProvider) new ClassLoaderProvider((ClassLoader) this));
        this.jarMapping.setFallbackInheritanceProvider((InheritanceProvider) provider);
        this.remapper = new AtomRemapper(this.jarMapping);
        this.classes = new HashMap<String, Class<?>>();
    }

    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
        return this.findClass(name, true);
    }

    private Class<?> findClass(final String name, final boolean checkGlobal) throws ClassNotFoundException {
        if (name.startsWith("net.minecraft.server." + AtomServerCore.getNativeVersion())) {
            final String remappedClass = this.jarMapping.classes.get(name.replaceAll("\\.", "\\/"));
            final Class<?> clazz = (Class<?>) ((LaunchClassLoader) FMLCommonHandler.instance().getMinecraftServerInstance().getClass().getClassLoader()).findClass(remappedClass);
            return clazz;
        }
        Class<?> result = this.classes.get(name);
        synchronized (name.intern()) {
            if (result == null) {
                result = this.remappedFindClass(name);
                if (result != null) {
                    this.setClass(name, result);
                }
                if (result == null) {
                    try {
                        result = super.findClass(name);
                    } catch (ClassNotFoundException e) {
                        result = (Class<?>) ((LaunchClassLoader) FMLCommonHandler.instance().getMinecraftServerInstance().getClass().getClassLoader()).findClass(name);
                    }
                }
                if (result == null) {
                    throw new ClassNotFoundException(name);
                }
                this.classes.put(name, result);
            }
        }
        return result;
    }

    private Class<?> remappedFindClass(final String name) throws ClassNotFoundException {
        Class<?> result = null;
        try {
            final String path = name.replace('.', '/').concat(".class");
            final URL url = this.findResource(path);
            if (url != null) {
                final InputStream stream = url.openStream();
                if (stream != null) {
                    byte[] bytecode = this.remapper.remapClassFile(stream, (ClassRepo) RuntimeRepo.getInstance());
                    bytecode = ReflectionTransformer.transform(bytecode);
                    final JarURLConnection jarURLConnection = (JarURLConnection) url.openConnection();
                    final URL jarURL = jarURLConnection.getJarFileURL();
                    final CodeSource codeSource = new CodeSource(jarURL, new CodeSigner[0]);
                    result = this.defineClass(name, bytecode, 0, bytecode.length, codeSource);
                    if (result != null) {
                        this.resolveClass(result);
                    }
                }
            }
        } catch (Throwable t) {
            throw new ClassNotFoundException("Failed to remap class " + name, t);
        }
        return result;
    }

    void setClass(final String name, final Class<?> clazz) {
        if (!this.classes.containsKey(name)) {
            this.classes.put(name, clazz);
            if (ConfigurationSerializable.class.isAssignableFrom(clazz)) {
                final Class<? extends ConfigurationSerializable> serializable = clazz.asSubclass(ConfigurationSerializable.class);
                ConfigurationSerialization.registerClass((Class) serializable);
            }
        }
    }
}
