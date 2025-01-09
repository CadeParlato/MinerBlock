package net.cade.goofytestmod.entity.custom;

import net.cade.goofytestmod.block.ModBlocks;
import net.cade.goofytestmod.block.custom.SpitterBlock;
import net.cade.goofytestmod.entity.ModBlockEntities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SingleStackInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

import java.util.Objects;

import static net.minecraft.block.Block.getRawIdFromState;

public class SpitterBlockEntity extends BlockEntity implements SingleStackInventory.SingleStackBlockEntityInventory {

    private ItemStack stack = ItemStack.EMPTY;
    private int mineTicksRemaining = 0;

    public SpitterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SPITTER_BLOCK, pos, state);
    }

//    @Override
//    public Packet<ClientPlayPacketListener> toUpdatePacket() {
//        return BlockEntityUpdateS2CPacket.create(this);
//    }
//
//    @Override
//    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
//        //return createNbt(registryLookup);
//        return createComponentlessNbt(registryLookup);
//    }

    public void explode() {
        Objects.requireNonNull(this.getWorld())
                .createExplosion(
                        null,
                        Explosion.createDamageSource(this.getWorld(), null),
                        null,
                        this.pos.getX(),
                        this.pos.getY(),
                        this.pos.getZ(),
                        2,
                        false,
                        World.ExplosionSourceType.TNT
                );
    }

    public static void tickMineTimer(World world, BlockPos pos, BlockState state, SpitterBlockEntity blockEntity) {
        int i = blockEntity.mineTicksRemaining - 1;
        if (i >= 0) {
            blockEntity.mineTicksRemaining = i;
        }if (i <= 0){
            //Only look untriggered when timer is over
            if (!world.isReceivingRedstonePower(pos)) {
                world.setBlockState(pos, state.with(SpitterBlock.TRIGGERED, Boolean.valueOf(false)), Block.NOTIFY_ALL);
            }
        }
    }

    public int getMineTicksRemaining() {
        return mineTicksRemaining;
    }

    public void setMineTicksRemaining(int ticks) {
        mineTicksRemaining = ticks;
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        if (!this.stack.isEmpty()){
            nbt.put("item", this.stack.toNbt(registryLookup));
        }

        nbt.putInt("mine_ticks_remaining", mineTicksRemaining);

        super.writeNbt(nbt, registryLookup);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);

        if (nbt.contains("item", NbtElement.COMPOUND_TYPE)) {
            this.stack = ItemStack.fromNbt(registryLookup, nbt.getCompound("item")).orElse(ItemStack.EMPTY);
        } else {
            this.stack = ItemStack.EMPTY;
        }

        mineTicksRemaining = nbt.getInt("mine_ticks_remaining");

    }

    @Override
    public BlockEntity asBlockEntity() {
        return this;
    }

    @Override
    public ItemStack getStack() {
        return this.stack;
    }

    @Override
    public void setStack(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        if (slot != 0){
            return ItemStack.EMPTY;
        }else{
            assert world != null;
            BlockState state = world.getBlockState(this.pos);
            if (state.isOf(ModBlocks.SPITTER_BLOCK)){
                world.setBlockState(pos, state.with(SpitterBlock.FULL, Boolean.valueOf(false)), Block.NOTIFY_ALL);
            }
            return decreaseStack(amount);
        }
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (slot == 0) {
            assert world != null;
            BlockState state = world.getBlockState(this.pos);
            if (state.isOf(ModBlocks.SPITTER_BLOCK)){
                world.setBlockState(pos, state.with(SpitterBlock.FULL, Boolean.valueOf(true)), Block.NOTIFY_ALL);
            }
            this.setStack(stack);
        }
    }

    public void giveStackToPlayer(PlayerEntity player) {
        ItemStack removedStack = stack.copyAndEmpty();
        boolean didGive = player.getInventory().insertStack(removedStack);
        if (!didGive) {
            player.dropItem(removedStack, false);
        }
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return stack.isIn(TagKey.of(RegistryKeys.ITEM, Identifier.of("minecraft", "pickaxes")));
    }

}
