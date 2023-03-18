package org.dessertj.concepts.duplicates;

import org.dessertj.slicing.Classpath;
import org.dessertj.slicing.ConcreteSlice;
import org.dessertj.slicing.Slice;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class DuplicatesTest {

    @Test
    void checkDuplicates() {
        // tag::duplicates[]
        Classpath cp = new Classpath();
        ConcreteSlice duplicates = cp.duplicates();
        duplicates.getClazzes().forEach(clazz -> System.out.println(clazz.getURI()));
        Assertions.assertThat(duplicates.getClazzes()).isNotEmpty();

        Slice slice = duplicates.minus(cp.asClazz("module-info").getAlternatives());
        Assertions.assertThat(slice.getClazzes()).isEmpty();

        Slice slice2 = duplicates.minus(cp.slice("module-info"));
        Assertions.assertThat(slice2.getClazzes()).isEmpty();

        Slice slice3 = duplicates.minus("module-info");
        Assertions.assertThat(slice3.getClazzes()).isEmpty();
        // end::duplicates[]
    }

}
