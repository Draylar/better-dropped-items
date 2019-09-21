package com.github.draylar.modid.common;

import com.github.draylar.modid.ExampleMod;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Entities
{


    public static void init() {
        // NO-OP
    }

    private static <T extends BlockEntity> BlockEntityType<T> register(String name, BlockEntityType<T> be)
    {
        return Registry.register(Registry.BLOCK_ENTITY, new Identifier(ExampleMod.MODID, name), be);
    }

    private static <T extends Entity> EntityType<T> register(String name, EntityType<T> be)
    {
        return Registry.register(Registry.ENTITY_TYPE, new Identifier(ExampleMod.MODID, name), be);
    }

    private Entities() {
        // NO-OP
    }
}
