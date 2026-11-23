package com.allowance.client;

import com.allowance.AllowanceMod;
import com.allowance.client.screens.PlayerRewardScreen;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = AllowanceMod.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class ClientTickHandler {
    private ClientTickHandler() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        while (ClientSetup.OPEN_GUI.consumeClick()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen == null && mc.player != null) {
                mc.setScreen(new PlayerRewardScreen());
            }
        }
    }
}
