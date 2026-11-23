package com.allowance.network;

import com.allowance.AllowanceMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record DeletePackagePayload(int packageId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<DeletePackagePayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(AllowanceMod.MODID, "delete_package"));

    public static final StreamCodec<ByteBuf, DeletePackagePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, DeletePackagePayload::packageId,
            DeletePackagePayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
