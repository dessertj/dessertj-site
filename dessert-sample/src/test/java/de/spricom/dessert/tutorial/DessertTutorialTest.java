package de.spricom.dessert.tutorial;

import de.spricom.dessert.resolve.ClassResolver;
import de.spricom.dessert.slicing.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.platform.commons.annotation.Testable;
import org.junit.platform.commons.util.PreconditionViolationException;
import org.junit.platform.engine.TestEngine;

import java.io.File;
import java.io.IOException;
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

    @Test
    @DisplayName("Check your layers")
    void checkDessertLayers() {
        Root dessert = cp.rootOf(Slice.class);

        // layers
        Slice utilities = dessert.slice("de.spricom.dessert.util|matching..*");
        Slice resolving = dessert.slice("de.spricom.dessert.classfile|resolve..*");
        Slice slicing = dessert.slice("de.spricom.dessert.slicing|partitioning..*");
        Slice modules = dessert.slice("de.spricom.dessert.modules..*");
        Slice assertions = dessert.slice("de.spricom.dessert.assertions..*");

        dessert(assertions, modules, slicing, resolving, utilities).isLayeredRelaxed();

        // dependencies
        dessert(assertions, modules, slicing).usesNot(cp.slice("java.lang.reflect..*"));
    }

    @Test
    @DisplayName("Modularize your project")
    void checkDessertModules() {
        Root dessert = cp.rootOf(Slice.class);

        // modules
        Slice utils = dessert.slice("de.spricom.dessert.util..*");
        Slice matching = dessert.slice("de.spricom.dessert.matching..*");
        Slice classfile = dessert.slice("de.spricom.dessert.classfile..*");
        Slice resolving = dessert.slice("de.spricom.dessert.resolve..*");
        Slice slicing = dessert.slice("de.spricom.dessert.slicing..*");
        Slice partitioning = dessert.slice("de.spricom.dessert.partitioning..*");
        Slice assertions = dessert.slice("de.spricom.dessert.assertions..*");

        // interface slices
        Slice matchingInterface = matching.slice("..NamePattern|ShortNameMatcher");
        Slice classfileInterface = classfile.slice("..ClassFile");

        // external dependencies
        Slice base = cp.slice("java.lang|util.*");
        Slice io = cp.slice("java.io|net.*").plus(cp.slice("java.util.jar.*"));
        Slice reflect = cp.slice("java.lang.reflect|annotation.*");
        Slice regex = cp.slice("java.util.regex.*");
        Slice logging = cp.slice("java.util.logging.*");
        Slice zip = cp.slice("java.util.jar|zip.*");

        // module dependencies
        dessert(utils).usesOnly(base, io, reflect);
        dessert(matching).usesOnly(base, regex, utils);
        dessert(classfile).usesOnly(base, io, regex);
        dessert(resolving).usesOnly(base, io, regex, logging, zip, reflect, utils, matchingInterface, classfileInterface);
        dessert(slicing).usesOnly(base, io, logging, utils, matchingInterface, classfileInterface, resolving);
        dessert(partitioning).usesOnly(base, io, utils, classfile, slicing);
        dessert(assertions).usesOnly(base, io, utils, slicing);
    }

    @Test
    @DisplayName("Define your custom classpath")
    void customClasspath() throws IOException {
        File jupiterApiJar = cp.asClazz(Test.class).getRootFile();
        File jupiterEngine = cp.asClazz(JupiterTestEngine.class).getRootFile();
        File junitPlatformEngine = cp.asClazz(TestEngine.class).getRootFile();

        ClassResolver resolver = new ClassResolver();
        resolver.add(junitPlatformEngine);
        resolver.add(jupiterEngine);
        resolver.add(jupiterApiJar);
        Classpath customClasspath = new Classpath(resolver);

        Slice jupiter = customClasspath.slice("org.junit.jupiter..*");

        assertThat(jupiter.getClazzes()).hasSameSizeAs(cp.slice("org.junit.jupiter..*").getClazzes());
    }

}
