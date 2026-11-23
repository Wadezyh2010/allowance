package com.allowance.network;

import com.allowance.AllowanceMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** 0 = 玩家奖励中心, 1 = OP 管理面板 */
public record OpenGuiPayload(int target) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenGuiPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(AllowanceMod.MODID, "open_gui"));

    public static final StreamCodec<ByteBuf, OpenGuiPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, OpenGuiPayload::target,
            OpenGuiPayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
