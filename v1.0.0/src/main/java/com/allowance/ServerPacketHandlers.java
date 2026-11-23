package com.allowance;

import com.allowance.menus.PackageEditMenu;
import com.allowance.network.CheckInPayload;
import com.allowance.network.ClaimPayload;
import com.allowance.network.DeletePackagePayload;
import com.allowance.network.OpenEditorPayload;
import com.allowance.network.SavePackagePayload;
import com.allowance.network.TogglePackagePayload;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public final class ServerPacketHandlers {
    private ServerPacketHandlers() {}

    public static void handleCheckIn(CheckInPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer player) {
                String msg = RewardData.get(player.server).checkIn(player);
                if (msg != null) player.sendSystemMessage(Component.literal(msg));
                PacketSender.syncTo(player);
            }
        });
    }

    public static void handleClaim(ClaimPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer player) {
                String msg = RewardData.get(player.server).claim(player, payload.packageId());
                if (msg != null) player.sendSystemMessage(Component.literal(msg));
                PacketSender.syncTo(player);
            }
        });
    }

    public static void handleSavePackage(SavePackagePayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player) || !player.hasPermissions(2)) return;
            if (!(player.containerMenu instanceof PackageEditMenu menu)) return;
            List<ItemStack> items = new ArrayList<>();
            for (int i = 0; i < menu.container.getContainerSize(); i++) {
                ItemStack stack = menu.container.getItem(i);
                if (!stack.isEmpty()) items.add(stack.copy());
            }
            String msg = RewardData.get(player.server).updatePackage(
                    payload.packageId(), payload.name(), payload.mode(), payload.enabled(), items);
            player.sendSystemMessage(Component.literal(msg));
            PacketSender.syncTo(player);
        });
    }

    public static void handleDeletePackage(DeletePackagePayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player) || !player.hasPermissions(2)) return;
            if (RewardData.get(player.server).deletePackage(payload.packageId())) {
                player.sendSystemMessage(Component.literal("§a奖励包已删除。"));
            }
            PacketSender.syncTo(player);
        });
    }

    public static void handleTogglePackage(TogglePackagePayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player) || !player.hasPermissions(2)) return;
            RewardPackage pkg = RewardData.get(player.server).byId(payload.packageId());
            if (pkg != null) {
                RewardData.get(player.server).togglePackage(payload.packageId());
                player.sendSystemMessage(Component.literal(pkg.enabled() ? "§a已启用奖励包。" : "§e已停用奖励包。"));
                PacketSender.syncTo(player);
            }
        });
    }

    public static void handleOpenEditor(OpenEditorPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player) || !player.hasPermissions(2)) return;
            RewardData data = RewardData.get(player.server);
            RewardPackage pkg = payload.packageId() < 0 ? null : data.byId(payload.packageId());
            int id = pkg == null ? -1 : pkg.id();
            String name = pkg == null ? "" : pkg.name();
            int mode = pkg == null ? 0 : pkg.mode().ordinal();
            boolean enabled = pkg == null || pkg.enabled();
            List<ItemStack> items = pkg == null ? List.of() : pkg.items();

            ((net.neoforged.neoforge.common.extensions.IPlayerExtension) player).openMenu(new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.literal(pkg == null ? "新建奖励包" : "编辑奖励包");
                }

                @Override
                public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player playerEntity) {
                    return new PackageEditMenu(windowId, inventory, id, name, mode, enabled, items);
                }
            }, buf -> {
                buf.writeInt(id);
                buf.writeUtf(name);
                buf.writeVarInt(mode);
                buf.writeBoolean(enabled);
                buf.writeVarInt(items.size());
                for (ItemStack stack : items) {
                    ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, stack);
                }
            });
            // openMenu 只会同步菜单数据，客户端还需此包来弹出编辑界面
            PacketSender.sendTo(player, new com.allowance.network.EditorOpenedPayload(id));
        });
    }
}
