package org.dessertj.concepts.predicates;

import org.dessertj.assertions.SliceAssertions;
import org.dessertj.partitioning.ClazzPredicates;
import org.dessertj.slicing.Classpath;
import org.dessertj.slicing.Root;
import org.dessertj.slicing.Slice;
import org.dessertj.util.Predicates;
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
        Assertions.assertThatCode(this::willFail).hasMessage("""
                Illegal Dependencies:
                org.dessertj.assertions.DefaultCycleRenderer
                 -> org.dessertj.slicing.Clazz
                 -> org.dessertj.slicing.ConcreteSlice
                 -> org.dessertj.slicing.PackageSlice
                org.dessertj.assertions.DefaultIllegalDependenciesRenderer
                 -> org.dessertj.slicing.Clazz
                org.dessertj.assertions.IllegalDependencies
                 -> org.dessertj.slicing.Clazz
                org.dessertj.assertions.SliceAssert
                 -> org.dessertj.slicing.Clazz
                 -> org.dessertj.slicing.ConcreteSlice
                 -> org.dessertj.slicing.Slices
                """);
    }
}
