package me.prosl3nderman.prortpo;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class ProRTPO extends JavaPlugin {

    private static ProRTPO plugin;

    @Override
    public void onEnable() {
        plugin = this;

        getCommand("prtpo").setExecutor(new PRTPOCommand());

        doConfig();
    }

    @Override
    public void onDisable() {
        String dir = getDataFolder() + File.separator + "staff";
        if (new File(dir).exists()) {
            for (File f : new File(dir).listFiles())
                f.delete();
        }
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
