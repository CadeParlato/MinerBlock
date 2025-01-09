package net.cade.goofytestmod.block.custom;

import com.mojang.serialization.MapCodec;
import net.cade.goofytestmod.block.ModBlocks;
import net.cade.goofytestmod.entity.ModBlockEntities;
import net.cade.goofytestmod.entity.custom.SpitterBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.Orientation;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ToolComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.block.WireOrientation;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
import java.util.Set;

public class SpitterBlock extends BlockWithEntity {

    public static final int ACTIVATION_DELAY = 20;
    public static final BooleanProperty TRIGGERED = Properties.TRIGGERED;
    public static final BooleanProperty FULL = Properties.SLOT_0_OCCUPIED; //Just use this as a stand-in property
    public static final EnumProperty<Orientation> ORIENTATION = Properties.ORIENTATION;
    private static final Set<TagKey<Item>> VALID_ITEM_TAGS = Set.of(
            ItemTags.PICKAXES,
            ItemTags.AXES,
            ItemTags.SHOVELS,
            ItemTags.HOES
    );

    public SpitterBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState(
                this.getStateManager().getDefaultState().with(ORIENTATION, Orientation.NORTH_UP).with(TRIGGERED, Boolean.valueOf(false))
                        .with(FULL, Boolean.valueOf(false))
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
    protected boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof SpitterBlockEntity spitterBlockEntity){
            if (canBreakAhead(state, world, pos, spitterBlockEntity.getStack())
                && !state.get(TRIGGERED)){
                return 15;
            }
        }
        return 0;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.getBlockEntity(pos) instanceof SpitterBlockEntity spitterBlockEntity){
            if (!world.isClient()) {
                //player.sendMessage(Text.literal("Delay left: " + spitterBlockEntity.getMineTicksRemaining()), true);
                ItemStack currentStack = spitterBlockEntity.getStack();
                if (!currentStack.isEmpty()) {
                    //Remove contents when clicking without pick
                    spitterBlockEntity.giveStackToPlayer(player);
                    spitterBlockEntity.markDirty();
                    //Effects
                    world.setBlockState(pos, state.with(FULL, Boolean.valueOf(false)), Block.NOTIFY_ALL);
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
                if (isItemValid(stack) && currentStack.isEmpty()){
                    //Test text, remove later
                    //player.sendMessage(Text.literal("Used an item"), true);
                    //Take the player's item and store it in the block
                    ItemStack newStack = stack.splitUnlessCreative(1, player);
                    spitterBlockEntity.setStack(newStack);

                    //Effects
                    world.setBlockState(pos, state.with(FULL, Boolean.valueOf(true)), Block.NOTIFY_LISTENERS);
                    world.playSound(null, pos, SoundEvents.BLOCK_DECORATED_POT_INSERT, SoundCategory.BLOCKS, 1.0F, 0.7F + 0.5F);
                    if (world instanceof ServerWorld serverWorld) {
                        serverWorld.spawnParticles(ParticleTypes.DUST_PLUME, (double)pos.getX() + 0.5, (double)pos.getY() + 1.2,
                                (double)pos.getZ() + 0.5, 7, 0.0, 0.0, 0.0, 0.0);
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
        if (world.isClient) {
            return; //We need to make sure we can cast to serverworld later
        }
        if (world.getBlockEntity(pos) instanceof SpitterBlockEntity spitterBlockEntity){
            //Update comparators
            world.updateComparators(pos, ModBlocks.SPITTER_BLOCK);

            boolean bl = world.isReceivingRedstonePower(pos) || world.isReceivingRedstonePower(pos.up());
            boolean bl2 = state.get(TRIGGERED);
            if (bl && !bl2) {
                //Only do something if the delay timer is zero and the block ahead is valid
                boolean canBreak = canBreakAhead(state, world, pos, spitterBlockEntity.getStack());
                if (spitterBlockEntity.getMineTicksRemaining() == 0 && canBreak){
                    spitterBlockEntity.setMineTicksRemaining(ACTIVATION_DELAY);
                    world.scheduleBlockTick(pos, this, 2);
                }else{
                    world.playSound(null, pos, SoundEvents.BLOCK_CRAFTER_FAIL, SoundCategory.BLOCKS, 1.5F, 0.6F);
                }
                world.setBlockState(pos, state.with(TRIGGERED, Boolean.valueOf(true)), Block.NOTIFY_LISTENERS);
            }else if(!bl && bl2){
                //Make sure we toggle TRIGGERED if unpowered after being triggered past the entity's delay timer
                if (spitterBlockEntity.getMineTicksRemaining() == 0){
                    world.setBlockState(pos, state.with(TRIGGERED, Boolean.valueOf(false)), Block.NOTIFY_LISTENERS);
                }
            }
        }
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!world.isClient()){
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof SpitterBlockEntity spitterBlockEntity){
                breakBlockAhead(state, world, pos, spitterBlockEntity);
            }
        }
    }

    //If the block is meant to be broken with the inserted tool, return true (doesn't need serverworld)
    public boolean canBreakAhead(BlockState state, World world, BlockPos pos, ItemStack stack) {
        Direction direction = state.get(ORIENTATION).getFacing();
        BlockPos breakPos = pos.offset(direction);
        BlockState targetState = world.getBlockState(breakPos);
        return stack.isSuitableFor(targetState) && stack.getDamage() < stack.getMaxDamage() - 1;
    }

    //Breaks the block in front, taking orientation into account
    //Checks block breaking validity again in case something was changed
    public void breakBlockAhead(BlockState state, ServerWorld world, BlockPos pos, SpitterBlockEntity spitterBlockEntity) {
        Direction direction = state.get(ORIENTATION).getFacing();
        BlockPos breakPos = pos.offset(direction);
        BlockState targetState = world.getBlockState(breakPos);
        ItemStack stack = spitterBlockEntity.getStack();
        if(stack.isSuitableFor(targetState) && stack.getDamage() < stack.getMaxDamage() - 1) {
            ToolComponent toolComponent = stack.get(DataComponentTypes.TOOL);
            if (toolComponent != null) {
                if (targetState.getHardness(world, breakPos) != 0.0F && toolComponent.damagePerBlock() > 0) {
                    stack.damage(1, world, null, item -> {});
                    spitterBlockEntity.markDirty();
                }
            }

            world.syncWorldEvent(null, WorldEvents.BLOCK_BROKEN, breakPos, getRawIdFromState(targetState));
            world.removeBlock(breakPos, false);
            Block.dropStacks(targetState, world, breakPos, null, null, stack);
            world.emitGameEvent(GameEvent.BLOCK_DESTROY, breakPos, GameEvent.Emitter.of(null, targetState));
            world.playSound(null, breakPos, SoundEvents.BLOCK_CRAFTER_CRAFT, SoundCategory.BLOCKS, 1.0F, 0.8F);
        }
    }

    public static boolean isItemValid(ItemStack stack) {
        for (TagKey<Item> tag : VALID_ITEM_TAGS){
            if (stack.isIn(tag)){
                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (type != ModBlockEntities.SPITTER_BLOCK) {return null;}
        return world.isClient ? null : validateTicker(type, ModBlockEntities.SPITTER_BLOCK, SpitterBlockEntity::tickMineTimer);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(ORIENTATION, TRIGGERED, FULL);
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
