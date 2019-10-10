package com.github.draylar.modid;

import com.github.draylar.modid.common.Blocks;
import com.github.draylar.modid.common.Items;
import com.github.draylar.modid.common.Entities;
import com.github.draylar.modid.config.ModConfig;
import me.sargunvohra.mcmods.autoconfig1.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ExampleMod implements ModInitializer
{
	public static final String MODID = "modid";
	public static final Logger LOGGER = LogManager.getLogger("World Trader");
	public static final ModConfig CONFIG = AutoConfig.register(ModConfig.class, GsonConfigSerializer::new).getConfig();

	@Override
	public void onInitialize()
	{
		Blocks.init();
		Items.init();
		Entities.init();
	}
}
