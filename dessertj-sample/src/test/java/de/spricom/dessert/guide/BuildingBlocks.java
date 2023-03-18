package de.spricom.dessert.guide;

import de.spricom.dessert.classfile.ClassFile;
import de.spricom.dessert.slicing.Classpath;
import de.spricom.dessert.slicing.Root;
import de.spricom.dessert.slicing.Slice;

// tag::code[]
public final class BuildingBlocks {
    public static final Classpath cp = new Classpath();
    public static final Root dessert = cp.rootOf(Slice.class);
    public static final Slice classfile = dessert.packageTreeOf(ClassFile.class);
    public static final Slice slicing = dessert.packageTreeOf(Slice.class);
    public static final Slice java = cp.slice("java.lang|util|io|net..*");

    private BuildingBlocks() {
    }
}
// end::code[]