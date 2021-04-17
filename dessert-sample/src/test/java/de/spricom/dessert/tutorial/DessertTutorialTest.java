package de.spricom.dessert.tutorial;

import de.spricom.dessert.slicing.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;
import org.junit.platform.commons.util.PreconditionViolationException;

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

    @Disabled("This test will fail, because there are cycles")
    @Test
    @DisplayName("Detect cycles")
    void detectCycles() {
        Root junitPlatformCommons = cp.rootOf(Testable.class);
        dessert(junitPlatformCommons.partitionByPackage()).isCycleFree();
    }

    @Test
    @DisplayName("Investigate your project")
    void showUsageOfReflection() {
        Slice commons = cp.slice("org.junit.platform.commons.*")
                .named("org.junit.platform.commons");
        Slice commonsUtil = cp.slice("org.junit.platform.commons.util.*")
                .named("org.junit.platform.commons.util");

        System.out.printf("%nDependencies from %s to %s:%n", commons, commonsUtil);
        showDependencyDetails(commons, commonsUtil);

        System.out.printf("%nDependencies from %s to %s:%n", commonsUtil, commons);
        showDependencyDetails(commonsUtil, commons);
    }

    private void showDependencyDetails(Slice from, Slice to) {
        from.slice(c -> c.uses(to)).getClazzes().stream().sorted().forEach(c ->
                System.out.printf("  %s uses %s%n", c.getSimpleName(),
                        c.getDependencies().slice(to).getClazzes().stream()
                                .map(Clazz::getSimpleName)
                                .collect(Collectors.joining(", "))));
    }

    @Test
    @DisplayName("Simulate refactorings")
    void simulateRefactoring() {
        Root junitPlatformCommons = cp.rootOf(Testable.class);
        SortedMap<String, PackageSlice> originalPackages = junitPlatformCommons.partitionByPackage();
        Map<String, Slice> refactoredPackages = new HashMap<>(originalPackages);

        // Resolve package-cycle by moving one class from one package to another.
        // (In fact that refactoring has already been done. That deprecated exception,
        // remains there only for compatibility reasons.)
        Clazz violatingClazz = cp.asClazz(PreconditionViolationException.class);
        String basePackage = "org.junit.platform.commons";
        String utilPackage = basePackage + ".util";
        refactoredPackages.put(utilPackage, refactoredPackages.get(utilPackage).minus(violatingClazz));
        // The addition below doesn't have much effect. Deleting the deprecated class is sufficient in this case.
        refactoredPackages.put(basePackage, refactoredPackages.get(basePackage).plus(violatingClazz));

        dessert(refactoredPackages).isCycleFree();
    }
}
