package com.github.draylar.modid.common;

import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Blocks
{
    static
    {

    }

    public static void init() { }

    private static Block register(String name, Block item)
    {
        return Registry.register(Registry.BLOCK, new Identifier("modid", name), item);
    }
}
