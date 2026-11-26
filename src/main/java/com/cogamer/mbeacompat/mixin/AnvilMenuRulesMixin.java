package com.cogamer.mbeacompat.mixin;

import com.cogamer.mbeacompat.Rules;
import net.minecraft.world.inventory.AnvilMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Vanilla-path anvils (and Easy Anvils' fallback to vanilla): undo Model Browser if not allowed. */
@Mixin(value = AnvilMenu.class, priority = 1500)
public abstract class AnvilMenuRulesMixin {
    @Inject(method = "createResult", at = @At("TAIL"))
    private void mbeacompat$enforceRules(CallbackInfo ci) {
        Rules.enforce((AnvilMenu) (Object) this);
    }
}
