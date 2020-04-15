package me.prosl3nderman.prortpo;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public final class ProRTPO extends JavaPlugin {

    private static ProRTPO plugin;
    public HashMap<UUID, List<String>> alreadyTpedTo = new HashMap<>();

    @Override
    public void onEnable() {
        plugin = this;

        getCommand("prtpo").setExecutor(new PRTPOCommand());
        getServer().getPluginManager().registerEvents(new PlayerLeaveEvent(), plugin);

        doConfig();
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

    public static ProRTPO getInstance() {
        return plugin;
    }
}
