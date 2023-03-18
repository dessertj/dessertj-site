package org.dessertj.concepts.cycle.bar;

import org.dessertj.concepts.cycle.CycleDump;

public class Bar {

    private void go() {
        if (CycleDump.DEBUG) {
            System.out.println("debug");
        }
    }
}
