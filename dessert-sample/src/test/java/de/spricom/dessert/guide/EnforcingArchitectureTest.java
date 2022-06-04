package de.spricom.dessert.guide;

import de.spricom.dessert.classfile.ClassFile;
import de.spricom.dessert.slicing.Classpath;
import de.spricom.dessert.slicing.Root;
import de.spricom.dessert.slicing.Slice;
import de.spricom.dessert.slicing.Slices;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;

import java.lang.invoke.MethodHandles;

import static de.spricom.dessert.assertions.SliceAssertions.assertThatSlice;
import static org.assertj.core.api.Assertions.assertThat;


public class EnforcingArchitectureTest {
    private static final Logger log = LogManager.getLogger(MethodHandles.lookup().lookupClass());
    // tag::blocks[]
    private static final Classpath cp = new Classpath();
    private static final Root dessert = cp.rootOf(Slice.class);
    private static final Slice classfile = dessert.packageTreeOf(ClassFile.class);
    private static final Slice slicing = dessert.packageTreeOf(Slice.class);
    private static final Slice java = cp.slice("java.lang|util|io|net..*");
    // end::blocks[]

    // tag::common[]
    private final Slice common = Slices.of(
        cp.slice("java.lang|util|io|net..*"), // java packages
        cp.sliceOf(Logger.class, LogManager.class), // logging
        cp.rootOf(StringUtils.class) // apache commons-lang
    );
    // end::common[]

    @Test
    void testUsage() {
        log.info("logging something");
        assertThat(StringUtils.isBlank("   ")).isTrue();
    }

    @Test
    void showUsesOnlySyntax() {
        Slice block = cp.packageTreeOf(ClassFile.class);
        Slice dep1 = cp.slice("java.lang|util..*");
        Slice dep2 = cp.slice("java.io..*");
        assertThat(dep1.getClazzes()).isNotEmpty();
        assertThat(dep2.getClazzes()).isNotEmpty();

        // tag::syntax[]
        assertThatSlice(block).usesOnly(dep1, dep2);
        // end::syntax[]
    }

    // tag::blocks[]

    @Test
    void testClassfileDependencies() {
        assertThatSlice(classfile).usesOnly(java);
    }
    // end::blocks[]

    @Test
    void testUsingBuildingBlocks() {
        assertThatSlice(BuildingBlocks.classfile).usesOnly(BuildingBlocks.java);
    }
}
