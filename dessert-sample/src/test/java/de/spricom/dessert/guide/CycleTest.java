package de.spricom.dessert.guide;

import de.spricom.dessert.modules.ModuleRegistry;
import de.spricom.dessert.modules.core.ModuleSlice;
import de.spricom.dessert.modules.fixed.JavaModules;
import de.spricom.dessert.partitioning.ClazzPredicates;
import de.spricom.dessert.partitioning.SlicePartitioners;
import de.spricom.dessert.slicing.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.spricom.dessert.assertions.SliceAssertions.assertThatSlice;
import static org.assertj.core.api.Assertions.assertThat;

public class CycleTest {
    private static final Logger log = LogManager.getLogger(MethodHandles.lookup().lookupClass());
    private static final Classpath cp = new Classpath();
    private static final ModuleRegistry mr = new ModuleRegistry(cp);
    private static final JavaModules java = new JavaModules(mr);

    private final ModuleSlice junit = mr.getModule("org.junit.jupiter.api");
    private static final Root dessert = cp.rootOf(Slice.class);
    private static final Slice spring = cp.slice("org.springframework..*");


    @Test
    void testPackageCycles() {
        Slice block = junit.getImplementation();

        // tag::cycle[]
        assertThatSlice(block.partitionByPackage()).isCycleFree();
        // end::cycle[]
    }

    @Test
    void testJUnit5() {
        // tag::junit[]
        assertThatSlice(cp.slice("org.junit..*")
                .minus(ClazzPredicates.DEPRECATED)
                .partitionByPackage()).isCycleFree();
        // end::junit[]
    }

    @Test
    void testSet() {
        Slice slice1 = dessert.slice("..assertions..*");
        Slice slice2 = dessert.slice("..slicing..*").minus(ClazzPredicates.DEPRECATED);
        Slice slice2a = dessert.slice("..slicing..*");
        Slice slice3 = dessert.slice("..resolve..*");

        // tag::set[]
        List<Slice> slices = Arrays.asList(slice1, slice2, slice3);
        assertThatSlice(slices).isCycleFree();
        // end::set[]

        // tag::enum[]
        assertThatSlice(slice1, slice2, slice3).isCycleFree();
        // end::enum[]

        // tag::classes[]
        assertThatSlice(slice1.getClazzes()).isCycleFree();
        // end::classes[]

        // tag::map[]
        Map<String, Slice> slicesByName = Map.of(
                "slice1", slice1,
                "slice2", slice2,
                "slice3", slice3
        );
        assertThatSlice(slicesByName).isCycleFree();
        // end::map[]

        for (Slice slice : slices) {
            assertThat(slice.getClazzes()).isNotEmpty();
        }
    }

    @Test
    void testList() {
        // tag::list[]
        spring.partitionByPackage().keySet().forEach(System.out::println);
        // end::list[]
    }

    @Test
    void testListSize() {
        // tag::size[]
        spring.partitionByPackage()
                .forEach((k, s) -> System.out.printf("%s[%d]%n", k, s.getClazzes().size()));
        // end::size[]
    }

    @Test
    void testTop() {
        // tag::top[]
        SortedMap<String, PartitionSlice> topLevelPackages = spring.partitionBy(this::topLevelPackageName);
        assertThatSlice(topLevelPackages).isCycleFree();
        // end::top[]
        topLevelPackages.forEach((k, s) -> System.out.printf("%s[%d]%n", k, s.getClazzes().size()));
    }

    // tag::topm[]
    private String topLevelPackageName(Clazz clazz) {
        Pattern pattern = Pattern.compile("org\\.springframework\\.([^.]+)\\.");
        Matcher matcher = pattern.matcher(clazz.getName());
        if (matcher.lookingAt()) {
            return matcher.group(1);
        }
        return clazz.getPackageName(); // fall-back, should not happen
    }
    // end::topm[]

    @Test
    void testHost() {
        Slice block = dessert.slice("..util..*");
        block.partitionBy(SlicePartitioners.HOST)
                .forEach((k, s) -> System.out.printf("%s[%d]%n", k, s.getClazzes().size()));
        // tag::host[]
        assertThatSlice(block.partitionBy(SlicePartitioners.HOST)).isCycleFree();
        // end::host[]
    }
}
