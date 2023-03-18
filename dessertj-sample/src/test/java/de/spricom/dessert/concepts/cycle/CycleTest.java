package de.spricom.dessert.concepts.cycle;

import de.spricom.dessert.slicing.Classpath;
import de.spricom.dessert.slicing.Slice;
import org.junit.jupiter.api.Test;

import static de.spricom.dessert.assertions.SliceAssertions.dessert;

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
