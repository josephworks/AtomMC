package org.atom.remapper;

import net.md_5.specialsource.Ownable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class CustomSearge {
    private static final String HEADER = "# {0}\n# THESE ARE AUTOMATICALLY GENERATED MAPPINGS BETWEEN {1} and {2}\n# THEY WERE GENERATED ON {3} USING Special Source (c) md_5 2012-2013.\n# PLEASE DO NOT REMOVE THIS HEADER!\n";
    private final List<String> lines;
    private final String oldJarName;
    private final String newJarName;
    private final String md5;

    public CustomSearge(final String oldJarName, final String newJarName, final String md5) {
        this.lines = new ArrayList<String>();
        this.oldJarName = oldJarName;
        this.newJarName = newJarName;
        this.md5 = md5;
    }

    public void addClassMap(final String oldClass, final String newClass) {
        this.addLine("CL: " + oldClass + " " + newClass);
    }

    public void addFieldMap(final Ownable oldField, final Ownable newField) {
        this.addLine("FD: " + oldField.owner + "/" + oldField.name + " " + newField.owner + "/" + newField.name);
    }

    public void addFieldMap(final String oldField, final String newField) {
        this.addLine("FD: " + oldField + " " + newField);
    }

    public void addMethodMap(final Ownable oldMethod, final Ownable newMethod) {
        this.addLine("MD: " + oldMethod.owner + "/" + oldMethod.name + " " + oldMethod.descriptor + " " + newMethod.owner + "/" + newMethod.name + " " + newMethod.descriptor);
    }

    public void addMethodMap(final String oldMethod, final String newMethod) {
        this.addLine("MD: " + oldMethod + " " + newMethod);
    }

    public void write(final File file) {
        Collections.sort(this.lines);
        try {
            final FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(MessageFormat.format("# {0}\n# THESE ARE AUTOMATICALLY GENERATED MAPPINGS BETWEEN {1} and {2}\n# THEY WERE GENERATED ON {3} USING Special Source (c) md_5 2012-2013.\n# PLEASE DO NOT REMOVE THIS HEADER!\n", this.md5, this.oldJarName, this.newJarName, new Date()));
            for (final String s : this.lines) {
                fileWriter.write(s + "\r\n");
            }
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getLines() {
        return this.lines;
    }

    protected void addLine(final String line) {
        this.lines.add(line);
    }
}
