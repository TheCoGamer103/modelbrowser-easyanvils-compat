package com.cogamer.mbeacompat.client;

import com.cogamer.mbeacompat.HiddenNamespaces;
import com.cogamer.mbeacompat.ModelCatalog;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Lists every model Model Browser found, grouped by mod. Toggle a whole mod or single models;
 * changes are saved to config/mbeacompat.properties and applied to the browser right away.
 */
public class ModelFilterScreen extends Screen {
    private static final int ROW_HEIGHT = 22;
    private static final int LIST_TOP = 48;
    private static final int LIST_WIDTH = 330;
    private static final int TOGGLE_WIDTH = 70;
    private static final int INDENT = 14;

    private final Screen parent;
    private final Map<String, TreeSet<String>> catalog = ModelCatalog.byNamespace();
    private final Set<String> hiddenNamespaces;
    private final Set<String> hiddenModels;
    private final Set<String> expanded = new HashSet<>();
    private final List<AbstractWidget> rowWidgets = new ArrayList<>();

    private EditBox search;
    private String query = "";
    private int scroll;

    /** One visible line: a mod header (model == null) or a single model under it. */
    private record Row(String namespace, String model) {}

    public ModelFilterScreen(Screen parent) {
        super(Component.literal("Model Browser Filter"));
        this.parent = parent;
        HiddenNamespaces.Hidden hidden = HiddenNamespaces.load();
        this.hiddenNamespaces = new HashSet<>(hidden.namespaces());
        this.hiddenModels = new HashSet<>(hidden.models());
    }

    @Override
    protected void init() {
        int left = (this.width - LIST_WIDTH) / 2;
        this.search = new EditBox(this.font, left, 22, LIST_WIDTH, 18, Component.literal("Search"));
        this.search.setValue(this.query);
        this.search.setResponder(text -> {
            this.query = text.toLowerCase(Locale.ROOT).trim();
            this.scroll = 0;
            this.refreshRows();
        });
        this.addRenderableWidget(this.search);

        int bottom = this.height - 26;
        this.addRenderableWidget(Button.builder(Component.literal("Expand all"), b -> {
            this.expanded.addAll(this.catalog.keySet());
            this.refreshRows();
        }).bounds(left, bottom, 80, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Collapse all"), b -> {
            this.expanded.clear();
            this.scroll = 0;
            this.refreshRows();
        }).bounds(left + 84, bottom, 80, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Done"), b -> this.onClose())
                .bounds(left + LIST_WIDTH - 100, bottom, 100, 20).build());

        this.refreshRows();
    }

    private int visibleRowCount() {
        return Math.max(1, (this.height - LIST_TOP - 34) / ROW_HEIGHT);
    }

    private List<Row> buildRows() {
        List<Row> rows = new ArrayList<>();
        boolean searching = !this.query.isEmpty();
        for (Map.Entry<String, TreeSet<String>> entry : this.catalog.entrySet()) {
            String namespace = entry.getKey();
            boolean nsMatches = !searching || namespace.contains(this.query);
            List<String> models = new ArrayList<>();
            for (String id : entry.getValue()) {
                if (nsMatches || id.contains(this.query)) models.add(id);
            }
            if (!nsMatches && models.isEmpty()) continue;
            rows.add(new Row(namespace, null));
            // While searching, show matching models of mods whose own name didn't match.
            if (this.expanded.contains(namespace) || (searching && !nsMatches)) {
                for (String id : models) rows.add(new Row(namespace, id));
            }
        }
        return rows;
    }

    private void refreshRows() {
        this.rowWidgets.forEach(this::removeWidget);
        this.rowWidgets.clear();

        List<Row> rows = this.buildRows();
        int visible = this.visibleRowCount();
        this.scroll = Math.max(0, Math.min(this.scroll, rows.size() - visible));
        int left = (this.width - LIST_WIDTH) / 2;

        for (int i = 0; i < visible && this.scroll + i < rows.size(); i++) {
            Row row = rows.get(this.scroll + i);
            int y = LIST_TOP + i * ROW_HEIGHT;
            if (row.model() == null) {
                this.addRow(this.modHeader(row.namespace(), left, y));
                this.addRow(this.modToggle(row.namespace(), left + LIST_WIDTH - TOGGLE_WIDTH, y));
            } else {
                this.addRow(this.modelLabel(row.model(), left + INDENT, y));
                this.addRow(this.modelToggle(row.namespace(), row.model(), left + LIST_WIDTH - TOGGLE_WIDTH, y));
            }
        }
    }

    private void addRow(AbstractWidget widget) {
        this.rowWidgets.add(widget);
        this.addRenderableWidget(widget);
    }

    private Button modHeader(String namespace, int x, int y) {
        int total = this.catalog.get(namespace).size();
        long hiddenCount = this.hiddenNamespaces.contains(namespace) ? total
                : this.catalog.get(namespace).stream().filter(this.hiddenModels::contains).count();
        String arrow = this.expanded.contains(namespace) ? "\u25BC " : "\u25B6 ";
        MutableComponent label = Component.literal(arrow + namespace + "  ")
                .append(Component.literal("(" + (total - hiddenCount) + "/" + total + " shown)")
                        .withStyle(ChatFormatting.GRAY));
        return Button.builder(label, b -> {
            if (!this.expanded.remove(namespace)) this.expanded.add(namespace);
            this.refreshRows();
        }).bounds(x, y, LIST_WIDTH - TOGGLE_WIDTH - 4, 20).build();
    }

    private Button modToggle(String namespace, int x, int y) {
        boolean hidden = this.hiddenNamespaces.contains(namespace);
        return Button.builder(state(!hidden), b -> {
            if (hidden) this.hiddenNamespaces.remove(namespace);
            else this.hiddenNamespaces.add(namespace);
            this.saveAndApply();
        }).bounds(x, y, TOGGLE_WIDTH, 20).build();
    }

    private Button modelLabel(String model, int x, int y) {
        Button label = Button.builder(Component.literal(model).withStyle(ChatFormatting.GRAY), b -> {})
                .bounds(x, y, LIST_WIDTH - TOGGLE_WIDTH - 4 - INDENT, 20).build();
        label.active = false;
        return label;
    }

    private Button modelToggle(String namespace, String model, int x, int y) {
        if (this.hiddenNamespaces.contains(namespace)) {
            Button locked = Button.builder(Component.literal("Mod hidden").withStyle(ChatFormatting.DARK_GRAY), b -> {})
                    .bounds(x, y, TOGGLE_WIDTH, 20).build();
            locked.active = false;
            return locked;
        }
        boolean hidden = this.hiddenModels.contains(model);
        return Button.builder(state(!hidden), b -> {
            if (hidden) this.hiddenModels.remove(model);
            else this.hiddenModels.add(model);
            this.saveAndApply();
        }).bounds(x, y, TOGGLE_WIDTH, 20).build();
    }

    private static Component state(boolean shown) {
        return shown ? Component.literal("Shown").withStyle(ChatFormatting.GREEN)
                : Component.literal("Hidden").withStyle(ChatFormatting.RED);
    }

    private void saveAndApply() {
        HiddenNamespaces.save(this.hiddenNamespaces, this.hiddenModels);
        ModelCatalog.reapply();
        this.refreshRows();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        this.scroll -= (int) Math.signum(scrollY) * 3;
        this.refreshRows();
        return true;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.text(this.font, this.title, (this.width - this.font.width(this.title)) / 2, 8, 0xFFFFFFFF, true);
        if (this.catalog.isEmpty()) {
            Component empty = Component.literal("No models loaded yet. Open an anvil once or press F3+T, then reopen.");
            graphics.text(this.font, empty, (this.width - this.font.width(empty)) / 2, LIST_TOP + 4, 0xFFAAAAAA, true);
        }
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(this.parent);
    }
}
