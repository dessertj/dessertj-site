package org.dessertj.guide;

import org.dessertj.slicing.Classpath;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DuplicatesTest {

    private static final Classpath cp = new Classpath();

    @Test
    void testNoDuplicates() {
        // tag::noDuplicates[]
        assertThat(cp.duplicates().minus("module-info").getClazzes()).isEmpty();
        // end::noDuplicates[]
    }
}
