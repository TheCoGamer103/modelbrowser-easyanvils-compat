package com.cogamer.mbeacompat.mixin;

import com.cogamer.mbeacompat.HiddenNamespaces;
import com.cogamer.mbeacompat.ModelCatalog;
import com.cogamer.mbeacompat.ModelListDump;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;

/** Drops hidden models before Model Browser stores its list (anvil panel and creative tab). */
@Mixin(targets = "deborn.modelbrowser.ModelListData", remap = false)
public abstract class ModelListDataMixin {
    @ModifyVariable(method = "setStacks", at = @At("HEAD"), argsOnly = true, require = 0)
    private static List<ItemStack> mbeacompat$hideNamespaces(List<ItemStack> stacks) {
        ModelCatalog.capture(stacks);
        HiddenNamespaces.Hidden hidden = HiddenNamespaces.load();
        ModelListDump.write(stacks, hidden);
        if (hidden.isEmpty()) return stacks;
        return stacks.stream().filter(stack -> {
            Identifier model = stack.get(DataComponents.ITEM_MODEL);
            return model == null || !hidden.hides(model.getNamespace(), model.toString());
        }).toList();
    }
}
