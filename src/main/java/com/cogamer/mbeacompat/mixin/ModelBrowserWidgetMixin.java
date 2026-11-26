package com.cogamer.mbeacompat.mixin;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

/**
 * Model Browser draws the "1/100" page counter left-aligned at a fixed spot, so longer counters run
 * off to the right. Center it between the page arrows instead, like the vanilla recipe book.
 * (require = 0: if Model Browser changes this code, the counter just stays where it was.)
 */
@Mixin(targets = "deborn.modelbrowser.gui.ModelBrowserWidget", remap = false)
public abstract class ModelBrowserWidgetMixin {
    /** Model Browser places the counter's left edge 8 px left of the centre between the arrows. */
    private static final int MBEACOMPAT$CENTER_OFFSET = 8;

    @ModifyArgs(method = "extractRenderState", require = 0, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V"))
    private void mbeacompat$centerPageCounter(Args args) {
        Font font = args.get(0);
        Component text = args.get(1);
        int x = args.get(2);
        args.set(2, x + MBEACOMPAT$CENTER_OFFSET - font.width(text) / 2);
    }
}
