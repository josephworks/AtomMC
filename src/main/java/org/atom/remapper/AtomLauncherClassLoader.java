package org.atom.remapper;

import net.minecraft.launchwrapper.LaunchClassLoader;

import java.net.URL;

public class AtomLauncherClassLoader extends LaunchClassLoader {
    public AtomLauncherClassLoader(URL[] sources) {
        super(sources);
    }

    private static String getPackageName(Class<?> clazz) {
        String name = clazz.getName();
        int i = name.lastIndexOf('.');
        if (i != -1) {
            return name.substring(0, i);
        } else {
            return null;
        }
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        Class<?> aClass = super.findClass(name);
        if (aClass != null && aClass.getPackage() == null && name.startsWith("net.minecraft.server")) {
            String packageName = getPackageName(aClass);
            if (packageName != null) {
                definePackage(packageName, null, null, null, null, null, null, null);
            }
        }
        return aClass;
    }
}
