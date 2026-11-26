package com.cogamer.mbeacompat;

import com.cogamer.mbeacompat.client.ModelFilterScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public class MbeaCompatClient implements ClientModInitializer {
    public static KeyMapping openFilter;

    @Override
    public void onInitializeClient() {
        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("mbeacompat", "main"));
        // Unbound by default; set it in Controls > Key Binds.
        openFilter = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.mbeacompat.open_filter", InputConstants.UNKNOWN.getValue(), category));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openFilter.consumeClick()) {
                if (client.gui.screen() == null) client.gui.setScreen(new ModelFilterScreen(null));
            }
        });
    }
}
