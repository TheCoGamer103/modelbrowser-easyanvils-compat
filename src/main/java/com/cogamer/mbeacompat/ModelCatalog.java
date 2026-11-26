package com.cogamer.mbeacompat;

import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/** The full (unfiltered) model list Model Browser found, so hidden entries can be turned back on. */
public final class ModelCatalog {
    private static final Logger LOGGER = LoggerFactory.getLogger("mbeacompat");
    private static volatile List<ItemStack> all = List.of();

    private ModelCatalog() {}

    public static void capture(List<ItemStack> stacks) {
        all = List.copyOf(stacks);
    }

    /** namespace -> sorted model ids */
    public static Map<String, TreeSet<String>> byNamespace() {
        Map<String, TreeSet<String>> map = new TreeMap<>();
        for (ItemStack stack : all) {
            Identifier model = stack.get(DataComponents.ITEM_MODEL);
            if (model != null) map.computeIfAbsent(model.getNamespace(), k -> new TreeSet<>()).add(model.toString());
        }
        return map;
    }

    /** Hand the full list to Model Browser again; ModelListDataMixin re-applies the current filter. */
    public static void reapply() {
        try {
            Class.forName("deborn.modelbrowser.ModelListData")
                    .getMethod("setStacks", List.class)
                    .invoke(null, all);
        } catch (ReflectiveOperationException e) {
            LOGGER.error("Could not refresh Model Browser's list; press F3+T to apply", e);
        }
    }
}
