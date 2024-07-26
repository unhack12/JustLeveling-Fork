package com.seniors.justlevelingfork.common.command;

import com.seniors.justlevelingfork.common.capability.AptitudeCapability;
import com.seniors.justlevelingfork.handler.HandlerConfigCommon;
import com.seniors.justlevelingfork.network.packet.client.SyncAptitudeCapabilityCP;
import com.seniors.justlevelingfork.registry.RegistryAptitudes;
import com.seniors.justlevelingfork.registry.aptitude.Aptitude;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class AptitudeLevelCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register((Commands.literal("aptitudes")
                .requires(source -> source.hasPermission(2)))
                .then(Commands.argument("player", (ArgumentType) EntityArgument.player())
                        .then(((RequiredArgumentBuilder) Commands.argument("aptitude", AptitudeArgument.getArgument())
                                .then(Commands.literal("get")
                                        .executes(source -> getAptitude(source, EntityArgument.getPlayer(source, "player"), AptitudeArgument.getAptitude(source, "aptitude")))))
                                .then(Commands.literal("set")
                                        .then(Commands.argument("lvl", (ArgumentType) IntegerArgumentType.integer(1, HandlerConfigCommon.aptitudeMaxLevel.get()))
                                                .executes(source -> setAptitude(source, EntityArgument.getPlayer(source, "player"), AptitudeArgument.getAptitude(source, "aptitude"), IntegerArgumentType.getInteger(source, "lvl"))))))));
    }


    public static int getAptitude(CommandContext<CommandSourceStack> source, ServerPlayer player, ResourceLocation aptitudeKey) {
        Aptitude aptitude = RegistryAptitudes.APTITUDES_REGISTRY.get().getValue(aptitudeKey);

        if (player != null && aptitude != null) {
            AptitudeCapability capability = AptitudeCapability.get(player);

            source.getSource().sendSuccess(() -> Component.translatable("commands.message.aptitude.get", player.getName().copy().withStyle(ChatFormatting.BOLD), Component.literal(String.valueOf(capability.getAptitudeLevel(aptitude))).withStyle(ChatFormatting.BOLD), Component.translatable(aptitude.getKey()).withStyle(ChatFormatting.BOLD)), false);


            return 1;
        }


        return 0;
    }

    public static int setAptitude(CommandContext<CommandSourceStack> source, ServerPlayer player, ResourceLocation aptitudeKey, int setLevel) {
        Aptitude aptitude = RegistryAptitudes.APTITUDES_REGISTRY.get().getValue(aptitudeKey);
        if (player != null && aptitude != null) {
            AptitudeCapability capability = AptitudeCapability.get(player);
            capability.setAptitudeLevel(aptitude, setLevel);
            SyncAptitudeCapabilityCP.send(player);

            source.getSource().sendSuccess(() -> Component.translatable("commands.message.aptitude.set", player.getName().copy().withStyle(ChatFormatting.BOLD), Component.literal(String.valueOf(capability.getAptitudeLevel(aptitude))).withStyle(ChatFormatting.BOLD), Component.translatable(aptitude.getKey()).withStyle(ChatFormatting.BOLD)), false);


            return 1;
        }

        return 0;
    }
}

