package com.allowance;

import com.allowance.network.OpenEditorPayload;
import com.allowance.network.OpenGuiPayload;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public final class AllowanceCommands {
    private AllowanceCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("allowance")
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    PacketSender.syncTo(player);
                    PacketSender.sendTo(player, new OpenGuiPayload(0));
                    return 1;
                })
                .then(Commands.literal("admin")
                        .requires(src -> src.hasPermission(2))
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            PacketSender.syncTo(player);
                            PacketSender.sendTo(player, new OpenGuiPayload(1));
                            return 1;
                        }))
                .then(Commands.literal("editor")
                        .requires(src -> src.hasPermission(2))
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            PacketSender.sendTo(player, new OpenEditorPayload(-1));
                            return 1;
                        })));
    }
}
