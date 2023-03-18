package org.dessertj.concepts.modules;

import org.dessertj.modules.ModuleRegistry;
import org.dessertj.modules.core.ModuleSlice;
import org.dessertj.modules.fixed.JavaModules;
import org.dessertj.modules.fixed.JdkModules;
import org.dessertj.modules.jpms.JpmsModule;
import org.dessertj.slicing.Classpath;
import org.junit.jupiter.api.Test;

import static org.dessertj.assertions.SliceAssertions.assertThatSlice;

class ModulesTest {

    // tag::declaration[]
    private final Classpath cp = new Classpath();
    private final ModuleRegistry mr = new ModuleRegistry(cp);
    private final JavaModules java = new JavaModules(mr);
    private final JdkModules jdk = new JdkModules(mr);
    // end::declaration[]

    @Test
    void testJUnitApiDependencies() {
        // tag::junit[]
        ModuleSlice junit = mr.getModule("org.junit.jupiter.api");
        assertThatSlice(junit.getImplementation()
                .minus(cp.slice("org.junit.jupiter.api.AssertionsKt*")))
                .usesOnly(
                        java.base,
                        ((JpmsModule)mr.getModule("org.junit.platform.commons"))
                                .getExportsTo("org.junit.jupiter.api"),
                        mr.getModule("org.opentest4j"),
                        mr.getModule("org.apiguardian.api")
                );
        // end::junit[]
    }
}
