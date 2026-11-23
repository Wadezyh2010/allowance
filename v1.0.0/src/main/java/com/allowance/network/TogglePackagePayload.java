package com.allowance.network;

import com.allowance.AllowanceMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record TogglePackagePayload(int packageId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<TogglePackagePayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(AllowanceMod.MODID, "toggle_package"));

    public static final StreamCodec<ByteBuf, TogglePackagePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, TogglePackagePayload::packageId,
            TogglePackagePayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
