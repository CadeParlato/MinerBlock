package net.cade.goofytestmod.entity.custom;

import net.cade.goofytestmod.entity.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.SingleStackInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;

public class SpitterBlockEntity extends BlockEntity implements SingleStackInventory.SingleStackBlockEntityInventory {

    private ItemStack stack = ItemStack.EMPTY;

    public SpitterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SPITTER_BLOCK, pos, state);
    }

    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return createNbt(registryLookup);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        if (!this.stack.isEmpty()){
            nbt.put("item", this.stack.toNbt(registryLookup));
        }

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
}
