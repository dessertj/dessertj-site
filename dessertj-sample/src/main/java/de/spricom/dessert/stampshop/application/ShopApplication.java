package de.spricom.dessert.stampshop.application;

import de.spricom.dessert.stampshop.parts.part1.ShopPart1;
import de.spricom.dessert.stampshop.parts.part2.ShopPart2;
import de.spricom.dessert.stampshop.parts.part3.ShopPart3;
import de.spricom.dessert.stampshop.parts.part3.SomeUtil;

public class ShopApplication {
    private final ShopPart1 part1 = new ShopPart1();
    private final ShopPart2 part2 = new ShopPart2();
    private final ShopPart3 part3 = new ShopPart3();
    // private final ShopCommons commons = new ShopCommons();

    private SomeUtil util;
}
