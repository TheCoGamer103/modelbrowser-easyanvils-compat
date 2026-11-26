package com.cogamer.mbeacompat;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/** Writes config/mbeacompat-models.txt: every model Model Browser found, grouped by namespace. */
public final class ModelListDump {
    private static final Logger LOGGER = LoggerFactory.getLogger("mbeacompat");

    private ModelListDump() {}

    public static void write(List<ItemStack> stacks, HiddenNamespaces.Hidden hidden) {
        Map<String, TreeSet<String>> byNamespace = new TreeMap<>();
        for (ItemStack stack : stacks) {
            Identifier model = stack.get(DataComponents.ITEM_MODEL);
            if (model == null) continue;
            byNamespace.computeIfAbsent(model.getNamespace(), k -> new TreeSet<>()).add(model.toString());
        }
        StringBuilder out = new StringBuilder("# Every model in Model Browser, grouped by namespace (mod id).\n")
                .append("# [hidden] = hidden by config/mbeacompat.properties. Regenerated on F3+T.\n\n");
        byNamespace.forEach((namespace, ids) -> {
            boolean nsHidden = hidden.namespaces().contains(namespace);
            out.append("== ").append(namespace).append(" (").append(ids.size()).append(")")
                    .append(nsHidden ? " [hidden]" : "").append('\n');
            for (String id : ids) {
                out.append("  ").append(id).append(!nsHidden && hidden.models().contains(id) ? " [hidden]" : "").append('\n');
            }
            out.append('\n');
        });
        Path file = FabricLoader.getInstance().getConfigDir().resolve("mbeacompat-models.txt");
        try {
            Files.writeString(file, out);
        } catch (Exception e) {
            LOGGER.warn("Could not write {}", file, e);
        }
    }
}
