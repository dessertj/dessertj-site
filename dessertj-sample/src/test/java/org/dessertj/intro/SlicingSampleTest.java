package org.dessertj.intro;

import org.dessertj.slicing.Classpath;
import org.dessertj.slicing.Root;
import org.dessertj.slicing.Slice;
import org.junit.jupiter.api.Test;

import static org.dessertj.assertions.SliceAssertions.assertThatSlice;

class SlicingSampleTest {
    private static final Classpath cp = new Classpath();

    @Test
    void checkDessertLibraryDependencies() {
        // application code:
        Root dessert = cp.rootOf(Slice.class);

        // dependencies:
        Slice allowedDependencies = cp.slice("java.lang|util|io|net..*");

        // requirement:
        assertThatSlice(dessert).usesOnly(allowedDependencies);
    }
}
