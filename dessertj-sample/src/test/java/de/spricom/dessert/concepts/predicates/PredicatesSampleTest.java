package de.spricom.dessert.concepts.predicates;

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
        Assertions.assertThatCode(this::willFail).hasMessage("""
                Illegal Dependencies:
                de.spricom.dessert.assertions.DefaultCycleRenderer
                 -> de.spricom.dessert.slicing.Clazz
                 -> de.spricom.dessert.slicing.ConcreteSlice
                 -> de.spricom.dessert.slicing.PackageSlice
                de.spricom.dessert.assertions.DefaultIllegalDependenciesRenderer
                 -> de.spricom.dessert.slicing.Clazz
                de.spricom.dessert.assertions.IllegalDependencies
                 -> de.spricom.dessert.slicing.Clazz
                de.spricom.dessert.assertions.SliceAssert
                 -> de.spricom.dessert.slicing.Clazz
                 -> de.spricom.dessert.slicing.ConcreteSlice
                 -> de.spricom.dessert.slicing.Slices
                """);
    }
}
