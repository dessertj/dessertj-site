package de.spricom.dessert.sample;

import de.spricom.dessert.assertions.SliceAssertions;
import de.spricom.dessert.slicing.Classpath;
import de.spricom.dessert.slicing.Clazz;
import de.spricom.dessert.slicing.Root;
import de.spricom.dessert.slicing.Slice;
import org.junit.jupiter.api.Test;

import static de.spricom.dessert.assertions.SliceAssertions.dessert;

public class DessertSampleTest {


    // tag::fails[]
    @Test
    void willFail() {
        Classpath cp = new Classpath();
        Clazz me = cp.asClazz(this.getClass());
        Root root = cp.rootOf(Test.class);
        SliceAssertions.dessert(me).usesNot(root);
    }
    // end::fails[]

    // tag::succeeds[]
    @Test
    void willSucceed() {
        Classpath cp = new Classpath();
        Slice myPackage = cp.packageOf(this.getClass());
        Slice java = cp.slice("java..*");
        Slice libs = cp.packageOf(Test.class).plus(cp.slice("..dessert.assertions|slicing.*"));
        dessert(myPackage).usesOnly(java, libs);
    }
    // end::succeeds[]

    @Test
    void queuingAssertions() {
        // tag::queuing[]
        Classpath cp = new Classpath();
        dessert(cp.asClazz(this.getClass()))
                .usesNot(cp.slice("java.io|net..*"))
                .usesNot(cp.slice("org.junit.jupiter.api.Assertions"))
                .usesOnly(cp.slice("..junit.jupiter.api.*"),
                        cp.slice("..dessert..*"),
                        cp.slice("java.lang..*"));
        // end::queuing[]
    }
}
