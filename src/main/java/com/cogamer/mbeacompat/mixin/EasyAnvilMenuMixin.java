package com.cogamer.mbeacompat.mixin;

import com.cogamer.mbeacompat.ModelApplier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AnvilMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Easy Anvils' menu overrides createResult() and, for ordinary renames, never calls the vanilla
 * AnvilMenu#createResult, so Model Browser's result hook never fires. Re-apply the model at the end
 * of Easy Anvils' own result calculation.
 */
@Mixin(targets = "fuzs.easyanvils.common.world.inventory.ModAnvilMenu")
public abstract class EasyAnvilMenuMixin extends AnvilMenu {
    protected EasyAnvilMenuMixin(int id, Inventory inventory) {
        super(id, inventory);
    }

    @Inject(method = "createAnvilResult()V", at = @At("TAIL"))
    private void mbeacompat$applyModel(CallbackInfo ci) {
        ModelApplier.apply((AnvilMenu) (Object) this);
    }
}
