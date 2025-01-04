package net.cade.goofytestmod.block.custom;

import com.mojang.serialization.MapCodec;
import net.cade.goofytestmod.entity.custom.AngryBlockEntity;
import net.cade.goofytestmod.entity.custom.SpitterBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.FacingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class SpitterBlock extends BlockWithEntity {

    public SpitterBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends SpitterBlock> getCodec() {
        return createCodec(SpitterBlock::new);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SpitterBlockEntity(pos, state);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(Properties.FACING);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FacingBlock.FACING, ctx.getPlayerLookDirection().getOpposite());
    }

}
