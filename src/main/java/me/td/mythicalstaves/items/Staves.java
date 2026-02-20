package me.td.mythicalstaves.items;

import me.td.mythicalstaves.MythicalStaves;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class Staves {

    public static final StaffOfIceItem STAFF_OF_ICE = register("staff_of_ice", new StaffOfIceItem(
            new Item.Settings().maxCount(1).maxDamage(6)
    ));
    public static final StaffOfFireItem STAFF_OF_FIRE = register("staff_of_fire", new StaffOfFireItem(
            new Item.Settings().maxCount(1).maxDamage(6).fireproof()
    ));
    public static final StaffOfZeusItem STAFF_OF_LIGHTNING = register("staff_of_zeus", new StaffOfZeusItem(
            new Item.Settings().maxCount(1).maxDamage(6).fireproof()
    ));
    public static final StaffOfTheDepthsItem STAFF_OF_THE_DEPTHS = register("staff_of_the_depths", new StaffOfTheDepthsItem(
            new Item.Settings().maxCount(1).maxDamage(6)
    ));
    public static final StaffOfGamblingItem STAFF_OF_GAMBLING = register("staff_of_gambling", new StaffOfGamblingItem(
            new Item.Settings().maxCount(1).maxDamage(24)
    ));

    public static final ItemGroup MS_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(Staves.STAFF_OF_ICE))
            .displayName(Text.translatable("itemGroup.mythical_staves"))
            .entries((context, entries) -> {
                entries.add(STAFF_OF_ICE);
                entries.add(STAFF_OF_FIRE);
                entries.add(STAFF_OF_LIGHTNING);
                entries.add(STAFF_OF_THE_DEPTHS);
                entries.add(STAFF_OF_GAMBLING);
            })
            .build();

    public static <T extends Item> T register(String path, T item) {
        return Registry.register(Registries.ITEM, Identifier.of(MythicalStaves.MOD_ID, path), item);
    }

    public static void initialize() { // Hack to statically initialize the class. This gets called in the mod
        Registry.register(Registries.ITEM_GROUP, Identifier.of(MythicalStaves.MOD_ID, "mythical_staves"), MS_GROUP);
    }
}