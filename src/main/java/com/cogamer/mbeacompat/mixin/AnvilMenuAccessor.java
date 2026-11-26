package com.cogamer.mbeacompat.mixin;

import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AnvilMenu.class)
public interface AnvilMenuAccessor {
    @Accessor("itemName")
    String mbeacompat$getItemName();

    @Accessor("cost")
    DataSlot mbeacompat$getCost();
}
