// This code is from CatServer (Added by the AtomMC Team)
// Mohist is Prohibited from using this code
package org.atom.remapper;

import LZMA.LzmaInputStream;
import net.md_5.specialsource.JarMapping;
import net.md_5.specialsource.transformer.MappingTransformer;
import net.md_5.specialsource.transformer.MethodDescriptor;

import java.io.*;
import java.util.*;

class MappingTools {
    private static JarMapping srgMappings;
    private static JarMapping spigotMappings;
    private static final String classCsrg = "bukkit-1.12.2-cl.csrg";
    private static final String memberCsrg = "bukkit-1.12.2-members.csrg";
    private static final String packageSrg = "package.srg";
    private static final File cacheDir;
    private static StringBuilder mappingsBuilder;
    private static boolean debug;
    private static final String[] addMapping;
    private static final List<String> delMapping;

    static BufferedReader getMappingReader() {
        if (MappingTools.mappingsBuilder == null) {
            final List<String> cb2srg = genCb2Srg();
            MappingTools.mappingsBuilder = new StringBuilder();
            final String NEW_LINE = System.getProperty("line.separator");
            for (final String line : cb2srg) {
                for (final String delLine : MappingTools.delMapping) {
                    final String delObject = new String(Base64.getDecoder().decode(delLine));
                    if (line.equalsIgnoreCase(delObject)) {
                        break;
                    }
                }
                MappingTools.mappingsBuilder.append(line).append(NEW_LINE);
            }
            for (final String addLine : MappingTools.addMapping) {
                MappingTools.mappingsBuilder.append(new String(Base64.getDecoder().decode(addLine))).append(NEW_LINE);
            }
        }
        return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(MappingTools.mappingsBuilder.toString().getBytes())));
    }

    private static JarMapping getSrgMappings() {
        if (MappingTools.srgMappings == null) {
            MappingTools.srgMappings = new JarMapping();
            try (final InputStream classData = MappingTools.class.getResourceAsStream("/deobfuscation_data-1.12.2.lzma");
                 final BufferedReader br = new BufferedReader(new InputStreamReader(new LzmaInputStream(classData)))) {// 
                MappingTools.srgMappings.loadMappings(br, null, null, false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return MappingTools.srgMappings;
    }

    private static JarMapping getSpigotMappings() {
        if (MappingTools.spigotMappings == null) {
            MappingTools.spigotMappings = new JarMapping();
            try {
                MappingTools.spigotMappings.loadMappings(new BufferedReader(new InputStreamReader(MappingTools.class.getResourceAsStream("/mappings/v1_12_R1/package.srg"))), null, null, false);
                MappingTools.spigotMappings.loadMappings(new BufferedReader(new InputStreamReader(MappingTools.class.getResourceAsStream("/mappings/v1_12_R1/bukkit-1.12.2-cl.csrg"))), null, null, false);
                MappingTools.spigotMappings.loadMappings(new BufferedReader(new InputStreamReader(MappingTools.class.getResourceAsStream("/mappings/v1_12_R1/bukkit-1.12.2-members.csrg"))), null, null, false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return MappingTools.spigotMappings;
    }

    private static List<String> genCb2Srg() {
        if (MappingTools.srgMappings == null) {
            getSrgMappings();
        }
        if (MappingTools.spigotMappings == null) {
            getSpigotMappings();
        }
        final CustomSearge mappingWriter = new CustomSearge("Spigot", "Atom", "");
        final Map<String, String> spigotClassMap = new HashMap<>();
        final Map<String, String> spigotPackgeMap = new HashMap<>();
        for (final String key : MappingTools.srgMappings.classes.keySet()) {
            String nms = MappingTools.spigotMappings.classes.get(key);
            final String srg = MappingTools.srgMappings.classes.get(key);
            if (nms == null || nms.equals("")) {
                if (key.contains("$")) {
                    final String[] splits = key.split("\\$");
                    final String nms_tmp = MappingTools.spigotMappings.classes.get(splits[0]);
                    if (nms_tmp != null && !nms_tmp.equals("")) {
                        final StringBuilder innerClassName = new StringBuilder();
                        for (final String split : splits) {
                            if (!split.equals(splits[0])) {
                                innerClassName.append("$").append(split);
                            }
                        }
                        nms = nms_tmp + innerClassName.toString();
                    } else {
                        nms = key;
                    }
                } else {
                    nms = key;
                }
            }
            if (!nms.contains("/") && !nms.equals(key)) {
                nms = MappingTools.spigotMappings.packages.get(".") + nms;
            } else if (nms.contains("net/minecraft/server/MinecraftServer")) {
                nms = nms.replace("net/minecraft/server/MinecraftServer", MappingTools.spigotMappings.packages.get(".") + "MinecraftServer");
            }
            if (!nms.equals(key)) {
                spigotClassMap.put(key, nms);
                spigotPackgeMap.put(MappingTools.spigotMappings.classes.get(key), nms);
            }
            mappingWriter.addClassMap(nms, srg);
        }
        for (final String key : MappingTools.srgMappings.fields.keySet()) {
            String[] splits2 = key.split("/");
            String nms2 = key;
            String srg2 = MappingTools.srgMappings.classes.get(splits2[0]);
            if (srg2 != null && !srg2.equals("")) {
                srg2 = srg2 + "/" + MappingTools.srgMappings.fields.get(key);
            } else {
                srg2 = key;
            }
            final StringBuilder fieldName = new StringBuilder();
            for (final String split2 : splits2) {
                if (!fieldName.toString().equals("")) {
                    fieldName.append("/");
                }
                if (!split2.equals(splits2[0])) {
                    fieldName.append(split2);
                }
            }
            String nms_Class = MappingTools.spigotMappings.classes.get(splits2[0]);
            if (nms_Class != null && !nms_Class.equals("")) {
                nms2 = MappingTools.spigotMappings.fields.get(nms_Class + "/" + fieldName);
                if (nms2 != null && !nms2.equals("")) {
                    nms2 = spigotClassMap.get(splits2[0]) + "/" + nms2;
                } else {
                    nms2 = spigotClassMap.get(splits2[0]) + "/" + fieldName.toString();
                }
            } else if (key.contains("$")) {
                splits2 = key.split("\\$");
                nms_Class = spigotClassMap.get(splits2[0]);
                if (nms_Class != null && !nms_Class.equals("")) {
                    nms2 = key.replace(splits2[0], nms_Class);
                    spigotClassMap.put(key.split("/")[0], nms2.split("/")[0]);
                }
            }
            if (nms2.contains("net/minecraft/server/MinecraftServer")) {
                nms2 = nms2.replace("net/minecraft/server/MinecraftServer", MappingTools.spigotMappings.packages.get(".") + "MinecraftServer");
            }
            mappingWriter.addFieldMap(nms2, srg2);
        }
        for (final String key : MappingTools.srgMappings.methods.keySet()) {
            final String[] splits2 = key.split(" ", 2);
            MethodDescriptor nmsMethodDescriptor = new MethodDescriptor(null, MappingTools.spigotMappings.classes);
            final MethodDescriptor srgMethodDescriptor = new MethodDescriptor(null, MappingTools.srgMappings.classes);
            String nms_Descriptor = nmsMethodDescriptor.transform(splits2[1]);
            final String srg_Descriptor = srgMethodDescriptor.transform(splits2[1]);
            final String srg3 = MappingTools.srgMappings.methods.get(key);
            final String srg_Class = MappingTools.srgMappings.classes.get(splits2[0].substring(0, splits2[0].lastIndexOf("/")));
            String nms3 = splits2[0].substring(splits2[0].lastIndexOf("/") + 1);
            String nms_Class2 = MappingTools.spigotMappings.classes.get(splits2[0].substring(0, splits2[0].lastIndexOf("/")));
            if (nms_Class2 == null || nms_Class2.equals("")) {
                nms_Class2 = splits2[0].substring(0, splits2[0].lastIndexOf("/"));
                if (nms_Class2.contains("$")) {
                    final String[] strings = nms_Class2.split("\\$", 2);
                    final String temp_Class = MappingTools.spigotMappings.classes.get(strings[0]);
                    if (temp_Class != null && !temp_Class.equals("")) {
                        nms_Class2 = temp_Class + "$" + strings[1];
                    }
                }
            }
            String temp = MappingTools.spigotMappings.methods.get(nms_Class2 + "/" + nms3 + " " + nms_Descriptor);
            if (temp != null && !temp.equals("")) {
                nms3 = temp;
            }
            temp = spigotPackgeMap.get(nms_Class2);
            if (temp != null && !temp.equals("")) {
                nms_Class2 = temp;
            } else if (nms_Class2.contains("$")) {
                final String[] strings2 = nms_Class2.split("\\$", 2);
                temp = spigotPackgeMap.get(strings2[0]);
                if (temp != null && !temp.equals("")) {
                    nms_Class2 = temp + "$" + strings2[1];
                }
            }
            nmsMethodDescriptor = new MethodDescriptor(null, spigotPackgeMap);
            nms_Descriptor = nmsMethodDescriptor.transform(nms_Descriptor);
            nms3 = nms_Class2 + "/" + nms3 + " " + nms_Descriptor;
            if (nms3.contains("net/minecraft/server/MinecraftServer")) {
                nms3 = nms3.replaceAll("net/minecraft/server/MinecraftServer", MappingTools.spigotMappings.packages.get(".") + "MinecraftServer");
            }
            mappingWriter.addMethodMap(nms3, srg_Class + "/" + srg3 + " " + srg_Descriptor);
        }
        return mappingWriter.getLines();
    }

    static {
        cacheDir = new File("cache");
        MappingTools.debug = false;
        addMapping = new String[]{"TUQ6IG5ldC9taW5lY3JhZnQvc2VydmVyL01pbmVjcmFmdFNlcnZlci9nZXRTZXJ2ZXIgKClMbmV0L21pbmVjcmFmdC9zZXJ2ZXIvTWluZWNyYWZ0U2VydmVyOyBuZXQvbWluZWNyYWZ0L3NlcnZlci9NaW5lY3JhZnRTZXJ2ZXIvZ2V0U2VydmVySW5zdCAoKUxuZXQvbWluZWNyYWZ0L3NlcnZlci9NaW5lY3JhZnRTZXJ2ZXI7", "TUQ6IG5ldC9taW5lY3JhZnQvc2VydmVyL01pbmVjcmFmdFNlcnZlci9nZXRTZXJ2ZXJDb25uZWN0aW9uICgpTG5ldC9taW5lY3JhZnQvc2VydmVyL1NlcnZlckNvbm5lY3Rpb247IG5ldC9taW5lY3JhZnQvc2VydmVyL01pbmVjcmFmdFNlcnZlci9mdW5jXzE0NzEzN19hZyAoKUxuZXQvbWluZWNyYWZ0L25ldHdvcmsvTmV0d29ya1N5c3RlbTs=", "TUQ6IG5ldC9taW5lY3JhZnQvc2VydmVyL0VudGl0eUFybW9yU3RhbmQvc2V0SW52aXNpYmxlIChaKVYgbmV0L21pbmVjcmFmdC9lbnRpdHkvaXRlbS9FbnRpdHlBcm1vclN0YW5kL2Z1bmNfODIxNDJfYyAoWilW", "TUQ6IG5ldC9taW5lY3JhZnQvc2VydmVyL0VudGl0eVBsYXllci9jbG9zZUludmVudG9yeSAoKVYgbmV0L21pbmVjcmFmdC9lbnRpdHkvcGxheWVyL0VudGl0eVBsYXllck1QL2Z1bmNfNzEwNTNfaiAoKVY=", "TUQ6IG5ldC9taW5lY3JhZnQvc2VydmVyL0NoYXRDb21wb25lbnRUZXh0L2dldFRleHQgKClMamF2YS9sYW5nL1N0cmluZzsgbmV0L21pbmVjcmFmdC91dGlsL3RleHQvVGV4dENvbXBvbmVudFN0cmluZy9mdW5jXzE1MDI2NV9nICgpTGphdmEvbGFuZy9TdHJpbmc7", "RkQ6IG5ldC9taW5lY3JhZnQvc2VydmVyL01pbmVjcmFmdFNlcnZlci93b3JsZHMgbmV0L21pbmVjcmFmdC9zZXJ2ZXIvTWluZWNyYWZ0U2VydmVyL3dvcmxkU2VydmVyTGlzdA==", "TUQ6IG5ldC9taW5lY3JhZnQvc2VydmVyL0VudGl0eUh1bWFuL2dldEFic29ycHRpb25IZWFydHMgKClGIG5ldC9taW5lY3JhZnQvZW50aXR5L3BsYXllci9FbnRpdHlQbGF5ZXIvZnVuY18xMTAxMzlfYmogKClG", "TUQ6IG5ldC9taW5lY3JhZnQvc2VydmVyL0VudGl0eUh1bWFuL3NldEFic29ycHRpb25IZWFydHMgKEYpViBuZXQvbWluZWNyYWZ0L2VudGl0eS9wbGF5ZXIvRW50aXR5UGxheWVyL2Z1bmNfMTEwMTQ5X20gKEYpVg=="};
        delMapping = Arrays.asList("TUQ6IG5ldC9taW5lY3JhZnQvc2VydmVyL0VudGl0eUh1bWFuL2NEICgpRiBuZXQvbWluZWNyYWZ0L2VudGl0eS9wbGF5ZXIvRW50aXR5UGxheWVyL2Z1bmNfMTEwMTM5X2JqICgpRg==", "TUQ6IG5ldC9taW5lY3JhZnQvc2VydmVyL0VudGl0eUh1bWFuL20gKEYpViBuZXQvbWluZWNyYWZ0L2VudGl0eS9wbGF5ZXIvRW50aXR5UGxheWVyL2Z1bmNfMTEwMTQ5X20gKEYpVg==");
    }
}
