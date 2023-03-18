package org.dessertj.concepts.arch;

import org.dessertj.assertions.SliceAssertions;
import org.dessertj.classfile.ClassFile;
import org.dessertj.partitioning.ClazzPredicates;
import org.dessertj.resolve.ClassResolver;
import org.dessertj.slicing.Classpath;
import org.dessertj.slicing.Slice;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.dessertj.assertions.SliceAssertions.dessert;

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
