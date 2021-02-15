package de.spricom.dessert.cycle.bar;

import de.spricom.dessert.cycle.CycleDump;

public class Bar {

    private void go() {
        if (CycleDump.DEBUG) {
            System.out.println("debug");
        }
    }
}
