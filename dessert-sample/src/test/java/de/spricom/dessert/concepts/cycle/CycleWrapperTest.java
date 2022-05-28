package de.spricom.dessert.concepts.cycle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class CycleWrapperTest {
    private static final File docgen = new File("target/docgen");

    private final CycleTest cycleTest = new CycleTest();

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

            assertThat(message.trim().split("\n"))
                    .hasSize(4)
                    .contains("java.lang.AssertionError: Cycle detected:",
                            "de.spricom.dessert.concepts.cycle.foo.Foo -> de.spricom.dessert.concepts.cycle.bar.Bar",
                            "de.spricom.dessert.concepts.cycle.bar.Bar -> de.spricom.dessert.concepts.cycle.CycleDump",
                            "de.spricom.dessert.concepts.cycle.CycleDump -> de.spricom.dessert.concepts.cycle.foo.Foo");
        }
    }

    @Test
    void checkPackageCycle() throws IOException {
        try {
            cycleTest.packageCycle();
        } catch (AssertionError er) {
            String message = er.toString();
            Files.writeString(Path.of(docgen.getPath(), "packageCycle.txt"), message);

            assertThat(message.trim().split("\n"))
                    .hasSize(7)
                    .contains("java.lang.AssertionError: Cycle detected:",
                            "de.spricom.dessert.concepts.cycle.bar -> de.spricom.dessert.concepts.cycle:",
                            "	Bar -> CycleDump",
                            "de.spricom.dessert.concepts.cycle -> de.spricom.dessert.concepts.cycle.foo:",
                            "	CycleDump -> Foo",
                            "de.spricom.dessert.concepts.cycle.foo -> de.spricom.dessert.concepts.cycle.bar:",
                            "	Foo -> Bar");
        }
    }
}
