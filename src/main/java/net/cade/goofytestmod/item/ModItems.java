package net.cade.goofytestmod.item;

import net.cade.goofytestmod.GoofyTestMod;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class ModItems {

    public static final Item GOOBER = registerItem("goober", Item::new, new Item.Settings());

    public static Item registerItem(String name, Function<Item.Settings, Item> factory, Item.Settings settings) {
        final RegistryKey<Item> registryKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(GoofyTestMod.MOD_ID, name));
        return Items.register(registryKey, factory, settings);
    }

    public static void RegisterItems(){
        GoofyTestMod.LOGGER.info("Registering items for " + GoofyTestMod.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entry -> {
            entry.add(GOOBER);
        });
    }

}
