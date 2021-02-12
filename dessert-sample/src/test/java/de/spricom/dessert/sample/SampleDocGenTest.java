package de.spricom.dessert.sample;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SampleDocGenTest {
    private static final File docgen = new File("target/docgen");

    @BeforeEach
    void init() {
        docgen.mkdirs();
    }

    @Test
    void generateFailureSample() throws IOException {
        try {
            new DessertSampleTest().willFail();
        } catch (AssertionError er) {
            String message = er.toString();
            Files.writeString(Path.of(docgen.getPath(), "willFail.txt"), message);
        }
    }
}
