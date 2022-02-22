package de.spricom.dessert.predicates;

import de.spricom.dessert.assertions.SliceAssertions;
import de.spricom.dessert.partitioning.ClazzPredicates;
import de.spricom.dessert.slicing.Classpath;
import de.spricom.dessert.slicing.Root;
import de.spricom.dessert.slicing.Slice;
import de.spricom.dessert.util.Predicates;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class PredicatesSampleTest {

    @Disabled
    @Test
    void willFail() {
        // tag::predicates[]
        Classpath cp = new Classpath();
        Root dessert = cp.rootOf(Slice.class);
        Slice assertions = dessert.packageOf(SliceAssertions.class);
        Slice slicing = dessert.packageOf(Slice.class);
        Slice slicingInterfaces = slicing.slice(
                Predicates.and(ClazzPredicates.PUBLIC,
                        Predicates.or(
                                ClazzPredicates.INTERFACE,
                                ClazzPredicates.ANNOTATION,
                                ClazzPredicates.ENUM
                        )
                )
        );
        SliceAssertions.dessert(assertions).usesNot(slicing.minus(slicingInterfaces));
        // end::predicates[]
    }

    @Test
    void checkFailure() {
        Assertions.assertThatCode(this::willFail).hasMessage("Illegal Dependencies:\n" +
                "de.spricom.dessert.assertions.DefaultCycleRenderer\n" +
                " -> de.spricom.dessert.slicing.Clazz\n" +
                " -> de.spricom.dessert.slicing.ConcreteSlice\n" +
                " -> de.spricom.dessert.slicing.PackageSlice\n" +
                "de.spricom.dessert.assertions.DefaultIllegalDependenciesRenderer\n" +
                " -> de.spricom.dessert.slicing.Clazz\n" +
                "de.spricom.dessert.assertions.IllegalDependencies\n" +
                " -> de.spricom.dessert.slicing.Clazz\n" +
                "de.spricom.dessert.assertions.SliceAssert\n" +
                " -> de.spricom.dessert.slicing.Clazz\n" +
                " -> de.spricom.dessert.slicing.ConcreteSlice\n" +
                " -> de.spricom.dessert.slicing.Slices\n");
    }
}
