package com.allowance.network;

import com.allowance.AllowanceMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** 服务端已打开编辑器菜单，通知客户端弹出编辑界面（packageId = -1 表示新建） */
public record EditorOpenedPayload(int packageId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<EditorOpenedPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(AllowanceMod.MODID, "editor_opened"));

    public static final StreamCodec<ByteBuf, EditorOpenedPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, EditorOpenedPayload::packageId,
            EditorOpenedPayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
