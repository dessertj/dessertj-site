package de.spricom.dessert.concepts.modules;

import de.spricom.dessert.modules.ModuleRegistry;
import de.spricom.dessert.modules.core.ModuleSlice;
import de.spricom.dessert.modules.fixed.JavaModules;
import de.spricom.dessert.modules.fixed.JdkModules;
import de.spricom.dessert.modules.jpms.JpmsModule;
import de.spricom.dessert.slicing.Classpath;
import org.junit.jupiter.api.Test;

import static de.spricom.dessert.assertions.SliceAssertions.assertThatSlice;

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
