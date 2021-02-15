package de.spricom.dessert.sample;

import de.spricom.dessert.classfile.ClassFile;
import de.spricom.dessert.util.ClassUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class DessertSampleWrapperTest {
    private static final File docgen = new File("target/docgen");

    private DessertSampleTest dessertSampleTest = new DessertSampleTest();

    @BeforeEach
    void init() {
        docgen.mkdirs();
    }

    @Test
    void checkWillFail() throws IOException {
        try {
            dessertSampleTest.willFail();
        } catch (AssertionError er) {
            String message = er.toString();
            Files.writeString(Path.of(docgen.getPath(), "willFail.txt"), message);
            Assertions.assertEquals("java.lang.AssertionError: Illegal Dependencies:\n" +
                    "de.spricom.dessert.sample.DessertSampleTest\n" +
                    " -> org.junit.jupiter.api.Test", message.trim());
        }
    }

    @Test
    void checkWillSucceed() {
        dessertSampleTest.willSucceed();
    }

    @Test
    void showUri() throws IOException {
        System.out.println(ClassUtils.getURI(List.class));
        var cf = new ClassFile(List.class);
        cf.getDependentClasses().forEach(System.out::println);
    }
}
