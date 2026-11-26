package com.cogamer.mbeacompat;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/** Reaches Model Browser's widget (merged into AnvilScreen by its mixin) without a compile-time dependency. */
public final class ModelBrowserBridge {
    private static final Logger LOGGER = LoggerFactory.getLogger("mbeacompat");
    private static boolean resolved, broken;
    private static Field widgetField;
    private static Method isOpen, drawForeground;

    private ModelBrowserBridge() {}

    private static void resolve() {
        resolved = true;
        try {
            for (Field f : AnvilScreen.class.getDeclaredFields()) {
                if (f.getType().getName().endsWith("ModelBrowserWidget")) {
                    widgetField = f;
                    f.setAccessible(true);
                    break;
                }
            }
            if (widgetField == null) throw new NoSuchFieldException("ModelBrowserWidget field on AnvilScreen");
            Class<?> widget = widgetField.getType();
            isOpen = widget.getMethod("isOpen");
            for (Method m : widget.getMethods()) {
                if (m.getName().equals("drawForeground") && m.getParameterCount() == 3) {
                    drawForeground = m;
                    break;
                }
            }
            if (drawForeground == null) throw new NoSuchMethodException("ModelBrowserWidget#drawForeground");
        } catch (ReflectiveOperationException e) {
            broken = true;
            LOGGER.error("Could not hook Model Browser's model grid; the panel will open without items", e);
        }
    }

    public static void drawForeground(AnvilScreen screen, GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (!resolved) resolve();
        if (broken) return;
        try {
            Object widget = widgetField.get(screen);
            if (widget != null && (boolean) isOpen.invoke(widget)) {
                drawForeground.invoke(widget, graphics, mouseX, mouseY);
            }
        } catch (ReflectiveOperationException e) {
            broken = true;
            LOGGER.error("Model Browser model grid hook failed", e);
        }
    }
}
