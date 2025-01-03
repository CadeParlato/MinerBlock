package net.cade.goofytestmod.block;

import net.cade.goofytestmod.GoofyTestMod;
import net.cade.goofytestmod.block.custom.AngryBlock;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class ModBlocks {

    public static final Block COOL_BLOCK = registerBlock("cool_block", Block::new, Block.Settings.create()
            .strength(0.5f).sounds(BlockSoundGroup.BONE));
    public static final Block ANGRY_BLOCK = registerBlock("angry_block", AngryBlock::new, AbstractBlock.Settings.create()
            .strength(0.3f).sounds(BlockSoundGroup.ANVIL));

    private static Block registerBlock(String name, Function<Block.Settings, Block> factory, Block.Settings settings) {
        final RegistryKey<Block> registryKey = RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(GoofyTestMod.MOD_ID, name));
        final Block block = Blocks.register(registryKey, factory, settings);
        //Apparently the items register can just take a block :)
        Items.register(block);
        return block;
    }

    public static void RegisterModBlocks() {
        GoofyTestMod.LOGGER.info("Registering mod blocks for " + GoofyTestMod.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(entry -> {
            entry.add(COOL_BLOCK);
            entry.add(ANGRY_BLOCK);
        });
    }

}
