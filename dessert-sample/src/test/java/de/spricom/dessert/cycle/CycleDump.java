package de.spricom.dessert.cycle;

import de.spricom.dessert.cycle.foo.Foo;
import de.spricom.dessert.util.ClassUtils;

public class CycleDump {
    public static final boolean DEBUG = false;

    public static void main(String [] args) {
        System.out.println(ClassUtils.getURI(Foo.class));
    }
}
