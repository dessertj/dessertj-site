package de.spricom.dessert.docgen;

import de.spricom.dessert.modules.ModuleRegistry;
import de.spricom.dessert.slicing.Classpath;
import de.spricom.dessert.slicing.Clazz;
import de.spricom.dessert.slicing.Slice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class GeneratePlantumlTest {

    private static final File docgen = new File("target/docgen");

    @BeforeEach
    void init() {
        docgen.mkdirs();
    }

    @Test
    void generateSliceUml() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("@startuml\n");
        sb.append("hide empty members\n\n");
        addSlices(sb);
        sb.append("@enduml\n");
        Files.writeString(Path.of(docgen.getPath(), "slice-overview.puml"), sb);
    }

    private void addSlices(StringBuilder sb) {
        Classpath cp = new Classpath();
        Slice slice = cp.packageOf(Slice.class)
                .plus(cp.packageTreeOf(ModuleRegistry.class));
        List<? extends Class<?>> sliceClasses = slice.getClazzes().stream()
                .filter(this::filter)
                .map(Clazz::getClassImpl)
                .sorted(this::compare)
                .toList();
        sliceClasses.forEach(c -> addClassDefinition(sb, c));
        sb.append("\n");
        sliceClasses.forEach(c -> addClassDependencies(sb, c));
    }

    private boolean filter(Clazz clazz) {
        return clazz.getClassFile().isPublic()
                && !clazz.getClassFile().isInnerClass()
                && Slice.class.isAssignableFrom(clazz.getClassImpl());
    }

    private int compare(Class<?> a, Class<?> b) {
        if (a.isAssignableFrom(b)) {
            return b.isAssignableFrom(a) ? 0 : -1;
        } else {
            return b.isAssignableFrom(a) ? 1 : 0;
        }
    }

    private void addClassDefinition(StringBuilder sb, Class<?> clazz) {
        if (clazz.isInterface()) {
            sb.append("interface ");
        } else if (Modifier.isAbstract(clazz.getModifiers())) {
            sb.append("abstract class ");
        } else {
            return;
        }
        sb.append(clazz.getSimpleName()).append("\n");
    }

    private void appendMethod(StringBuilder sb, Method method) {
        if (!Modifier.isPublic(method.getModifiers())) {
            return;
        }
        sb.append(method.getName()).append("(");
        sb.append(")");
        if (method.getReturnType() != Void.class) {
            sb.append(": ").append(method.getReturnType().getSimpleName());
        }
        sb.append("\n");
    }

    private void addClassDependencies(StringBuilder sb, Class<?> clazz) {
        if (clazz.getSuperclass() != null
                && clazz.getSuperclass() != Object.class
        && isPublic(clazz.getSuperclass())) {
            sb.append(clazz.getSuperclass().getSimpleName());
            sb.append(" <|-- ");
            sb.append(clazz.getSimpleName());
            sb.append("\n");
        }
        for (Class<?> ifc : clazz.getInterfaces()) {
            if (!isPublic(ifc)) {
                continue;
            }
            if (Slice.class.isAssignableFrom(ifc)) {
                sb.append(ifc.getSimpleName());
                sb.append(" <|.. ");
                sb.append(clazz.getSimpleName());
                sb.append("\n");
            }
        }
    }

    private boolean isPublic(Class<?> clazz) {
        return Modifier.isPublic(clazz.getModifiers());
    }
}
