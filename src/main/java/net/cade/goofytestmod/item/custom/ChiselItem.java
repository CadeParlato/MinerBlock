package net.cade.goofytestmod.item.custom;

import net.cade.goofytestmod.block.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;

import java.util.Map;

public class ChiselItem extends Item {
    private static final Map<Block, Block> SWAP_MAP =
            Map.of(
                    Blocks.DIAMOND_BLOCK, ModBlocks.COOL_BLOCK,
                    Blocks.NETHERITE_BLOCK, ModBlocks.COOL_BLOCK
            );

    public ChiselItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        Block clickedBlock = world.getBlockState(context.getBlockPos()).getBlock();

        if (SWAP_MAP.containsKey(clickedBlock)){
            if (!world.isClient()){
                world.setBlockState(context.getBlockPos(), SWAP_MAP.get(clickedBlock).getDefaultState());

                context.getStack().damage(1, (ServerWorld) world, (ServerPlayerEntity) context.getPlayer(),
                        item -> context.getPlayer().sendEquipmentBreakStatus(item, EquipmentSlot.MAINHAND));

                world.playSound(null, context.getBlockPos(), SoundEvents.BLOCK_BEEHIVE_SHEAR,
                        SoundCategory.BLOCKS);
            }
        }

        return ActionResult.SUCCESS;
    }
}
