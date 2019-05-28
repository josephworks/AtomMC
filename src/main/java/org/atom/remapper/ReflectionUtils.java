package org.atom.remapper;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class ReflectionUtils {
    private static SecurityManager sm;

    private static Class<?> getCallerClass(final int skip) {
        return ReflectionUtils.sm.getCallerClass(skip);
    }

    static ClassLoader getCallerClassloader() {
        return getCallerClass(3).getClassLoader();
    }

    public static Class<?>[] getStackClass() {
        return ReflectionUtils.sm.getStackClass();
    }

    public static Unsafe getUnsafe() {
        try {
            final Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            return (Unsafe) field.get(null);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return null;
        }
    }

    static {
        ReflectionUtils.sm = new SecurityManager();
    }

    static class SecurityManager extends java.lang.SecurityManager {
        Class<?> getCallerClass(final int skip) {
            return (Class<?>) this.getClassContext()[skip + 1];
        }

        Class<?>[] getStackClass() {
            return (Class<?>[]) this.getClassContext();
        }
    }
}
