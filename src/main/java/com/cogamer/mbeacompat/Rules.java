package com.cogamer.mbeacompat;

import com.cogamer.mbeacompat.mixin.AnvilMenuAccessor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;

/** Blocks model renames that the config doesn't allow, by clearing the anvil result. */
public final class Rules {
    private Rules() {}

    /** @return true if the rename was a disallowed model id and the result was cleared */
    public static boolean enforce(AnvilMenu menu) {
        String name = ((AnvilMenuAccessor) menu).mbeacompat$getItemName();
        if (name == null) return false;
        var matcher = ModelApplier.MODEL_ID_PATTERN.matcher(name.trim());
        if (!matcher.matches()) return false;
        Identifier id = Identifier.tryParse(matcher.group(1));
        if (id == null) return false;

        ItemStack input = menu.getSlot(0).getItem();
        String itemId = BuiltInRegistries.ITEM.getKey(input.getItem()).toString();
        if (CompatConfig.namespaceAllowed(id.getNamespace()) && CompatConfig.itemAllowed(itemId)) return false;

        if (menu.getSlot(2).hasItem()) menu.getSlot(2).set(ItemStack.EMPTY);
        return true;
    }
}
