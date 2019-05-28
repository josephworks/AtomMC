package org.atom.remapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

public class HandleLookup {
    private static HashMap<String, String> map;

    public static MethodHandle findSpecial(final MethodHandles.Lookup lookup, final Class<?> refc, String name, final MethodType type, final Class<?> specialCaller) throws NoSuchMethodException, IllegalAccessException {
        if (refc.getName().startsWith("net.minecraft.")) {
            name = RemapUtils.mapMethod(refc, name, type.parameterArray());
        }
        return lookup.findSpecial(refc, name, type, specialCaller);
    }

    public static MethodHandle findVirtual(final MethodHandles.Lookup lookup, final Class<?> refc, String name, final MethodType oldType) throws NoSuchMethodException, IllegalAccessException {
        if (refc.getName().startsWith("net.minecraft.")) {
            name = RemapUtils.mapMethod(refc, name, oldType.parameterArray());
        } else if (refc.getName().equals("java.lang.Class") || refc.getName().equals("java.lang.ClassLoader")) {
            final String s = name;
            switch (s) {
                case "getField":
                case "getDeclaredField":
                case "getMethod":
                case "getDeclaredMethod":
                case "getSimpleName":
                case "getName":
                case "loadClass": {
                    final Class<?>[] newParArr = (Class<?>[]) new Class[oldType.parameterArray().length + 1];
                    if (refc.getName().equals("java.lang.Class")) {
                        newParArr[0] = Class.class;
                    } else {
                        newParArr[0] = ClassLoader.class;
                    }
                    System.arraycopy(oldType.parameterArray(), 0, newParArr, 1, oldType.parameterArray().length);
                    final MethodType newType = MethodType.methodType(oldType.returnType(), newParArr);
                    final MethodHandle handle = lookup.findStatic(ReflectionMethods.class, name, newType);
                    return handle;
                }
            }
        }
        return lookup.findVirtual(refc, name, oldType);
    }

    public static MethodHandle findStatic(final MethodHandles.Lookup lookup, Class<?> refc, String name, final MethodType type) throws NoSuchMethodException, IllegalAccessException {
        if (refc.getName().startsWith("net.minecraft.")) {
            name = RemapUtils.mapMethod(refc, name, type.parameterArray());
        } else if (refc.getName().equals("java.lang.Class") && name.equals("forName")) {
            refc = ReflectionMethods.class;
        }
        return lookup.findStatic(refc, name, type);
    }

    public static MethodType fromMethodDescriptorString(final String descriptor, final ClassLoader loader) {
        final String remapDesc = HandleLookup.map.getOrDefault(descriptor, descriptor);
        return MethodType.fromMethodDescriptorString(remapDesc, loader);
    }

    public static MethodHandle unreflect(final MethodHandles.Lookup lookup, final Method m) throws IllegalAccessException {
        if (m.getDeclaringClass().getName().equals("java.lang.Class")) {
            final String name = m.getName();
            switch (name) {
                case "forName": {
                    return getClassReflectionMethod(lookup, m.getName(), String.class);
                }
                case "getField":
                case "getDeclaredField": {
                    return getClassReflectionMethod(lookup, m.getName(), Class.class, String.class);
                }
                case "getMethod":
                case "getDeclaredMethod": {
                    return getClassReflectionMethod(lookup, m.getName(), Class.class, String.class, Class[].class);
                }
                case "getSimpleName": {
                    return getClassReflectionMethod(lookup, m.getName(), Class.class);
                }
            }
        } else if (m.getName().equals("getName")) {
            if (m.getDeclaringClass().getName().equals("java.lang.reflect.Field")) {
                return getClassReflectionMethod(lookup, m.getName(), Field.class);
            }
            if (m.getDeclaringClass().getName().equals("java.lang.reflect.Method")) {
                return getClassReflectionMethod(lookup, m.getName(), Method.class);
            }
        } else if (m.getName().equals("loadClass") && m.getDeclaringClass().getName().equals("java.lang.ClassLoader")) {
            return getClassReflectionMethod(lookup, m.getName(), ClassLoader.class, String.class);
        }
        return lookup.unreflect(m);
    }

    private static MethodHandle getClassReflectionMethod(final MethodHandles.Lookup lookup, final String name, final Class<?>... p) {
        try {
            return lookup.unreflect(ReflectionMethods.class.getMethod(name, p));
        } catch (NoSuchMethodException | IllegalAccessException ex2) {
            ex2.printStackTrace();
            return null;
        }
    }

    public static void loadMappings(final BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            final int commentIndex = line.indexOf(35);
            if (commentIndex != -1) {
                line = line.substring(0, commentIndex);
            }
            if (!line.isEmpty()) {
                if (!line.startsWith("MD: ")) {
                    continue;
                }
                final String[] sp = line.split("\\s+");
                final String firDesc = sp[2];
                final String secDesc = sp[4];
                HandleLookup.map.put(firDesc, secDesc);
            }
        }
    }

    static {
        HandleLookup.map = new HashMap<String, String>();
    }
}