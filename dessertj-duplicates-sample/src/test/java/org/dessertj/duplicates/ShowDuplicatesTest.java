package org.dessertj.duplicates;

import org.dessertj.classfile.ClassFile;
import org.dessertj.classfile.FieldInfo;
import org.dessertj.classfile.MethodInfo;
import org.dessertj.slicing.Classpath;
import org.dessertj.slicing.Clazz;
import org.dessertj.slicing.Slice;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.HamcrestCondition.matching;
import static org.hamcrest.core.StringStartsWith.startsWith;

public class ShowDuplicatesTest {
    private static final Classpath cp = new Classpath();

    @Test
    @Disabled
    void testNoDuplicates() {
        assertThat(cp.duplicates().minus("module-info").getClazzes()).isEmpty();
    }

    @Test
    void testNoDuplicatesFails() {
        assertThatCode(this::testNoDuplicates).isInstanceOf(AssertionError.class);
    }

    // tag::noAdditionalDuplicates[]
    @Test
    @DisplayName("Make sure there are no additional duplicates")
    void ensureNoAdditonalDuplicates() {
        Slice duplicates = cp.duplicates().minus("module-info");

        List<File> duplicateJars = duplicates.getClazzes().stream()
                .map(this::getRootFile).distinct()
                .sorted(Comparator.comparing(File::getName))
                .collect(Collectors.toList());

        Map<String, Set<File>> duplicateJarsByClass = duplicates.getClazzes().stream()
                .collect(Collectors.groupingBy(Clazz::getName,
                        TreeMap::new,
                        Collectors.mapping(this::getRootFile, Collectors.toSet())));

        System.out.printf("There are %d duplicate classes spread over %d jars:%n",
                duplicateJarsByClass.size(), duplicateJars.size());
        System.out.println("\nDuplicate classes:");
        duplicateJarsByClass.forEach((name, files) -> System.out.printf("%s (%s)%n", name,
                files.stream().map(File::getName).sorted().collect(Collectors.joining(", "))));
        System.out.println("\nJARs containing duplicates:");
        duplicateJars.forEach(jar -> System.out.printf("%s%n", jar.getAbsolutePath()));

        // make sure there are no additional jars involved
        assertThat(duplicateJars.stream().map(File::getName))
                .areAtLeast(3, matching(startsWith("jakarta.")))
                .hasSize(5);

        // make sure there are no additonal classes involved
        assertThat(duplicates
                .minus("javax.activation|annotation|transaction|xml..*")
                .minus("com.sun.activation..*")
                .getClazzes()).isEmpty();
    }

    private File getRootFile(Clazz clazz) {
        return clazz.getRoot().getRootFile();
    }
    // end::noAdditionalDuplicates[]

    // tag::binary[]
    @Test
    @DisplayName("Dump all duplicates for which the .class files are different")
    void dumpBinaryDifferences() {
        Slice duplicates = cp.duplicates().minus("module-info");

        Map<String, List<Clazz>> duplicatesByName = duplicates.getClazzes().stream()
                .collect(Collectors.groupingBy(Clazz::getName));

        for (List<Clazz> list : duplicatesByName.values()) {
            list.subList(1, list.size()).forEach(c -> checkBinaryContent(list.get(0), c));
        }
    }

    private void checkBinaryContent(Clazz c1, Clazz c2) {
        if (!isSameBinaryContent(c1, c2)) {
            System.out.printf("Binaries of %s in %s and %s are different.%n",
                    c1.getName(), getRootFile(c1).getPath(), getRootFile(c2).getPath());
        }
    }

    private boolean isSameBinaryContent(Clazz c1, Clazz c2) {
        try {
            byte[] bin1 = IOUtils.toByteArray(c1.getURI().toURL().openStream());
            byte[] bin2 = IOUtils.toByteArray(c2.getURI().toURL().openStream());
            return Arrays.equals(bin1, bin2);
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot compare duplicates of " + c1.getName());
        }
    }
    // end::binary[]

    // tag::api[]
    @Test
    @DisplayName("Dump all duplicates for which the API differs")
    void dumpApiDifferences() {
        Slice duplicates = cp.duplicates().minus("module-info");

        Map<String, List<Clazz>> duplicatesByName = duplicates.getClazzes().stream()
                .collect(Collectors.groupingBy(Clazz::getName));

        for (List<Clazz> list : duplicatesByName.values()) {
            list.subList(1, list.size()).forEach(c -> checkAPI(list.get(0), c));
        }
    }

    private void checkAPI(Clazz c1, Clazz c2) {
        if (!isSameAPI(c1, c2)) {
            System.out.printf("API of %s in %s and %s is different.%n",
                    c1.getName(), getRootFile(c1).getPath(), getRootFile(c2).getPath());
        }
    }

    private boolean isSameAPI(Clazz c1, Clazz c2) {
        ClassFile cf1 = c1.getClassFile();
        ClassFile cf2 = c2.getClassFile();
        return cf1.getAccessFlags() == cf2.getAccessFlags()
                && cf1.getThisClass().equals(cf2.getThisClass())
                && cf1.getSuperClass().equals(cf2.getSuperClass())
                && Arrays.equals(cf1.getInterfaces(), cf2.getInterfaces())
                && isEqual(cf1.getFields(), cf2.getFields(), this::isEqual)
                && isEqual(cf1.getMethods(), cf2.getMethods(), this::isEqual);
    }

    private <T> boolean isEqual(T[] t1, T[] t2, BiPredicate<T, T> predicate) {
        if (t1 == null && t2 == null) {
            return true;
        }
        if (t1 == null || t2 == null) {
            return false;
        }
        if (t1.length != t2.length) {
            return false;
        }
        for (int i = 0; i < t1.length; i++) {
            if (!predicate.test(t1[i], t2[i])) {
                return false;
            }
        }
        return true;
    }

    private boolean isEqual(MethodInfo m1, MethodInfo m2) {
        return m1.getAccessFlags() == m2.getAccessFlags()
                && m1.getDeclaration().equals(m2.getDeclaration());
    }

    private boolean isEqual(FieldInfo f1, FieldInfo f2) {
        return f1.getAccessFlags() == f2.getAccessFlags()
                && f1.getDeclaration().equals(f2.getDeclaration());
    }
    // end::api[]
}
