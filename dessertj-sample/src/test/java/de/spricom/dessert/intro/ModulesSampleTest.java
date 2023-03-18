package de.spricom.dessert.intro;

import de.spricom.dessert.assertions.SliceAssertions;
import de.spricom.dessert.modules.ModuleRegistry;
import de.spricom.dessert.modules.core.ModuleSlice;
import de.spricom.dessert.modules.fixed.JavaModules;
import de.spricom.dessert.slicing.Classpath;
import de.spricom.dessert.slicing.Slice;
import org.junit.jupiter.api.Test;

import static de.spricom.dessert.assertions.SliceAssertions.assertThatSlice;

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
