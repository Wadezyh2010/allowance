package com.allowance;

import com.allowance.network.SyncDataPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.time.LocalDate;

public final class PacketSender {
    private PacketSender() {}

    public static void sendTo(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    public static void syncTo(ServerPlayer player) {
        RewardData data = RewardData.get(player.server);
        long today = LocalDate.now().toEpochDay();
        RewardData.PlayerEntry entry = data.entry(player.getUUID());
        var list = data.packages().stream()
                .map(p -> new SyncDataPayload.PackageEntry(
                        p.id(), p.name(), p.mode().ordinal(), p.enabled(),
                        data.statusFor(entry, p, today), p.items()))
                .toList();
        PacketDistributor.sendToPlayer(player,
                new SyncDataPayload(entry.lastCheckIn == today, entry.streak, entry.total, list));
    }
}
