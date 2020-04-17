package me.prosl3nderman.prortpo;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public final class ProRTPO extends JavaPlugin {

    private static ProRTPO plugin;
    public HashMap<UUID, List<String>> alreadyTpedTo = new HashMap<>();
    private boolean logRTPOs = true;
    private String rtpoPermission = "ProRTPO.rtpo";

    @Override
    public void onEnable() {
        plugin = this;

        getCommand("prtpo").setExecutor(new PRTPOCommand());
        getServer().getPluginManager().registerEvents(new PlayerLeaveEvent(), plugin);

        doConfig();

        refreshVariables();
    }

    @Override
    public void onDisable() {
        alreadyTpedTo.clear();
    }

    private void doConfig() {
        try {
            if (!getDataFolder().exists()) {
                getDataFolder().mkdirs();
            }
            File file = new File(getDataFolder(), "config.yml");
            if (!file.exists()) {
                getLogger().info("Config.yml not found, creating!");
                saveDefaultConfig();
            } else {
                getLogger().info("Config.yml found, loading!");
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public boolean logRTPOS() {
        return logRTPOs;
    }

    public String rtpoPermission() {
        return rtpoPermission;
    }

    public void refreshVariables() {
        logRTPOs = getConfig().getBoolean("logRTPO");
        rtpoPermission = getConfig().getString("permissions.prtpo");
    }

    public static ProRTPO getInstance() {
        return plugin;
    }
}
