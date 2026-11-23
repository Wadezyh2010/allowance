package com.allowance;

import com.allowance.network.OpenEditorPayload;
import com.allowance.network.OpenGuiPayload;
import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

@Mod(AllowanceMod.MODID)
public class AllowanceMod {
    public static final String MODID = "allowance";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AllowanceMod(IEventBus modEventBus) {
        ModRegistry.MENUS.register(modEventBus);
        modEventBus.addListener(this::registerPayloads);
        NeoForge.EVENT_BUS.addListener(this::onPlayerLoggedIn);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
        LOGGER.info("[Allowance] 会员奖励管理系统已加载");
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(com.allowance.network.CheckInPayload.TYPE, com.allowance.network.CheckInPayload.STREAM_CODEC, ServerPacketHandlers::handleCheckIn);
        registrar.playToServer(com.allowance.network.ClaimPayload.TYPE, com.allowance.network.ClaimPayload.STREAM_CODEC, ServerPacketHandlers::handleClaim);
        registrar.playToServer(com.allowance.network.SavePackagePayload.TYPE, com.allowance.network.SavePackagePayload.STREAM_CODEC, ServerPacketHandlers::handleSavePackage);
        registrar.playToServer(com.allowance.network.DeletePackagePayload.TYPE, com.allowance.network.DeletePackagePayload.STREAM_CODEC, ServerPacketHandlers::handleDeletePackage);
        registrar.playToServer(com.allowance.network.TogglePackagePayload.TYPE, com.allowance.network.TogglePackagePayload.STREAM_CODEC, ServerPacketHandlers::handleTogglePackage);
        registrar.playToServer(OpenEditorPayload.TYPE, OpenEditorPayload.STREAM_CODEC, ServerPacketHandlers::handleOpenEditor);
        // 注意：S2C 通道必须在两端都注册，否则客户端握手会报「服务端缺少此客户端需要的网络通道」。
        // 专用服务端只注册通道本身，不加载任何客户端类（处理逻辑用空实现）。
        if (FMLEnvironment.dist.isClient()) {
            registrar.playToClient(com.allowance.network.SyncDataPayload.TYPE, com.allowance.network.SyncDataPayload.STREAM_CODEC, com.allowance.client.ClientPayloadHandler::handleSync);
            registrar.playToClient(OpenGuiPayload.TYPE, OpenGuiPayload.STREAM_CODEC, com.allowance.client.ClientPayloadHandler::handleOpenGui);
            registrar.playToClient(com.allowance.network.EditorOpenedPayload.TYPE, com.allowance.network.EditorOpenedPayload.STREAM_CODEC, com.allowance.client.ClientPayloadHandler::handleEditorOpened);
        } else {
            registrar.playToClient(com.allowance.network.SyncDataPayload.TYPE, com.allowance.network.SyncDataPayload.STREAM_CODEC, (payload, ctx) -> { });
            registrar.playToClient(OpenGuiPayload.TYPE, OpenGuiPayload.STREAM_CODEC, (payload, ctx) -> { });
            registrar.playToClient(com.allowance.network.EditorOpenedPayload.TYPE, com.allowance.network.EditorOpenedPayload.STREAM_CODEC, (payload, ctx) -> { });
        }
    }

    private void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PacketSender.syncTo(player);
        }
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        AllowanceCommands.register(event.getDispatcher());
    }
}
