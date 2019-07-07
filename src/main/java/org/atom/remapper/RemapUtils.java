package org.atom.remapper;

import net.md_5.specialsource.JarRemapper;
import org.objectweb.asm.Type;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;

public class RemapUtils {
    private static final String NMS_PREFIX = "net/minecraft/server/";
    public static final String NMS_VERSION = "v1_12_R1";

    // Classes
    static String reverseMapExternal(Class<?> name) {
        return reverseMap(name).replace('$', '.').replace('/', '.');
    }

    static String reverseMap(Class<?> name) {
        return reverseMap(Type.getInternalName(name));
    }

    static String reverseMap(String check) {
        return ReflectionTransformer.classDeMapping.getOrDefault(check, check);
    }

    // Methods
    static String mapMethod(Class<?> inst, String name, Class<?>... parameterTypes) {
        String result = mapMethodInternal(inst, name, parameterTypes);
        if (result != null) {
            return result;
        }
        return name;
    }

    /**
     * Recursive method for finding a method from superclasses/interfaces
     */
    private static String mapMethodInternal(final Class<?> inst, final String name, final Class<?>... parameterTypes) {
        final String match = reverseMap(inst) + "/" + name;
        final Collection<String> colls = ReflectionTransformer.methodFastMapping.get(match);
        for (final String value : colls) {
            final String[] str = value.split("\\s+");
            int i = 0;
            for (final Type type : Type.getArgumentTypes(str[1])) {
                final String typename = (type.getSort() == 9) ? type.getInternalName() : type.getClassName();
                if (i >= parameterTypes.length || !typename.equals(reverseMapExternal(parameterTypes[i]))) {
                    i = -1;
                    break;
                }
                ++i;
            }
            if (i >= parameterTypes.length) {
                return ReflectionTransformer.jarMapping.methods.get(value);
            }
        }
        final Class<?> superClass = inst.getSuperclass();
        if (superClass != null) {
            final String superMethodName = mapMethodInternal(superClass, name, parameterTypes);
            if (superMethodName != null) {
                return superMethodName;
            }
        }
        for (final Class<?> interfaceClass : inst.getInterfaces()) {
            final String superMethodName2 = mapMethodInternal(interfaceClass, name, parameterTypes);
            if (superMethodName2 != null) {
                return superMethodName2;
            }
        }
        return null;
    }

    static String mapFieldName(final Class<?> inst, final String name) {
        final String key = reverseMap(inst) + "/" + name;
        String mapped = ReflectionTransformer.jarMapping.fields.get(key);
        if (mapped == null) {
            final Class<?> superClass = inst.getSuperclass();
            if (superClass != null) {
                mapped = mapFieldName(superClass, name);
            }
        }
        return (mapped != null) ? mapped : name;
    }

    static String mapClass(String className) {
        String tRemapped = JarRemapper.mapTypeName(className, ReflectionTransformer.jarMapping.packages, ReflectionTransformer.jarMapping.classes, className);
        if (tRemapped.equals(className) && className.startsWith(NMS_PREFIX) && !className.contains(NMS_VERSION)) {
            String tNewClassStr = NMS_PREFIX + NMS_VERSION + "/" + className.substring(NMS_PREFIX.length());
            return JarRemapper.mapTypeName(tNewClassStr, ReflectionTransformer.jarMapping.packages, ReflectionTransformer.jarMapping.classes, className);
        }
        return tRemapped;
    }

    static String demapFieldName(Field field) {
        String name = field.getName();
        String match = reverseMap(field.getDeclaringClass());

        Collection<String> colls = ReflectionTransformer.fieldDeMapping.get(name);

        for (String value : colls) {
            if (value.startsWith(match)) {
                String[] matched = value.split("/");
                return matched[matched.length - 1];
            }
        }

        return name;
    }

    static String demapMethodName(Method method) {
        String name = method.getName();
        String match = reverseMap(method.getDeclaringClass());

        Collection<String> colls = ReflectionTransformer.methodDeMapping.get(name);

        for (String value : colls) {
            if (value.startsWith(match)) {
                String[] matched = value.split("\\s+")[0].split("/");
                return matched[matched.length - 1];
            }
        }

        return name;
    }

    static boolean isClassNeedRemap(Class<?> clazz, final boolean checkSuperClass) {
        while (clazz != null && clazz.getClassLoader() != null) {
            if (clazz.getName().startsWith("net.minecraft.")) {
                return true;
            }
            if (!checkSuperClass) {
                return false;
            }
            for (final Class<?> interfaceClass : clazz.getInterfaces()) {
                if (isClassNeedRemap(interfaceClass, true)) {
                    return true;
                }
            }
            clazz = clazz.getSuperclass();
        }
        return false;
    }
}
