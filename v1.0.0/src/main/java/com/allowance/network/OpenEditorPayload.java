package com.allowance.network;

import com.allowance.AllowanceMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** OP 请求打开奖励包编辑器，packageId = -1 表示新建 */
public record OpenEditorPayload(int packageId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenEditorPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(AllowanceMod.MODID, "open_editor"));

    public static final StreamCodec<ByteBuf, OpenEditorPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, OpenEditorPayload::packageId,
            OpenEditorPayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
