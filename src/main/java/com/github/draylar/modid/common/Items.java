package com.github.draylar.modid.common;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Items
{
    static
    {

    }

    public static void init() { }

    private static Item register(String name, Item item)
    {
        return Registry.register(Registry.ITEM, new Identifier("modid", name), item);
    }
}
