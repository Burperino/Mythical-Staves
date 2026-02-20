package me.td.mythicalstaves.server;

import me.td.mythicalstaves.MythicalStaves;
import me.td.mythicalstaves.networking.NetworkingConstants;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ServerInitializer implements DedicatedServerModInitializer {
    public ArrayList<UUID> playersNotChecked = new ArrayList<>();

    @Override
    public void onInitializeServer() {
        ServerPlayNetworking.registerGlobalReceiver(NetworkingConstants.VERSION_PACKET_ID, (server, player, handler, buf, responseSender) -> {
            playersNotChecked.remove(player.getUuid());
            String version = buf.readString();
            if(!version.equals(MythicalStaves.version)) {
                handler.disconnect(Text.literal(String.format("Your MythicalStaves version doesn't match the server's.\nServer: %s You: %s", MythicalStaves.version, version)));
                MythicalStaves.LOGGER.info(String.format("Kicked %s for joining with version %s whilst the server is on %s.", version, MythicalStaves.version));
            }
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            int time = 5;
            ServerPlayerEntity player = handler.getPlayer();
            MythicalStaves.LOGGER.info(String.format("%s with UUID %s joined. Giving them %d seconds to send their mod version.",
                    player.getName().getString(), player.getUuidAsString(), time));
            playersNotChecked.add(player.getUuid());

            sched(() -> {
                if(playersNotChecked.contains(player.getUuid())) {
                    handler.disconnect(Text.literal(String.format("Your MythicalStaves mod is not installed or outdated.\nThe server is on version %s.", MythicalStaves.version)));
                    MythicalStaves.LOGGER.info(String.format("%s with UUID %s was kicked for not sending the mod version."), player.getName().getString(), player.getUuidAsString());
                    playersNotChecked.remove(player.getUuid());
                }
            }, time * 1000);
        });
    }

    private void sched(Runnable runnable, long millis) {
        MythicalStaves.scheduler.schedule(runnable, millis, TimeUnit.MILLISECONDS);
    }
}
