package com.cogamer.mbeacompat;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * config/mbeacompat.properties
 *   hidden_namespaces = mod ids whose models are hidden from Model Browser
 *   hidden_models     = single model ids to hide (namespace:path)
 */
public final class HiddenNamespaces {
    private static final Logger LOGGER = LoggerFactory.getLogger("mbeacompat");
    private static final String NS_KEY = "hidden_namespaces";
    private static final String MODEL_KEY = "hidden_models";
    private static final String NS_DEFAULT = "";

    public record Hidden(Set<String> namespaces, Set<String> models) {
        public boolean isEmpty() { return namespaces.isEmpty() && models.isEmpty(); }
        public boolean hides(String namespace, String fullId) {
            return namespaces.contains(namespace) || models.contains(fullId);
        }
    }

    private HiddenNamespaces() {}

    /** Re-read on every model list reload (F3+T), so edits apply without restarting. */
    public static Hidden load() {
        Path file = FabricLoader.getInstance().getConfigDir().resolve("mbeacompat.properties");
        Properties props = new Properties();
        try {
            if (Files.exists(file)) {
                try (Reader r = Files.newBufferedReader(file)) { props.load(r); }
            }
            boolean changed = false;
            if (!props.containsKey(NS_KEY)) { props.setProperty(NS_KEY, NS_DEFAULT); changed = true; }
            if (!props.containsKey(MODEL_KEY)) { props.setProperty(MODEL_KEY, ""); changed = true; }
            if (changed) {
                try (Writer w = Files.newBufferedWriter(file)) {
                    props.store(w, "Model Browser x Easy Anvils Compat\n"
                            + NS_KEY + ": comma-separated mod ids whose models are hidden from Model Browser\n"
                            + MODEL_KEY + ": comma-separated single model ids to hide, e.g. somemod:bow\n"
                            + "All model ids are listed in config/mbeacompat-models.txt. Press F3+T to apply changes.");
                }
            }
        } catch (IOException e) {
            LOGGER.warn("Could not read/write {}", file, e);
        }
        return new Hidden(split(props.getProperty(NS_KEY, NS_DEFAULT)), split(props.getProperty(MODEL_KEY, "")));
    }

    /** Writes both lists back to config/mbeacompat.properties. */
    public static void save(Set<String> namespaces, Set<String> models) {
        Path file = FabricLoader.getInstance().getConfigDir().resolve("mbeacompat.properties");
        Properties props = new Properties();
        props.setProperty(NS_KEY, String.join(",", new TreeSet<>(namespaces)));
        props.setProperty(MODEL_KEY, String.join(",", new TreeSet<>(models)));
        try (Writer w = Files.newBufferedWriter(file)) {
            props.store(w, "Model Browser x Easy Anvils Compat (edit here or with the in-game filter keybind)\n"
                    + NS_KEY + ": comma-separated mod ids whose models are hidden from Model Browser\n"
                    + MODEL_KEY + ": comma-separated single model ids to hide, e.g. somemod:bow");
        } catch (IOException e) {
            LOGGER.warn("Could not write {}", file, e);
        }
    }

    private static Set<String> split(String value) {
        return Arrays.stream(value.split(","))
                .map(s -> s.trim().toLowerCase(Locale.ROOT))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }
}
