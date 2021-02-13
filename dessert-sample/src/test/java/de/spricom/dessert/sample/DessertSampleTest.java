package de.spricom.dessert.sample;

import de.spricom.dessert.assertions.SliceAssertions;
import de.spricom.dessert.slicing.Classpath;
import de.spricom.dessert.slicing.Clazz;
import de.spricom.dessert.slicing.Root;
import de.spricom.dessert.slicing.Slice;
import org.junit.jupiter.api.Test;

public class DessertSampleTest {

    // tag::fails[]
    @Test
    void willFail() {
        Classpath cp = new Classpath();
        Clazz me = cp.asClazz(this.getClass());
        Root root = cp.rootOf(Test.class);
        SliceAssertions.assertThat(me).usesNot(root);
    }
    // end::fails[]

    // tag::succeeds[]
    @Test
    void willSucceed() {
        Classpath cp = new Classpath();
        Slice myPackage = cp.packageOf(this.getClass());
        Slice java = cp.slice("java..*");
        Slice libs = cp.packageOf(Test.class).plus(cp.slice("..dessert.*.*"));
        SliceAssertions.dessert(myPackage).usesOnly(java, libs);
    }
    // end::succeeds[]
}
