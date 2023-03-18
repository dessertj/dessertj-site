package org.dessertj.guide;

import org.dessertj.classfile.ClassFile;
import org.dessertj.slicing.Classpath;
import org.dessertj.slicing.Root;
import org.dessertj.slicing.Slice;

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