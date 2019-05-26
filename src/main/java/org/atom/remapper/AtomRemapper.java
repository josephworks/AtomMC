package org.atom.remapper;

import net.md_5.specialsource.JarMapping;
import net.md_5.specialsource.JarRemapper;

public class AtomRemapper extends JarRemapper {

    public AtomRemapper(JarMapping jarMapping) {
        super(jarMapping);
    }

    public String mapSignature(String signature, boolean typeSignature) {
        try {
            return super.mapSignature(signature, typeSignature);
        } catch (Exception e) {
            return signature;
        }
    }

    public String mapFieldName(final String owner, final String name, final String desc, final int access) {
        return super.mapFieldName(owner, name, desc, -1);
    }
}
