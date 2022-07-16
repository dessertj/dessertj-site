package de.spricom.dessert.guide;

import de.spricom.dessert.resolve.ClassResolver;
import de.spricom.dessert.resolve.ClassRoot;
import de.spricom.dessert.slicing.Classpath;
import de.spricom.dessert.slicing.Clazz;
import de.spricom.dessert.slicing.Root;
import de.spricom.dessert.slicing.Slice;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.platform.engine.TestEngine;

import java.io.File;

import static de.spricom.dessert.assertions.SliceAssertions.assertThatSlice;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

public class CustomClasspathTest {

    // tag::customClasspath[]
    Classpath customClasspath = new Classpath(ClassResolver.ofClassPathWithoutJars());
    // end::customClasspath[]

    @Test
    void dumpRoots() {
        // tag::slowDump[]
        customClasspath.getClazzes().stream()
                .map(Clazz::getRoot)
                .map(Root::getURI)
                .distinct()
                .forEach(System.out::println);
        // end::slowDump[]
    }

    @Test
    void dumpResolverRoots() {
        dumpRoots(ClassResolver.ofClassPathWithoutJars());
    }

    @Test
    void dumpDefaultRoots() {
        // tag::dumpDefault[]
        dumpRoots(ClassResolver.ofClassPathAndJavaRuntime());
        // end::dumpDefault[]
    }

    // tag::fastDump[]
    private void dumpRoots(ClassResolver resolver) {
        resolver.getPath().stream()
                .map(ClassRoot::getURI)
                .forEach(System.out::println);
    }
    // end::fastDump[]

    @Disabled("will fail")
    @Test
    void testPattern() {
        // tag::pattern[]
        assertThatSlice(customClasspath).doesNotUse(customClasspath.slice("org.junit.jupiter..*"));
        // end::pattern[]
    }

    @Test
    void testPatternCheck() {
        assertThatCode(this::testPattern)
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("Illegal Dependencies:");
    }

    @Disabled("will fail")
    @Test
    void testClass() {
        // tag::class[]
        assertThatSlice(customClasspath).doesNotUse(customClasspath.rootOf(Test.class));
        // end::class[]
    }

    @Test
    void testClassCheck() {
        assertThatCode(this::testClass)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("org.junit.jupiter.api.Test not found");
    }

    @Test
    void junitClasspath() {
        // tag::junitClasspath[]
        Classpath cp = new Classpath();
        File jupiterApiJar = cp.asClazz(Test.class).getRoot().getRootFile();
        File jupiterEngine = cp.asClazz(JupiterTestEngine.class).getRoot().getRootFile();
        File junitPlatformEngine = cp.asClazz(TestEngine.class).getRoot().getRootFile();

        ClassResolver resolver = new ClassResolver();
        resolver.add(junitPlatformEngine);
        resolver.add(jupiterEngine);
        resolver.add(jupiterApiJar);
        Classpath customClasspath = new Classpath(resolver);
        // end::junitClasspath[]

        dumpRoots(resolver);
        Slice jupiter = customClasspath.slice("org.junit.jupiter..*");
        assertThat(jupiter.getClazzes()).hasSameSizeAs(cp.slice("org.junit.jupiter..*").getClazzes());
    }

}
