package org.dessertj.guide;

import org.dessertj.slicing.Classpath;
import org.dessertj.slicing.Clazz;
import org.dessertj.slicing.Root;
import org.dessertj.slicing.Slice;
import org.dessertj.stampshop.application.ShopApplication;
import org.dessertj.stampshop.parts.part3.SomeUtil;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.SortedMap;
import java.util.TreeMap;

import static org.dessertj.assertions.SliceAssertions.assertThatSlices;
import static org.assertj.core.api.Assertions.assertThatCode;

public class SimulatingRefactoringsTest {
    private static final Classpath cp = new Classpath();

    @Disabled("will fail")
    @Test
    void simulateRefactoring() {
        // tag::simulate[]
        Root stampshop = cp.rootOf(ShopApplication.class);

        // Make sure original packages are cycle-free.
        SortedMap<String, Slice> packages = new TreeMap<>(stampshop.partitionByPackage()); // <1>
        assertThatSlices(packages).areCycleFree();

        // Simulate moving class SomeUtil to package of the ShopApplication class.
        Clazz classToMove = cp.asClazz(SomeUtil.class);
        packages.compute(classToMove.getPackageName(),
                (packageName, slice) -> slice.minus(classToMove).named(packageName)); // <2>
        packages.compute(ShopApplication.class.getPackageName(),
                (packageName, slice) -> slice.plus(classToMove).named(packageName));

        // Check for package cycles after moving the class.
        assertThatSlices(packages).areCycleFree(); // <3>
        // end::simulate[]
    }

    @Test
    void checkSimulateRefactoring() {
        assertThatCode(this::simulateRefactoring)
                .isInstanceOf(AssertionError.class)
                .hasMessageContainingAll(
                        "Cycle detected:",
                        "org.dessertj.stampshop.application -> org.dessertj.stampshop.parts.part3:",
                        "ShopApplication -> ShopPart3",
                        "org.dessertj.stampshop.parts.part3 -> org.dessertj.stampshop.application:",
                        "ShopPart3 -> SomeUtil"
                );
    }
}
