package me.td.mythicalstaves;

import me.td.mythicalstaves.effects.Effects;
import me.td.mythicalstaves.items.Staves;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class MythicalStaves implements ModInitializer {
    public static final String MOD_ID = "mythicalstaves";
    public static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    public static final String version = "1.0.9";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        Staves.initialize();
        Effects.initialize();
    }
}
