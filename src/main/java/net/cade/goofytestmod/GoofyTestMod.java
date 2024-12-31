package net.cade.goofytestmod;

import net.cade.goofytestmod.block.ModBlocks;
import net.cade.goofytestmod.item.ModItems;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GoofyTestMod implements ModInitializer {
	public static final String MOD_ID = "goofytestmod";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModItems.RegisterItems();
		ModBlocks.RegisterModBlocks();
	}
}