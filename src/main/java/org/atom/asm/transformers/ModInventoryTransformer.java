package org.atom.asm.transformers;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.util.Set;

import static org.objectweb.asm.Opcodes.*;

/**
 * This transformer modifies all classes that implement {@link net.minecraft.inventory.IInventory} and its derivatives.
 * The essence of transformation is {@link org.atom.asm.IInventoryTransactionProvider} implementation, so BukkitAPI can
 * interoperate with all inventories properly, even if they are Forge inventories.
 *
 * @see org.atom.asm.IInventoryTransactionProvider
 */

public class ModInventoryTransformer implements IClassTransformer {

    // This blacklist represents classes we modify manually
    private final Set<String> blackList = ImmutableSet.of("net.minecraft.inventory.InventoryLargeChest", "tu");

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) {
            return basicClass;
        }
        if (!blackList.contains(transformedName) && implementsIInventory(basicClass)) {
            return transformClass(basicClass);
        }
        return basicClass;
    }

    private boolean implementsIInventory(byte[] basicClass) {
        final boolean[] doesClassImplementIInventory = {false};
        ClassReader classReader = new ClassReader(basicClass);
        final ClassVisitor classVisitor = new ClassVisitor(ASM5) {
            @Override
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                Set<String> allInterfaces = Sets.newHashSet();
                getAllInterfaces(name, allInterfaces);
                doesClassImplementIInventory[0] = allInterfaces.stream().anyMatch(interfaceName -> interfaceName.equals("tv") || interfaceName.equals("net/minecraft/inventory/IInventory")) && (access & ACC_INTERFACE) == 0;
                super.visit(version, access, name, signature, superName, interfaces);
            }

            private void getAllInterfaces(String baseInterfaceName, Set<String> result) {
                try {
                    ClassReader interfaceClassReader = new ClassReader(baseInterfaceName);
                    result.add(baseInterfaceName);
                    for (String interfaceName : interfaceClassReader.getInterfaces())
                        getAllInterfaces(interfaceName, result);
                } catch (IOException ignore) {

                }
            }
        };
        classReader.accept(classVisitor, 0);
        return doesClassImplementIInventory[0];
    }

    private byte[] transformClass(byte[] basicClass) {
        ClassNode classNode = new ClassNode();
        new ClassReader(basicClass).accept(classNode, 0);
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);

        // implements IInventoryTransactionProvider
        classNode.interfaces.add("org/atom/asm/IInventoryTransactionProvider");

        // private List<HumanEntity> transaction;
        classWriter.visitField(ACC_PRIVATE, "transaction", "Ljava/util/List;", "Ljava/util/List<Lorg/bukkit/entity/HumanEntity;>;", null).visitEnd();

        /*
         * public void onOpen(CraftHumanEntity craftHumanEntity) {
         *     if (this.transaction == null)
         *         this.transaction = new ArrayList<HumanEntity>(1);
         *     this.transaction.add((HumanEntity)craftHumanEntity);
         * }
         */
        MethodVisitor mv = classWriter.visitMethod(ACC_PUBLIC, "onOpen", "(Lorg/bukkit/craftbukkit/v1_12_R1/entity/CraftHumanEntity;)V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, classNode.name, "transaction", "Ljava/util/List;");
        Label l1 = new Label();
        mv.visitJumpInsn(IFNONNULL, l1);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitTypeInsn(NEW, "java/util/ArrayList");
        mv.visitInsn(DUP);
        mv.visitInsn(ICONST_1);
        mv.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "(I)V", false);
        mv.visitFieldInsn(PUTFIELD, classNode.name, "transaction", "Ljava/util/List;");
        mv.visitLabel(l1);
        mv.visitFrame(F_SAME, 0, null, 0, null);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, classNode.name, "transaction", "Ljava/util/List;");
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", true);
        mv.visitInsn(POP);
        mv.visitInsn(RETURN);

        /*
         * public void onClose(CraftHumanEntity craftHumanEntity) {
         *    if (this.transaction != null) {
         *        this.transaction.remove(craftHumanEntity);
         *        if (this.transaction.isEmpty())
         *            this.transaction = null;
         *    }
         * }
         */
        mv = classWriter.visitMethod(ACC_PUBLIC, "onClose", "(Lorg/bukkit/craftbukkit/v1_12_R1/entity/CraftHumanEntity;)V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, classNode.name, "transaction", "Ljava/util/List;");
        Label t1 = new Label();
        mv.visitJumpInsn(IFNULL, t1);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, classNode.name, "transaction", "Ljava/util/List;");
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "remove", "(Ljava/lang/Object;)Z", true);
        mv.visitInsn(POP);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, classNode.name, "transaction", "Ljava/util/List;");
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "isEmpty", "()Z", true);
        mv.visitJumpInsn(IFEQ, t1);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(ACONST_NULL);
        mv.visitFieldInsn(PUTFIELD, classNode.name, "transaction", "Ljava/util/List;");
        mv.visitLabel(t1);
        mv.visitFrame(F_SAME, 0, null, 0, null);
        mv.visitInsn(RETURN);
        mv.visitEnd();

        /*
         * public List<HumanEntity> getViewers() {
         *     if (this.transaction == null)
         *         return Collections.emptyList();
         *     return this.transaction;
         * }
         */
        mv = classWriter.visitMethod(ACC_PUBLIC, "getViewers", "()Ljava/util/List;", "()Ljava/util/List<Lorg/bukkit/entity/HumanEntity;>;", null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, classNode.name, "transaction", "Ljava/util/List;");
        Label k1 = new Label();
        mv.visitJumpInsn(IFNONNULL, k1);
        mv.visitMethodInsn(INVOKESTATIC, "java/util/Collections", "emptyList", "()Ljava/util/List;", false);
        mv.visitInsn(ARETURN);
        mv.visitLabel(k1);
        mv.visitFrame(F_SAME, 0, null, 0, null);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, classNode.name, "transaction", "Ljava/util/List;");
        mv.visitInsn(ARETURN);
        mv.visitEnd();

        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }
}
