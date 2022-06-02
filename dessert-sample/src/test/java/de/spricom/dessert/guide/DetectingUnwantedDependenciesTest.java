package de.spricom.dessert.guide;

import de.spricom.dessert.classfile.ClassFile;
import de.spricom.dessert.slicing.*;
import org.junit.jupiter.api.Test;

import static de.spricom.dessert.assertions.SliceAssertions.assertThatSlice;
import static org.assertj.core.api.Assertions.assertThat;


public class DetectingUnwantedDependenciesTest {
    // tag::cp[]
    private final static Classpath cp = new Classpath();
    // end::cp[]

    @Test
    void showDoesNotUseSyntax() {
        Slice something = cp.sliceOf(this.getClass());
        Slice unwanted1 = cp.slice("java.io..*");
        Slice unwanted2 = cp.slice("java.net..*");
        assertThat(unwanted1.getClazzes()).isNotEmpty();
        assertThat(unwanted2.getClazzes()).isNotEmpty();

        // tag::doesNotUse[]
        assertThatSlice(something).doesNotUse(unwanted1, unwanted2);
        // end::doesNotUse[]
    }

    @Test
    void slices() {
        // tag::slices[]
        // The .jar file or the classes directory containing ClazzResolver.class
        Root something1 = cp.rootOf(ClazzResolver.class);

        // A single class
        Clazz something2 = cp.asClazz(ClazzResolver.class);


        // end::slices[]
    }

    @Test
    void unwanted() {
        // tag::unwanted[]
        // All classes withing the package name 'com.sun' or any sub-package
        Slice unwanted1 = cp.slice("com.sun..*");

        // An arbitrary list of classes
        Slice unwanted2 = cp.sliceOf(ClazzResolver.class, ClassFile.class);
        // end::unwanted[]
    }

}
