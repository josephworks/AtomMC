package org.atom.remapper;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import net.md_5.specialsource.JarMapping;
import net.md_5.specialsource.provider.InheritanceProvider;
import net.md_5.specialsource.provider.JointProvider;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ListIterator;

public class ReflectionTransformer {

    public static final String DESC_ReflectionMethods;
    public static final String DESC_RemapMethodHandle;


    public static JarMapping jarMapping;
    public static AtomRemapper remapper;

    public static final HashMap<String, String> classDeMapping = Maps.newHashMap();
    public static final Multimap<String, String> methodDeMapping = ArrayListMultimap.create();
    public static final Multimap<String, String> fieldDeMapping = ArrayListMultimap.create();
    public static final Multimap<String, String> methodFastMapping = ArrayListMultimap.create();

    private static boolean disable = false;

    public static void init() {
        try {
            ReflectionUtils.getCallerClassloader();
        } catch (Throwable e) {
            new RuntimeException("Unsupported Java version, disabled reflection remap!", e).printStackTrace();
            ReflectionTransformer.disable = true;
        }
        ReflectionTransformer.jarMapping = MappingLoader.loadMapping();
        final JointProvider provider = new JointProvider();
        provider.add((InheritanceProvider) new ClassInheritanceProvider());
        ReflectionTransformer.jarMapping.setFallbackInheritanceProvider((InheritanceProvider) provider);
        ReflectionTransformer.remapper = new AtomRemapper(ReflectionTransformer.jarMapping);
        final String[] s = new String[1];
        ReflectionTransformer.jarMapping.classes.forEach((k, v) -> s[0] = ReflectionTransformer.classDeMapping.put(v, k));
        ReflectionTransformer.jarMapping.methods.forEach((k, v) -> ReflectionTransformer.methodDeMapping.put((String) v, (String) k));
        ReflectionTransformer.jarMapping.fields.forEach((k, v) -> ReflectionTransformer.fieldDeMapping.put((String) v, (String) k));
        ReflectionTransformer.jarMapping.methods.forEach((k, v) -> ReflectionTransformer.methodFastMapping.put((String) k.split("\\s+")[0], (String) k));
    }

    /**
     * Convert code from using Class.X methods to our remapped versions
     */
    public static byte[] transform(byte[] code) {
        ClassReader reader = new ClassReader(code); // Turn from bytes into visitor
        ClassNode node = new ClassNode();
        reader.accept(node, 0); // Visit using ClassNode
        boolean remapCL = false;
        if (node.superName.equals("java/net/URLClassLoader")) {
            node.superName = "org/atom/remapper/AtomURLClassLoader";
            remapCL = true;
        }

        for (MethodNode method : node.methods) { // Taken from SpecialSource
            ListIterator<AbstractInsnNode> insnIterator = method.instructions.iterator();
            while (insnIterator.hasNext()) {
                AbstractInsnNode next = insnIterator.next();

                if (next instanceof TypeInsnNode) {
                    TypeInsnNode insn = (TypeInsnNode) next;
                    if (insn.getOpcode() == Opcodes.NEW && insn.desc.equals("java/net/URLClassLoader")) { // remap new URLClassLoader
                        insn.desc = "org/atom/remapper/AtomURLClassLoader";
                        remapCL = true;
                    }
                }

                if (!(next instanceof MethodInsnNode)) continue;
                MethodInsnNode insn = (MethodInsnNode) next;
                switch (insn.getOpcode()) {
                    case Opcodes.INVOKEVIRTUAL:
                        remapVirtual(insn);
                        break;
                    case Opcodes.INVOKESTATIC:
                        remapForName(insn);
                        break;
                    case Opcodes.INVOKESPECIAL:
                        if (remapCL) remapURLClassLoader(insn);
                        break;
                }

                if (insn.owner.equals("javax/script/ScriptEngineManager") && insn.desc.equals("()V") && insn.name.equals("<init>")) {
                    insn.desc = "(Ljava/lang/ClassLoader;)V";
                    method.instructions.insertBefore(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/ClassLoader", "getSystemClassLoader", "()Ljava/lang/ClassLoader;"));
                    method.maxStack++;
                }
            }
        }

        ClassWriter writer = new ClassWriter(0/* ClassWriter.COMPUTE_FRAMES */);
        node.accept(writer); // Convert back into bytes
        return writer.toByteArray();
    }


    public static void remapForName(final AbstractInsnNode insn) {
        final MethodInsnNode method = (MethodInsnNode) insn;
        if (method.owner.equals("java/lang/invoke/MethodType") && method.name.equals("fromMethodDescriptorString")) {
            method.owner = ReflectionTransformer.DESC_RemapMethodHandle;
        }
        if (ReflectionTransformer.disable || !method.owner.equals("java/lang/Class") || !method.name.equals("forName")) {
            return;
        }
        method.owner = ReflectionTransformer.DESC_ReflectionMethods;
    }

    public static void remapVirtual(final AbstractInsnNode insn) {
        final MethodInsnNode method = (MethodInsnNode) insn;
        boolean remapFlag = false;
        if (method.owner.equals("java/lang/Class")) {
            final String name = method.name;
            switch (name) {
                case "getField":
                case "getDeclaredField":
                case "getMethod":
                case "getDeclaredMethod":
                case "getSimpleName": {
                    remapFlag = true;
                    break;
                }
            }
        } else if (method.name.equals("getName")) {
            final String owner = method.owner;
            switch (owner) {
                case "java/lang/reflect/Field":
                case "java/lang/reflect/Method": {
                    remapFlag = true;
                    break;
                }
            }
        } else if (method.owner.equals("java/lang/ClassLoader") && method.name.equals("loadClass")) {
            remapFlag = true;
        } else if (method.owner.toLowerCase().endsWith("classloader") && method.name.equals("defineClass")) {
            remapFlag = true;
        } else if (method.owner.equals("java/lang/invoke/MethodHandles$Lookup")) {
            final String name2 = method.name;
            switch (name2) {
                case "findVirtual":
                case "findStatic":
                case "findSpecial":
                case "unreflect": {
                    virtualToStatic(method, ReflectionTransformer.DESC_RemapMethodHandle);
                    break;
                }
            }
        }
        if (remapFlag) {
            virtualToStatic(method, ReflectionTransformer.DESC_ReflectionMethods);
        }
    }

    private static void remapURLClassLoader(final MethodInsnNode method) {
        if (!method.owner.equals("java/net/URLClassLoader") || !method.name.equals("<init>")) {
            return;
        }
        method.owner = "org/atom/remapper/AtomURLClassLoader";
    }

    private static void virtualToStatic(final MethodInsnNode method, final String desc) {
        final Type returnType = Type.getReturnType(method.desc);
        final ArrayList<Type> args = new ArrayList<Type>();
        if (method.owner.toLowerCase().endsWith("classloader")) {
            args.add(Type.getObjectType("java/lang/ClassLoader"));
        } else {
            args.add(Type.getObjectType(method.owner));
        }
        args.addAll(Arrays.asList(Type.getArgumentTypes(method.desc)));
        method.setOpcode(184);
        method.owner = desc;
        method.desc = Type.getMethodDescriptor(returnType, (Type[]) args.toArray(new Type[0]));
    }

    static {
        DESC_ReflectionMethods = Type.getInternalName((Class) ReflectionMethods.class);
        DESC_RemapMethodHandle = Type.getInternalName((Class) HandleLookup.class);
        ReflectionTransformer.disable = false;
    }
}
