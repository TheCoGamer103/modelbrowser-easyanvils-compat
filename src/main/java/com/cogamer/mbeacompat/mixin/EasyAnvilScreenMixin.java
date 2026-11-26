package com.cogamer.mbeacompat.mixin;

import com.cogamer.mbeacompat.ModelBrowserBridge;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AnvilMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Easy Anvils' ModAnvilScreen overrides subInit() and extractLabels() without calling super, so the
 * code Model Browser injects into AnvilScreen (toggle button, browser panel, model grid) never runs.
 */
@Mixin(targets = "fuzs.easyanvils.common.client.gui.screens.inventory.ModAnvilScreen")
public abstract class EasyAnvilScreenMixin extends AnvilScreen {
    protected EasyAnvilScreenMixin(AnvilMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Inject(method = "subInit", at = @At("TAIL"))
    private void mbeacompat$addModelBrowser(CallbackInfo ci) {
        AnvilScreenAccessor accessor = (AnvilScreenAccessor) (Object) this;
        EditBox easyAnvilsBox = accessor.mbeacompat$getName();

        // Run vanilla AnvilScreen#subInit, which now carries Model Browser's injections.
        // It creates a second, vanilla name box; take its position/size, then drop it.
        super.subInit();

        EditBox vanillaBox = accessor.mbeacompat$getName();
        if (vanillaBox != null && vanillaBox != easyAnvilsBox) {
            this.removeWidget(vanillaBox);
            easyAnvilsBox.setX(vanillaBox.getX());
            easyAnvilsBox.setWidth(vanillaBox.getWidth());
            accessor.mbeacompat$setName(easyAnvilsBox);
            this.setFocused(easyAnvilsBox);
        }
    }

    @Inject(method = "extractLabels", at = @At("TAIL"))
    private void mbeacompat$drawModelGrid(GuiGraphicsExtractor graphics, int mouseX, int mouseY, CallbackInfo ci) {
        ModelBrowserBridge.drawForeground(this, graphics, mouseX, mouseY);
    }
}
