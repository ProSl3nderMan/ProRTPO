package me.prosl3nderman.prortpo;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class StaffConfig {
    private FileConfiguration customConfig = null;
    private File customConfigFile = null;
    private String dir = ProRTPO.getInstance().getDataFolder() + File.separator + "staff";
    private String staffName;

    public StaffConfig(String staff) {
        File directory = new File(dir);
        if (! directory.exists())
            directory.mkdir();
        staffName = staff;
    }

    public void addPlayerToAlreadyTpedToList(String tpedTo, List<String> alreadyTpedTo) { //add a player to the already rtpo[ed] to list, so the staff won't rtpo to them again until they've rtpo[ed] everyone.
        alreadyTpedTo.add(tpedTo);
        getConfig().set("tpedAlready", alreadyTpedTo);
        srConfig();
    }

    public List<String> getAlreadyTpedTo() { //get the list of people that the staff has already rtpo[ed] to, empty list returned if none is created already.
        if (getConfig().contains("tpedAlready"))
            return getConfig().getStringList("tpedAlready");
        return new ArrayList<>();
    }

    public void clearAlreadyTpedToList() { //clear the list of people that the staff has already rtpo[ed] to, so the staff can cycle through the player list again.
        getConfig().set("tpedAlready", null);
        srConfig();
    }


    public void reloadConfig() {
        if (customConfigFile == null) {

            customConfigFile = new File(dir, staffName + ".yml");
        }
        if (!customConfigFile.exists()) {
            try {
                customConfigFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        customConfig = YamlConfiguration.loadConfiguration(customConfigFile);
    }

    public FileConfiguration getConfig() {
        if (customConfig == null) {
            reloadConfig();
        }
        return customConfig;
    }

    public void saveConfig() {
        if (customConfig == null || customConfigFile == null) {
            return;
        }
        try {
            getConfig().save(customConfigFile);
        } catch (IOException ex) {
            ProRTPO.getInstance().getLogger().log(Level.SEVERE, "Could not save config to " + customConfigFile, ex);
        }
    }

    public void srConfig() {
        saveConfig();
        reloadConfig();
    }

    public void delete() {
        if (customConfig == null)
            customConfigFile = new File(dir, staffName + ".yml");
        if (customConfigFile.exists()) {
            customConfigFile.delete();
            Bukkit.getLogger().log(Level.INFO, "[ProRTPO] The file /ProRTPO/staff/" + staffName + ".yml has been deleted.");
        } else
            Bukkit.getLogger().log(Level.WARNING, "[ProRTPO] Error! No file /ProRTPO/staff/" + staffName + ".yml, skipping deletion.");
    }
}
