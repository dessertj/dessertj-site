package org.dessertj.concepts.cycle;

import org.dessertj.slicing.Classpath;
import org.dessertj.slicing.Slice;
import org.junit.jupiter.api.Test;

import static org.dessertj.assertions.SliceAssertions.dessert;

public class CycleTest {

    @Test
    void classCycle() {
        // tag::classCycle[]
        Classpath cp = new Classpath();
        dessert(cp.packageTreeOf(CycleDump.class).getClazzes()).isCycleFree();
        // end::classCycle[]
    }

    @Test
    void packageCycle() {
        // tag::packageCycle[]
        Classpath cp = new Classpath();
        Slice slice = cp.packageTreeOf(CycleDump.class);
        dessert(slice.partitionByPackage()).isCycleFree();
        // end::packageCycle[]
    }
}
