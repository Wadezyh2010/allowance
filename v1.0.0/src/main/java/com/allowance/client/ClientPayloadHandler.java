package com.allowance.client;

import com.allowance.network.OpenGuiPayload;
import com.allowance.network.SyncDataPayload;
import com.allowance.network.EditorOpenedPayload;
import com.allowance.client.screens.OpRewardScreen;
import com.allowance.client.screens.PackageEditScreen;
import com.allowance.client.screens.PlayerRewardScreen;
import com.allowance.menus.PackageEditMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ClientPayloadHandler {
    private ClientPayloadHandler() {}

    public static void handleEditorOpened(EditorOpenedPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.player.containerMenu instanceof PackageEditMenu menu) {
                mc.setScreen(new PackageEditScreen(menu, mc.player.getInventory(),
                        Component.literal(payload.packageId() < 0 ? "新建奖励包" : "编辑奖励包")));
            }
        });
    }

    public static void handleSync(SyncDataPayload payload, IPayloadContext ctx) {
        ClientRewardCache.update(payload);
        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen instanceof PlayerRewardScreen screen) screen.refreshData();
            if (mc.screen instanceof OpRewardScreen screen) screen.refreshData();
        });
    }

    public static void handleOpenGui(OpenGuiPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (payload.target() == 0) {
                mc.setScreen(new PlayerRewardScreen());
            } else {
                mc.setScreen(new OpRewardScreen());
            }
        });
    }
}
