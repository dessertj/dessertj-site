package de.spricom.dessert.cycle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;


public class CycleWrapperTest {
    private static final File docgen = new File("target/docgen");

    private CycleTest cycleTest = new CycleTest();

    @BeforeEach
    void init() {
        docgen.mkdirs();
    }

    @Test
    void checkClassCycle() throws IOException {
        try {
            cycleTest.classCycle();
        } catch (AssertionError er) {
            String message = er.toString();
            Files.writeString(Path.of(docgen.getPath(), "classCycle.txt"), message);

            // URI is different on each target system
            assertThat(message)
                    .containsOnlyOnce("java.lang.AssertionError: Cycle:")
                    .containsOnlyOnce("/de/spricom/dessert/cycle/bar/Bar.class")
                    .containsOnlyOnce("/de/spricom/dessert/cycle/CycleDump.class")
                    .contains("/de/spricom/dessert/cycle/foo/Foo.class");

            assertThat(message.trim().split(",?\n")).hasSize(5);
        }
    }

    @Test
    void checkPackageCycle() throws IOException {
        try {
            cycleTest.packageCycle();
        } catch (AssertionError er) {
            String message = er.toString();
            Files.writeString(Path.of(docgen.getPath(), "packageCycle.txt"), message);

            assertThat(message.trim().split(",?\n"))
                    .hasSize(5)
                    .contains("java.lang.AssertionError: Cycle:",
                            "slice partition de.spricom.dessert.cycle.foo",
                            "slice partition de.spricom.dessert.cycle.bar",
                            "slice partition de.spricom.dessert.cycle");
        }
    }
}
