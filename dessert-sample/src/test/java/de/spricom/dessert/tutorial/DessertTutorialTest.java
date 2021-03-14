package de.spricom.dessert.tutorial;

import de.spricom.dessert.slicing.Classpath;
import de.spricom.dessert.slicing.Clazz;
import de.spricom.dessert.slicing.Slice;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

import static de.spricom.dessert.assertions.SliceAssertions.dessert;
import static org.assertj.core.api.Assertions.assertThat;

public class DessertTutorialTest {
    private final Classpath cp = new Classpath();

    @Test
    @DisplayName("Detect usage of internal APIs")
    void detectUsageOfInteralApis() {
        Slice myCompanyCode = cp.slice("de.spricom..*");
        dessert(myCompanyCode).usesNot(
                cp.slice("com.sun..*"),
                cp.slice("sun..*"),
                cp.slice("..internal..*").minus(myCompanyCode));
    }

    @Test
    @DisplayName("Detect duplicates")
    void detectDuplicates() {
        Slice duplicates = cp.duplicates().minus("module-info");

        List<File> duplicateJars = duplicates.getClazzes().stream()
                .map(Clazz::getRootFile).distinct()
                .sorted(Comparator.comparing(File::getName))
                .collect(Collectors.toList());

        Map<String, Set<File>> duplicateJarsByClass = duplicates.getClazzes().stream()
                .collect(Collectors.groupingBy(Clazz::getName,
                        TreeMap::new,
                        Collectors.mapping(Clazz::getRootFile, Collectors.toSet())));

        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw)) {
            pw.printf("%nThere are %d duplicate classes spread over %d jars:%n",
                    duplicateJarsByClass.size(), duplicateJars.size());
            pw.println("\nDuplicate classes:");
            duplicateJarsByClass.forEach((name, files) -> pw.printf("%s (%s)%n", name,
                    files.stream().map(File::getName).sorted().collect(Collectors.joining(", "))));
            pw.println("\nJARs containing duplicates:");
            duplicateJars.forEach(jar -> pw.printf("%s%n", jar.getName()));
        }

        assertThat(duplicates.getClazzes().size()).as(sw.toString()).isEqualTo(0);
    }
}
