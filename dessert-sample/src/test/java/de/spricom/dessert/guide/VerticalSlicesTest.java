package de.spricom.dessert.guide;

import de.spricom.dessert.slicing.Classpath;
import de.spricom.dessert.slicing.Root;
import de.spricom.dessert.slicing.Slice;
import de.spricom.dessert.stampshop.application.ShopApplication;
import de.spricom.dessert.util.CombinationUtils;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static de.spricom.dessert.assertions.SliceAssertions.assertThatSlice;
import static de.spricom.dessert.assertions.SliceAssertions.assertThatSlices;
import static org.assertj.core.api.Assertions.assertThat;

public class VerticalSlicesTest {
    private static final Classpath cp = new Classpath();

    @Test
    void testNoCrossDependencies1() {
        // tag::parts[]
        Root stampShop = cp.rootOf(ShopApplication.class);
        Slice parts = stampShop.slice("..stampshop.parts..*"); // the parent package of all parts
        Map<String, ? extends Slice> partsByName = parts.partitionBy(clazz ->
                clazz.getPackageName().split("\\.", 6)[5]); // partition by sub-package name
        partsByName.forEach((nameA, partA) -> partsByName.forEach((nameB, partB) -> {
            if (!(nameA.equals(nameB))) {
                assertThatSlice(partA).doesNotUse(partB);
            }
        }));
        // end::parts[]
    }

    @Test
    void testNoCrossDependencies2() {
        Root stampShop = cp.rootOf(ShopApplication.class);
        Slice parts = stampShop.slice("..stampshop.parts..*"); // the parent package of all parts
        Map<String, ? extends Slice> partsByName = parts.partitionBy(clazz ->
                clazz.getPackageName().split("\\.", 6)[5]); // partition by sub-package name
        // tag::combinations[]
        CombinationUtils.combinations(new ArrayList<>(partsByName.values()))
                .forEach(pair -> assertThatSlice(pair.getLeft()).doesNotUse(pair.getRight()));
        // end::combinations[]
    }

    @Test
    void testLayeredStrict() {
        // tag::layered[]
        Root stampShop = cp.rootOf(ShopApplication.class);
        Slice application = stampShop.slice("..stampshop.application..*");
        Slice parts = stampShop.slice("..stampshop.parts..*");
        Slice commons = stampShop.slice("..stampshop.commons..*");

        assertThatSlices(application, parts, commons).areLayeredStrict();
        // end::layered[]
        List.of(application, parts, commons).forEach(slice -> assertThat(slice.getClazzes()).isNotEmpty());
    }

    @Test
    void testLayeredRelaxed() {
        Root stampShop = cp.rootOf(ShopApplication.class);
        Slice application = stampShop.slice("..stampshop.application..*");
        Slice parts = stampShop.slice("..stampshop.parts..*");
        Slice common = stampShop.slice("..stampshop.common..*");

        // tag::relaxed[]
        assertThatSlices(application, parts, common).areLayeredRelaxed();
        // end::relaxed[]
    }

}
