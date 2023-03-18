package org.dessertj.guide;

import org.dessertj.classfile.ClassFile;
import org.dessertj.partitioning.ClazzPredicates;
import org.dessertj.slicing.*;
import org.dessertj.util.AnnotationPattern;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Arrays;

import static org.dessertj.assertions.SliceAssertions.assertThatSlice;
import static org.assertj.core.api.Assertions.assertThat;


public class DetectingUnwantedDependenciesTest {
    // tag::cp[]
    private static final Classpath cp = new Classpath();
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
        // All classes of a .jar file or of the classes directory containing ClazzResolver.class
        Root something1 = cp.rootOf(ClazzResolver.class);

        // A single class
        Clazz something2 = cp.asClazz(ClazzResolver.class);

        // All classes within a certain package
        Slice something3 = cp.packageOf(ClazzResolver.class);

        // All classes within a certain package or any sub-package
        Slice something4 = cp.packageTreeOf(ClazzResolver.class);

        // The package-tree limited to the classes from the something1 Root
        Slice something5 = something1.packageTreeOf(ClazzResolver.class);
        // end::slices[]

        int index = 1;
        for (Slice slice : Arrays.asList(something1, something2, something3, something4, something5)) {
            assertThat(slice.getClazzes()).as("something" + index).isNotEmpty();
            index++;
        }
    }

    @Test
    void unwanted() {
        // tag::unwanted[]
        // All classes withing the package name 'com.sun' or any sub-package
        Slice unwanted1 = cp.slice("com.sun..*");

        // An arbitrary list of classes
        Slice unwanted2 = cp.sliceOf(ClazzResolver.class, ClassFile.class);

        // A combination of packages
        Slice unwanted3 = cp.slice("java.lang.reflect|runtime..*");

        // Classes from internal packages
        Slice unwanted4 = cp.slice("..internal..*");

        // Classes following some naming pattern
        Slice unwanted5 = cp.slice("..springframework..*Impl");

        // Everything form a framework but a certain class
        Slice unwanted6 = cp.slice("..springframework..*").minus(cp.sliceOf(Environment.class));

        // The deprecated classes from the JUnit-Jupiter API .jar
        Slice unwanted7 = cp.rootOf(Test.class).slice(ClazzPredicates.DEPRECATED);

        // All classes annotated with @Configuration
        Slice unwanted8 = cp.slice(ClazzPredicates.matchesAnnotation(AnnotationPattern.of(Configuration.class)));

        // The union of two slices
        Slice unwanted9 = unwanted1.plus(unwanted4);
        // end::unwanted[]

        int index = 1;
        for (Slice slice : Arrays.asList(unwanted1, unwanted2, unwanted3, unwanted4, unwanted5, unwanted6,
                unwanted7, unwanted8, unwanted9)) {
            assertThat(slice.getClazzes()).as("unwanted" + index).isNotEmpty();
            index++;
        }
    }

}
