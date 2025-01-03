package net.cade.goofytestmod.block.custom;

import com.mojang.serialization.MapCodec;
import net.cade.goofytestmod.GoofyTestMod;
import net.cade.goofytestmod.entity.custom.AngryBlockEntity;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class AngryBlock extends BlockWithEntity {

    public AngryBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient){
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof AngryBlockEntity angryBlockEntity) {

                if (player.isSneaky()){
                    angryBlockEntity.angerNumber = 0;
                } else if (angryBlockEntity.angerNumber >= 20) {
                    angryBlockEntity.explode();
                }else {
                    angryBlockEntity.angerNumber++;
                }

                player.sendMessage(Text.literal("Current anger level: " + angryBlockEntity.angerNumber), true);
                angryBlockEntity.markDirty();
            }
        }

        return ActionResult.SUCCESS;
    }

    @Override
    protected MapCodec<? extends AngryBlock> getCodec() {
        return createCodec(AngryBlock::new);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new AngryBlockEntity(pos, state);
    }

    //If invisible, override getRenderType()

}
