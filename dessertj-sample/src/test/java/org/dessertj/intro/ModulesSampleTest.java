package org.dessertj.intro;

import org.dessertj.assertions.SliceAssertions;
import org.dessertj.modules.ModuleRegistry;
import org.dessertj.modules.core.ModuleSlice;
import org.dessertj.modules.fixed.JavaModules;
import org.dessertj.slicing.Classpath;
import org.dessertj.slicing.Slice;
import org.junit.jupiter.api.Test;

import static org.dessertj.assertions.SliceAssertions.assertThatSlice;

class ModulesSampleTest {
    private final Classpath cp = new Classpath();
    private final ModuleRegistry mr = new ModuleRegistry(cp);
    private final JavaModules java = new JavaModules(mr);
    private final ModuleSlice junit = mr.getModule("org.junit.jupiter.api");
    private final Slice dessert = cp.rootOf(SliceAssertions.class);

    @Test
    void testDessertDependencies() {
        assertThatSlice(dessert)
                .usesOnly(java.base, java.logging);
    }

    @Test
    void testMyDependencies() {
        assertThatSlice(cp.sliceOf(this.getClass()))
                .usesOnly(java.base, junit, dessert);
    }
}
