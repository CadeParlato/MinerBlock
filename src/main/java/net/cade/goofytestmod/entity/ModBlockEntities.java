package net.cade.goofytestmod.entity;

import net.cade.goofytestmod.GoofyTestMod;
import net.cade.goofytestmod.block.ModBlocks;
import net.cade.goofytestmod.entity.custom.AngryBlockEntity;
import net.cade.goofytestmod.entity.custom.SpitterBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlockEntities {

    //Remember that these are types, not instances of the entities themselves
    public static final BlockEntityType<AngryBlockEntity> ANGRY_BLOCK = registerBlockEntity("angry_block",
            FabricBlockEntityTypeBuilder.create(AngryBlockEntity::new, ModBlocks.ANGRY_BLOCK).build());
    public static final BlockEntityType<SpitterBlockEntity> SPITTER_BLOCK = registerBlockEntity("spitter_block",
            FabricBlockEntityTypeBuilder.create(SpitterBlockEntity::new, ModBlocks.SPITTER_BLOCK).build());

    public static <T extends BlockEntityType<?>> T registerBlockEntity(String name, T blockEntityType){
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of("goofytestmod", name), blockEntityType);
    }

    public static void Initialize(){

    }

}
