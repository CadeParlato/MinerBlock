package net.cade.goofytestmod.entity.custom;

import net.cade.goofytestmod.block.ModBlocks;
import net.cade.goofytestmod.entity.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

import java.util.Objects;

public class AngryBlockEntity extends BlockEntity {

    public int angerNumber = 0;

    public AngryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ANGRY_BLOCK, pos, state);
    }

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

    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return createNbt(registryLookup);
    }

    //Serialize the block entity's data
    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        nbt.putInt("angerNumber", angerNumber);

        super.writeNbt(nbt, registryLookup);
    }

    //Deserialize the data
    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);

        angerNumber = nbt.getInt("angerNumber");
    }
}
