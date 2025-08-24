package com.autowaterdrop.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class AwdConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "autowaterdrop.json";
    private static AwdConfig INSTANCE;

    public int minFallHeightBlocks = 5; // range 3-20
    public int activationLeadTicks = 3; // range 1-6
    public boolean restorePreviousSlot = true;
    public int autoPickupTicks = 0; // 0=disabled, 1=1 tick, etc.
    public boolean searchNeighborPlacements = true;
    public boolean showHud = true;
    public boolean showChatOnActivate = false;
    public boolean safeModeExtraTick = false;

    public static AwdConfig get() {
        if (INSTANCE == null) {
            INSTANCE = new AwdConfig();
        }
        return INSTANCE;
    }

    public static void load() {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
        if (Files.exists(configPath)) {
            try (Reader r = Files.newBufferedReader(configPath)) {
                INSTANCE = GSON.fromJson(r, AwdConfig.class);
            } catch (IOException e) {
                INSTANCE = new AwdConfig();
            }
        } else {
            INSTANCE = new AwdConfig();
            save();
        }
    }

    public static void save() {
        try {
            Path configPath = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
            Files.createDirectories(configPath.getParent());
            try (Writer w = Files.newBufferedWriter(configPath)) {
                GSON.toJson(get(), w);
            }
        } catch (IOException ignored) {}
    }
}

