package com.github.draylar.modid.common;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Entities
{
    static
    {

    }

    public static void init() { }

    private static <T extends BlockEntity> BlockEntityType<T> register(String name, BlockEntityType<T> be)
    {
        return Registry.register(Registry.BLOCK_ENTITY, new Identifier("wrath", name), be);
    }
}
