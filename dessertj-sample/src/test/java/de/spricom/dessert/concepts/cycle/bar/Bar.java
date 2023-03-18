package de.spricom.dessert.concepts.cycle.bar;

import de.spricom.dessert.concepts.cycle.CycleDump;

public class Bar {

    private void go() {
        if (CycleDump.DEBUG) {
            System.out.println("debug");
        }
    }
}
