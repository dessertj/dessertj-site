package org.dessertj.start;

import org.dessertj.assertions.SliceAssertions;
import org.dessertj.slicing.Classpath;
import org.dessertj.slicing.Clazz;
import org.dessertj.slicing.Root;
import org.dessertj.slicing.Slice;
import org.junit.jupiter.api.Test;

import static org.dessertj.assertions.SliceAssertions.assertThatSlice;

public class DessertSampleTest {


    // tag::fails[]
    @Test
    void willFail() {
        Classpath cp = new Classpath();
        Clazz me = cp.asClazz(this.getClass());
        Root junit = cp.rootOf(Test.class);
        SliceAssertions.assertThatSlice(me).doesNotUse(junit);
    }
    // end::fails[]

    // tag::succeeds[]
    @Test
    void willSucceed() {
        Classpath cp = new Classpath();
        Slice myPackage = cp.packageOf(this.getClass());
        Slice java = cp.slice("java..*");
        Slice libs = cp.packageOf(Test.class).plus(cp.slice("..dessertj.assertions|slicing.*"));
        assertThatSlice(myPackage).usesOnly(java, libs);
    }
    // end::succeeds[]

    @Test
    void queuingAssertions() {
        // tag::queuing[]
        Classpath cp = new Classpath();
        assertThatSlice(cp.asClazz(this.getClass()))
                .usesNot(cp.slice("java.io|net..*"))
                .usesNot(cp.slice("org.junit.jupiter.api.Assertions"))
                .usesOnly(cp.slice("..junit.jupiter.api.*"),
                        cp.slice("..dessertj..*"),
                        cp.slice("java.lang..*"));
        // end::queuing[]
    }
}
