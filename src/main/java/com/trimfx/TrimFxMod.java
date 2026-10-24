package com.trimfx;

import com.mojang.logging.LogUtils;
import com.trimfx.config.TrimFxConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(TrimFxMod.MOD_ID)
public final class TrimFxMod {

    public static final String MOD_ID = "trimfx";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TrimFxMod(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.SYNCED, TrimFxConfig.SPEC);
        LOGGER.info("TrimFX initialized for Minecraft 26.3 (18 Trim Patterns & 11 Trim Materials active).");
    }
}
