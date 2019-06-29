package org.atom.remapper;

import net.md_5.specialsource.repo.ClassRepo;
import net.md_5.specialsource.repo.RuntimeRepo;
import org.atom.AtomServer;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.concurrent.ConcurrentHashMap;

public class ReflectionMethods {
    private static final ConcurrentHashMap<String, String> fieldGetNameCache;
    private static final ConcurrentHashMap<String, String> methodGetNameCache;
    private static final ConcurrentHashMap<String, String> simpleNameGetNameCache;
    private static Method defineClassM5;

    public static Class<?> forName(final String className) throws ClassNotFoundException {
        return forName(className, true, ReflectionUtils.getCallerClassloader());
    }

    public static Class<?> forName(String className, final boolean initialize, final ClassLoader classLoader) throws ClassNotFoundException {
        if (className.startsWith("net.minecraft.server." + AtomServer.getNativeVersion())) {
            className = ReflectionTransformer.jarMapping.classes.getOrDefault(className.replace('.', '/'), className).replace('/', '.');
        }
        return Class.forName(className, initialize, classLoader);
    }

    public static Field getField(final Class<?> inst, String name) throws NoSuchFieldException, SecurityException {
        if (RemapUtils.isClassNeedRemap(inst, true)) {
            name = RemapUtils.mapFieldName(inst, name);
        }
        return inst.getField(name);
    }

    public static Field getDeclaredField(final Class<?> inst, String name) throws NoSuchFieldException, SecurityException {
        if (RemapUtils.isClassNeedRemap(inst, false)) {
            name = ReflectionTransformer.remapper.mapFieldName(RemapUtils.reverseMap(inst), name, (String) null);
        }
        return inst.getDeclaredField(name);
    }

    public static Method getMethod(final Class<?> inst, String name, final Class<?>... parameterTypes) throws NoSuchMethodException, SecurityException {
        if (RemapUtils.isClassNeedRemap(inst, true)) {
            name = RemapUtils.mapMethod(inst, name, parameterTypes);
        }
        try {
            return inst.getMethod(name, parameterTypes);
        } catch (NoClassDefFoundError e) {
            throw new NoSuchMethodException(e.toString());
        }
    }

    public static Method getDeclaredMethod(final Class<?> inst, String name, final Class<?>... parameterTypes) throws NoSuchMethodException, SecurityException {
        if (RemapUtils.isClassNeedRemap(inst, true)) {
            name = RemapUtils.mapMethod(inst, name, parameterTypes);
        }
        try {
            return inst.getDeclaredMethod(name, parameterTypes);
        } catch (NoClassDefFoundError e) {
            throw new NoSuchMethodException(e.toString());
        }
    }

    public static String getName(final Field field) {
        if (!RemapUtils.isClassNeedRemap(field.getDeclaringClass(), false)) {
            return field.getName();
        }
        final String hash = String.valueOf(field.hashCode());
        final String cache = ReflectionMethods.fieldGetNameCache.get(hash);
        if (cache != null) {
            return cache;
        }
        final String retn = RemapUtils.demapFieldName(field);
        ReflectionMethods.fieldGetNameCache.put(hash, retn);
        return retn;
    }

    public static String getName(final Method method) {
        if (!RemapUtils.isClassNeedRemap(method.getDeclaringClass(), true)) {
            return method.getName();
        }
        final String hash = String.valueOf(method.hashCode());
        final String cache = ReflectionMethods.methodGetNameCache.get(hash);
        if (cache != null) {
            return cache;
        }
        final String retn = RemapUtils.demapMethodName(method);
        ReflectionMethods.methodGetNameCache.put(hash, retn);
        return retn;
    }

    public static String getSimpleName(final Class<?> inst) {
        if (!RemapUtils.isClassNeedRemap(inst, false)) {
            return inst.getSimpleName();
        }
        final String hash = String.valueOf(inst.hashCode());
        final String cache = ReflectionMethods.simpleNameGetNameCache.get(hash);
        if (cache != null) {
            return cache;
        }
        final String[] name = RemapUtils.reverseMapExternal(inst).split("\\.");
        final String retn = name[name.length - 1];
        ReflectionMethods.simpleNameGetNameCache.put(hash, retn);
        return retn;
    }

    public static Class<?> loadClass(final ClassLoader inst, String className) throws ClassNotFoundException {
        if (className.startsWith("net.minecraft.")) {
            className = RemapUtils.mapClass(className.replace('.', '/')).replace('/', '.');
        }
        return inst.loadClass(className);
    }

    public static Class<?> defineClass(final ClassLoader inst, final String className, final byte[] b, final int off, final int len, final ProtectionDomain protectionDomain) throws ClassFormatError, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        if (ReflectionMethods.defineClassM5 == null) {
            (ReflectionMethods.defineClassM5 = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, Integer.TYPE, Integer.TYPE, ProtectionDomain.class)).setAccessible(true);
        }
        byte[] classBytes = new byte[len];
        System.arraycopy(b, off, classBytes, 0, len);
        classBytes = ReflectionTransformer.remapper.remapClassFile(classBytes, (ClassRepo) RuntimeRepo.getInstance());
        classBytes = ReflectionTransformer.transform(classBytes);
        return (Class<?>) ReflectionMethods.defineClassM5.invoke(inst, RemapUtils.mapClass(className.replace('.', '/')).replace('/', '.'), classBytes, 0, classBytes.length, protectionDomain);
    }

    static {
        fieldGetNameCache = new ConcurrentHashMap<String, String>();
        methodGetNameCache = new ConcurrentHashMap<String, String>();
        simpleNameGetNameCache = new ConcurrentHashMap<String, String>();
    }
}