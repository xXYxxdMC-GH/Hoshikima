package com.xxyxxdmc.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.xxyxxdmc.Hoshikima;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class HoshikimaConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("HoshikimaConfig");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir()
            .resolve(Hoshikima.MOD_ID + ".json").toFile();

    private static HoshikimaConfig INSTANCE;

    public boolean enableEnderPearlBundle = true;
    public boolean enableFireworkThruster = true;
    public boolean enableLargeBucket = true;
    public boolean enableMultiFluidBucket = true;
    public boolean enableRottenFleshCluster = true;

    public HoshikimaConfig() {}

    public HoshikimaConfig(HoshikimaConfig other) {
        this.enableEnderPearlBundle = other.enableEnderPearlBundle;
        this.enableFireworkThruster = other.enableFireworkThruster;
        this.enableLargeBucket = other.enableLargeBucket;
        this.enableMultiFluidBucket = other.enableMultiFluidBucket;
        this.enableRottenFleshCluster = other.enableRottenFleshCluster;
    }

    public void apply(HoshikimaConfig other) {
        this.enableEnderPearlBundle = other.enableEnderPearlBundle;
        this.enableFireworkThruster = other.enableFireworkThruster;
        this.enableLargeBucket = other.enableLargeBucket;
        this.enableMultiFluidBucket = other.enableMultiFluidBucket;
        this.enableRottenFleshCluster = other.enableRottenFleshCluster;
    }


    public static synchronized HoshikimaConfig get() {
        if (INSTANCE == null) {
            INSTANCE = load();
        }
        return INSTANCE;
    }

    private static HoshikimaConfig load() {
        HoshikimaConfig config;
        if (CONFIG_FILE.exists() && CONFIG_FILE.isFile()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                config = GSON.fromJson(reader, HoshikimaConfig.class);
                if (config == null) config = new HoshikimaConfig();
            } catch (Exception e) {
                LOGGER.error("Failed to load config file, creating a new one with default values.", e);
                config = new HoshikimaConfig();
                config.save();
            }
        } else {
            LOGGER.info("Config file not found, creating a new one with default values.");
            config = new HoshikimaConfig();
            config.save();
        }
        return config;
    }

    public void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to save config file.", e);
        }
    }
}