package com.cogamer.mbeacompat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * config/mbeacompat.json
 *   allowed_namespaces: model namespaces that may be applied / shown in the browser ([] = all)
 *   allowed_items:      item ids that may receive a model ([] = all)
 * Ship the same file to clients (e.g. with Configured Defaults) so the browser list matches.
 */
public final class CompatConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("mbeacompat");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("mbeacompat.json");

    private static Set<String> namespaces;
    private static Set<String> items;

    private CompatConfig() {}

    private static void load() {
        namespaces = new LinkedHashSet<>(List.of("headgear"));
        items = new LinkedHashSet<>(List.of("minecraft:leather_helmet"));
        try {
            if (Files.exists(FILE)) {
                JsonObject json = JsonParser.parseString(Files.readString(FILE)).getAsJsonObject();
                namespaces = read(json, "allowed_namespaces", namespaces);
                items = read(json, "allowed_items", items);
            } else {
                JsonObject json = new JsonObject();
                json.add("allowed_namespaces", GSON.toJsonTree(namespaces));
                json.add("allowed_items", GSON.toJsonTree(items));
                Files.createDirectories(FILE.getParent());
                Files.writeString(FILE, GSON.toJson(json));
            }
        } catch (Exception e) {
            LOGGER.error("Failed to read {}, using defaults", FILE, e);
        }
    }

    private static Set<String> read(JsonObject json, String key, Set<String> fallback) {
        if (!json.has(key) || !json.get(key).isJsonArray()) return fallback;
        Set<String> out = new LinkedHashSet<>();
        JsonArray array = json.getAsJsonArray(key);
        array.forEach(e -> out.add(e.getAsString().trim()));
        return out;
    }

    public static boolean namespaceAllowed(String namespace) {
        if (namespaces == null) load();
        return namespaces.isEmpty() || namespaces.contains(namespace);
    }

    public static boolean itemAllowed(String itemId) {
        if (items == null) load();
        return items.isEmpty() || items.contains(itemId);
    }
}
