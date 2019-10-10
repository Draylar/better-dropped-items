package com.github.draylar.modid.config;

import com.github.draylar.modid.ExampleMod;
import me.sargunvohra.mcmods.autoconfig1.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1.ConfigManager;
import me.sargunvohra.mcmods.autoconfig1.serializer.ConfigSerializer;

public class ConfigUtils {

    private ConfigUtils() {
        // NO-OP
    }

    public static void serializeConfig() {
        try {
            ((ConfigManager<ModConfig>) AutoConfig.getConfigHolder(ModConfig.class)).getSerializer().serialize(ExampleMod.CONFIG);
        } catch (ConfigSerializer.SerializationException serializeException) {
            ExampleMod.LOGGER.error("Failed to serialize " + ExampleMod.LOGGER.getName() + "'s config!", serializeException);
        }
    }
}
