package me.td.mythicalstaves.client;

import me.td.mythicalstaves.MythicalStaves;
import me.td.mythicalstaves.Utils;
import me.td.mythicalstaves.effects.Effects;
import me.td.mythicalstaves.networking.NetworkingConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;

public class ClientInitializer implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_WORLD_TICK.register((world) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if(mc.player.hasStatusEffect(Effects.FROZEN_EFFECT)) {
                if(!Utils.isPlayerMovementDisabled()) {
                    Utils.disablePlayerMovement(true);
                }
            } else {
                if(Utils.isPlayerMovementDisabled()) {
                    Utils.disablePlayerMovement(false);
                }
            }
        });

        ClientPlayConnectionEvents.JOIN.register((playNetworkHandler, packetSender, client) -> {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeString(MythicalStaves.version);
            ClientPlayNetworking.send(NetworkingConstants.VERSION_PACKET_ID, buf);
        });
    }
}
