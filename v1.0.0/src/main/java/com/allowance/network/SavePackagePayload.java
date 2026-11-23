package com.allowance.network;

import com.allowance.AllowanceMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SavePackagePayload(int packageId, String name, int mode, boolean enabled) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SavePackagePayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(AllowanceMod.MODID, "save_package"));

    public static final StreamCodec<ByteBuf, SavePackagePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SavePackagePayload::packageId,
            ByteBufCodecs.STRING_UTF8, SavePackagePayload::name,
            ByteBufCodecs.VAR_INT, SavePackagePayload::mode,
            ByteBufCodecs.BOOL, SavePackagePayload::enabled,
            SavePackagePayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
