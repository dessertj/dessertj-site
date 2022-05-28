package de.spricom.dessert.concepts.arch;

import de.spricom.dessert.assertions.SliceAssertions;
import de.spricom.dessert.classfile.ClassFile;
import de.spricom.dessert.partitioning.ClazzPredicates;
import de.spricom.dessert.resolve.ClassResolver;
import de.spricom.dessert.slicing.Classpath;
import de.spricom.dessert.slicing.Slice;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static de.spricom.dessert.assertions.SliceAssertions.dessert;

public class ArchitectureTest {

    @Test
    void checkDessertAchitecture() {
        // tag::arch[]
        Classpath cp = new Classpath();
        List<Slice> layers = Arrays.asList(
                cp.packageTreeOf(SliceAssertions.class).named("assertions"),
                cp.packageTreeOf(Slice.class).minus(ClazzPredicates.DEPRECATED).named("slicing"),
                cp.packageTreeOf(ClassResolver.class).named("resolve"),
                cp.packageTreeOf(ClassFile.class).named("classfile"),
                cp.slice("..dessert.matching|util..*").named("util")
        );
        dessert(layers).isLayeredRelaxed();
        // end::arch[]
    }
}
