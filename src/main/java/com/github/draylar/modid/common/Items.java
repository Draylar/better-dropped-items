package com.github.draylar.modid.common;

import com.github.draylar.modid.ExampleMod;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Items
{


    public static void init() {
        // NO-OP
    }

    private static Item register(String name, Item item)
    {
        return Registry.register(Registry.ITEM, new Identifier(ExampleMod.MODID, name), item);
    }

    private Items() {
        // NO-OP
    }
}
