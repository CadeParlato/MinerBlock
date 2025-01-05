package net.cade.goofytestmod.block.custom;

import com.mojang.serialization.MapCodec;
import net.cade.goofytestmod.entity.custom.SpitterBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.Orientation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.block.WireOrientation;
import org.jetbrains.annotations.Nullable;

public class SpitterBlock extends BlockWithEntity {

    public static final BooleanProperty TRIGGERED = Properties.TRIGGERED;
    private static final EnumProperty<Orientation> ORIENTATION = Properties.ORIENTATION;

    public SpitterBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState(
                this.getStateManager().getDefaultState().with(ORIENTATION, Orientation.NORTH_UP).with(TRIGGERED, Boolean.valueOf(false))
        );
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
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.getBlockEntity(pos) instanceof SpitterBlockEntity spitterBlockEntity){
            if (!world.isClient()) {
                ItemStack currentStack = spitterBlockEntity.getStack();
                if (!currentStack.isEmpty()) {
                    //Remove contents when clicking without pick
                    spitterBlockEntity.giveStackToPlayer(player);
                    //Effects
                    world.playSound(null, pos, SoundEvents.ITEM_BUNDLE_REMOVE_ONE, SoundCategory.BLOCKS, 1.0F, 1);
                }else{
                    world.playSound(null, pos, SoundEvents.BLOCK_CRAFTER_FAIL, SoundCategory.BLOCKS, 2.0F, 1);
                }
            }
            return ActionResult.SUCCESS;
        }else{
            return ActionResult.PASS;
        }
    }

    @Override
    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.getBlockEntity(pos) instanceof SpitterBlockEntity spitterBlockEntity){
            if (world.isClient()){
                return ActionResult.SUCCESS;
            }else{
                ItemStack currentStack = spitterBlockEntity.getStack();
                if (stack.isIn(TagKey.of(RegistryKeys.ITEM, Identifier.of("minecraft", "pickaxes")))
                && currentStack.isEmpty()){
                    //Test text, remove later
                    player.sendMessage(Text.literal("Used an item"), true);
                    //Take the player's pickaxe and store it in the block
                    ItemStack newStack = stack.splitUnlessCreative(1, player);
                    if (spitterBlockEntity.isEmpty()){
                        spitterBlockEntity.setStack(newStack);
                    }

                    //Effects
                    world.playSound(null, pos, SoundEvents.BLOCK_DECORATED_POT_INSERT, SoundCategory.BLOCKS, 1.0F, 0.7F + 0.5F);
                    if (world instanceof ServerWorld serverWorld) {
                        serverWorld.spawnParticles(ParticleTypes.DUST_PLUME, (double)pos.getX() + 0.5, (double)pos.getY() + 1.2, (double)pos.getZ() + 0.5, 7, 0.0, 0.0, 0.0, 0.0);
                    }

                    spitterBlockEntity.markDirty();
                    return  ActionResult.SUCCESS;
                }else{
                    return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
                }
            }
        }else {
            return ActionResult.PASS;
        }
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, @Nullable WireOrientation wireOrientation, boolean notify) {
        boolean bl = world.isReceivingRedstonePower(pos) || world.isReceivingRedstonePower(pos.up());
        boolean bl2 = (Boolean)state.get(TRIGGERED);
        if (bl && !bl2) {
            world.scheduleBlockTick(pos, this, 4);
            world.setBlockState(pos, state.with(TRIGGERED, Boolean.valueOf(true)), Block.NOTIFY_LISTENERS);
        } else if (!bl && bl2) {
            world.setBlockState(pos, state.with(TRIGGERED, Boolean.valueOf(false)), Block.NOTIFY_LISTENERS);
        }
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!world.isClient()){
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof SpitterBlockEntity spitterBlockEntity){
                spitterBlockEntity.explode();
            }
        }
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(ORIENTATION, TRIGGERED);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction direction1 = ctx.getPlayerLookDirection().getOpposite();
        Direction direction2 = switch(direction1){
            case DOWN -> ctx.getHorizontalPlayerFacing().getOpposite();
            case UP -> ctx.getHorizontalPlayerFacing();
            case NORTH, EAST, SOUTH, WEST -> Direction.UP;
        };

        return this.getDefaultState().with(Properties.ORIENTATION, Orientation.byDirections(direction1, direction2));
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        ItemScatterer.onStateReplaced(state, newState, world, pos);
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(ORIENTATION, rotation.getDirectionTransformation().mapJigsawOrientation(state.get(ORIENTATION)));
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.with(ORIENTATION, mirror.getDirectionTransformation().mapJigsawOrientation(state.get(ORIENTATION)));
    }
}
