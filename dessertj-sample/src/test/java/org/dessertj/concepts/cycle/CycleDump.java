package org.dessertj.concepts.cycle;

import org.dessertj.concepts.cycle.foo.Foo;
import org.dessertj.util.ClassUtils;

public class CycleDump {
    public static final boolean DEBUG = false;

    public static void main(String [] args) {
        System.out.println(ClassUtils.getURI(Foo.class));
    }
}
