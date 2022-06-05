package de.spricom.dessert.guide;

import de.spricom.dessert.resolve.ClassPackage;
import de.spricom.dessert.resolve.ClassResolver;
import de.spricom.dessert.resolve.ClassRoot;
import de.spricom.dessert.resolve.TraversalRoot;
import de.spricom.dessert.slicing.Classpath;
import de.spricom.dessert.slicing.Slice;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DefiningSlicesTest {

    private static final Classpath cp = new Classpath();

    @Test
    void operations() {
        Slice slice1 = cp.sliceOf(ClassPackage.class, ClassResolver.class, ClassRoot.class);
        Slice slice2 = cp.sliceOf(ClassRoot.class, TraversalRoot.class);

        // tag::union[]
        Slice union = slice1.plus(slice2);
        // end::union[]
        // tag::intersection[]
        Slice intersection = slice1.slice(slice2);
        // end::intersection[]
        // tag::difference[]
        Slice difference = slice1.minus(slice2);
        // end::difference[]

        assertThat(union.getClazzes()).hasSize(4)
                .containsOnlyOnceElementsOf(cp.sliceOf(ClassPackage.class, ClassResolver.class, ClassRoot.class,
                        TraversalRoot.class).getClazzes());
        assertThat(intersection.getClazzes()).hasSize(1)
                .containsOnlyOnceElementsOf(cp.sliceOf(ClassRoot.class).getClazzes());
        assertThat(difference.getClazzes()).hasSize(2)
                .containsOnlyOnceElementsOf(cp.sliceOf(ClassPackage.class, ClassResolver.class).getClazzes());
    }
}
