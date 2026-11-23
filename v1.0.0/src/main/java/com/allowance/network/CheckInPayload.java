package com.allowance.network;

import com.allowance.AllowanceMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record CheckInPayload() implements CustomPacketPayload {
    public static final CheckInPayload INSTANCE = new CheckInPayload();

    public static final CustomPacketPayload.Type<CheckInPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(AllowanceMod.MODID, "check_in"));

    public static final StreamCodec<ByteBuf, CheckInPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
