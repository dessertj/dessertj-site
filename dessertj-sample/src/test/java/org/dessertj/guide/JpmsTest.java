package org.dessertj.guide;

import org.dessertj.modules.ModuleRegistry;
import org.dessertj.modules.core.ModuleSlice;
import org.dessertj.modules.fixed.JavaModules;
import org.dessertj.slicing.Classpath;
import org.dessertj.slicing.Root;
import org.dessertj.slicing.Slice;
import org.junit.jupiter.api.Test;

import static org.dessertj.assertions.SliceAssertions.assertThatSlice;

public class JpmsTest {
    // tag::jpms[]
    private static final Classpath cp = new Classpath();
    private static final ModuleRegistry mr = new ModuleRegistry(cp);
    private static final JavaModules java = new JavaModules(mr);
    // end::jpms[]

    private static final Root dessert = cp.rootOf(Slice.class);

    // tag::module[]
    private final ModuleSlice junit = mr.getModule("org.junit.jupiter.api");
    // end::module[]

    @Test
    void testDessertDependencies() {
        // tag::using[]
        assertThatSlice(dessert)
                .usesOnly(java.base, java.logging);
        // end::using[]
    }

    @Test
    void testMyDependencies() {
        assertThatSlice(cp.sliceOf(this.getClass()))
                .usesOnly(java.base, junit, dessert);
    }

    @Test
    void testUsesNot() {
        // tag::usesNot[]
        assertThatSlice(cp.sliceOf(this.getClass()))
                .doesNotUse(junit.getInternals());
        // end::usesNot[]
    }

    @Test
    void dumpLogging() {
        // Class<?> usage = sun.util.logging.internal.LoggingProviderImpl.class;
        java.logging.getInternals().getClazzes().forEach(System.out::println);
    }

    @Test
    void dumpJUnit() {
        // Class<?> usage = org.junit.jupiter.api.extension.support.TypeBasedParameterResolver.class;
        junit.getInternals().getClazzes().forEach(System.out::println);
    }
}
