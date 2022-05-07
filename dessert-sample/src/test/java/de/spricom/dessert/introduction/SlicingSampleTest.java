package de.spricom.dessert.introduction;

import de.spricom.dessert.slicing.Classpath;
import de.spricom.dessert.slicing.Root;
import de.spricom.dessert.slicing.Slice;
import org.junit.jupiter.api.Test;

import static de.spricom.dessert.assertions.SliceAssertions.assertThatSlice;

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
