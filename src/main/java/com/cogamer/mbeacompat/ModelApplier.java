package com.cogamer.mbeacompat;

import com.cogamer.mbeacompat.mixin.AnvilMenuAccessor;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.equipment.Equippable;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Mirrors Model Browser's anvil result logic ("namespace:path" name, optional "-egG" flags). */
public final class ModelApplier {
    static final Pattern MODEL_ID_PATTERN =
            Pattern.compile("^([a-z0-9_.-]+:[a-z0-9_/.-]+)(?:\\s+-([a-zA-Z]+))?$");
    private static Method namespaceCheck;
    private static boolean namespaceCheckResolved;

    private ModelApplier() {}

    public static void apply(AnvilMenu menu) {
        String name = ((AnvilMenuAccessor) menu).mbeacompat$getItemName();
        if (name == null || name.isBlank()) {
            tryReset(menu);
            return;
        }
        Matcher matcher = MODEL_ID_PATTERN.matcher(name.trim());
        if (!matcher.matches()) return;
        Identifier id = Identifier.tryParse(matcher.group(1));
        if (id == null || !isNamespaceAllowed(id.getNamespace())) return;

        Slot output = menu.getSlot(2);
        if (!output.hasItem()) return;
        ItemStack input = menu.getSlot(0).getItem();
        String flags = matcher.group(2) == null ? "" : matcher.group(2);

        ItemStack result = output.getItem().copy();
        result.set(DataComponents.ITEM_MODEL, id);

        // Keep the item's previous custom name (the model id is not meant to become its name).
        Component previousName = input.get(DataComponents.CUSTOM_NAME);
        if (previousName != null) result.set(DataComponents.CUSTOM_NAME, previousName);
        else result.remove(DataComponents.CUSTOM_NAME);

        // Helmets/caps: drop the armor look so the item model renders on the head instead.
        Equippable inputEquippable = input.get(DataComponents.EQUIPPABLE);
        if (inputEquippable != null && inputEquippable.slot() == EquipmentSlot.HEAD) {
            result.set(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD).build());
        } else if (flags.indexOf('e') >= 0 && inputEquippable == null) {
            result.set(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD).build());
        }
        if (flags.indexOf('g') >= 0) result.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, false);
        if (flags.indexOf('G') >= 0) result.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);

        CompoundTag tag = result.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putBoolean("model_browser_data", true);
        result.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        output.set(result);
    }

    /**
     * Empty name on an item that got its model from Model Browser: give back the item's own model,
     * name, wearable behavior and glint (Model Browser does the same in the vanilla anvil).
     */
    private static void tryReset(AnvilMenu menu) {
        ItemStack input = menu.getSlot(0).getItem();
        if (input.isEmpty()) return;
        CustomData data = input.get(DataComponents.CUSTOM_DATA);
        if (data == null || !data.copyTag().getBooleanOr("model_browser_data", false)) return;

        Slot output = menu.getSlot(2);
        boolean hadResult = output.hasItem();
        ItemStack result = hadResult ? output.getItem().copy() : input.copy();
        ItemStack prototype = result.getItem().getDefaultInstance();

        resetToDefault(result, prototype, DataComponents.ITEM_MODEL);
        resetToDefault(result, prototype, DataComponents.ITEM_NAME);
        resetToDefault(result, prototype, DataComponents.EQUIPPABLE);
        resetToDefault(result, prototype, DataComponents.ENCHANTMENT_GLINT_OVERRIDE);

        CompoundTag tag = data.copyTag();
        tag.remove("model_browser_data");
        if (tag.isEmpty()) result.remove(DataComponents.CUSTOM_DATA);
        else result.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        output.set(result);
        if (!hadResult) {
            // Nothing else changed, so Easy Anvils offered no result; charge a plain rename (1 level).
            ((AnvilMenuAccessor) menu).mbeacompat$getCost().set(1);
        }
    }

    private static <T> void resetToDefault(ItemStack stack, ItemStack prototype, DataComponentType<T> type) {
        T value = prototype.get(type);
        if (value != null) stack.set(type, value);
        else stack.remove(type);
    }

    /** Respect Model Browser's server namespace setting if it is reachable. */
    private static boolean isNamespaceAllowed(String namespace) {
        if (!namespaceCheckResolved) {
            namespaceCheckResolved = true;
            try {
                namespaceCheck = Class.forName("deborn.modelbrowser.config.ServerConfig")
                        .getMethod("isNamespaceAllowed", String.class);
            } catch (ReflectiveOperationException ignored) {
                namespaceCheck = null;
            }
        }
        if (namespaceCheck == null) return !"minecraft".equals(namespace);
        try {
            return (boolean) namespaceCheck.invoke(null, namespace);
        } catch (ReflectiveOperationException e) {
            return !"minecraft".equals(namespace);
        }
    }
}
