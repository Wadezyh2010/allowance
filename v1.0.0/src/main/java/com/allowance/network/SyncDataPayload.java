package com.allowance.network;

import com.allowance.AllowanceMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record SyncDataPayload(boolean checkedInToday, int streak, int total, List<PackageEntry> packages) implements CustomPacketPayload {

    public record PackageEntry(int id, String name, int mode, boolean enabled, int status, List<ItemStack> items) {}

    public static final CustomPacketPayload.Type<SyncDataPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(AllowanceMod.MODID, "sync_data"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncDataPayload> STREAM_CODEC =
            StreamCodec.ofMember(SyncDataPayload::write, SyncDataPayload::read);

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeBoolean(checkedInToday);
        buf.writeVarInt(streak);
        buf.writeVarInt(total);
        buf.writeVarInt(packages.size());
        for (PackageEntry e : packages) {
            buf.writeVarInt(e.id());
            buf.writeUtf(e.name());
            buf.writeVarInt(e.mode());
            buf.writeBoolean(e.enabled());
            buf.writeVarInt(e.status());
            buf.writeVarInt(e.items().size());
            for (ItemStack stack : e.items()) {
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, stack);
            }
        }
    }

    private static SyncDataPayload read(RegistryFriendlyByteBuf buf) {
        boolean checkedIn = buf.readBoolean();
        int streak = buf.readVarInt();
        int total = buf.readVarInt();
        int size = buf.readVarInt();
        List<PackageEntry> packages = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            int id = buf.readVarInt();
            String name = buf.readUtf();
            int mode = buf.readVarInt();
            boolean enabled = buf.readBoolean();
            int status = buf.readVarInt();
            int itemCount = buf.readVarInt();
            List<ItemStack> items = new ArrayList<>();
            for (int j = 0; j < itemCount; j++) {
                items.add(ItemStack.OPTIONAL_STREAM_CODEC.decode(buf));
            }
            packages.add(new PackageEntry(id, name, mode, enabled, status, items));
        }
        return new SyncDataPayload(checkedIn, streak, total, packages);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
