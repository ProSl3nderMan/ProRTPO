package me.prosl3nderman.prortpo;

import jdk.nashorn.internal.objects.annotations.Constructor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;

public class PRTPOCommand implements CommandExecutor {

    @Constructor
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            if (args.length == 0) {
                sender.sendMessage(ChatColor.RED + "The only command console can do here is " + ChatColor.WHITE + "/prtpo reload" + ChatColor.RED + ", which reloads the config.");
                return true;
            }
        }

        Player p = (Player) sender;
        if (!p.hasPermission(ProRTPO.getInstance().rtpoPermission())) {
            p.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
            return true;
        }

        if (args.length == 0) { //randomly tp the staff to any player
            if (Bukkit.getOnlinePlayers().size() == 1) { //if the staff member is the only one online, it'll stop the command from processing further.
                p.sendMessage(ChatColor.RED + "There are currently no players on except you!");
                return true;
            }
            List<String> alreadyTpedTo = getAlreadyTpedTo(p.getUniqueId()); //get list of players that the staff member has already tped to.
            List<String> possibleTps = getPlayerList(alreadyTpedTo, p.getUniqueId()); //get list of all players excluding the players the staff member has already tped to.

            String playerChosen = possibleTps.get(new Random().nextInt(possibleTps.size())); //choose randomly from the list to randomly teleport to a player.
            int counter = 0;
            while (Bukkit.getPlayer(playerChosen) == null || !Bukkit.getPlayer(playerChosen).isOnline() && counter < 10) { //just in case the player chosen is not online or is null
                playerChosen = possibleTps.get(new Random().nextInt(possibleTps.size()));
                counter++;
            }
            if (counter == 10) { //just a fail safe to prevent a while loop from going forever and crashing the server, shouldn't ever happen... ;)
                p.sendMessage(ChatColor.RED + "Something went wrong! We couldn't find any players to RTPO to!");
                return true;
            }
            GameMode gamemode = p.getGameMode();
            p.teleport(Bukkit.getPlayer(playerChosen)); //teleport the staff member to the player chosen.
            if (p.getGameMode() != gamemode) //fixes the glitch when a player changes worlds and it unsets the gamemode.
                p.setGameMode(gamemode);

            addPlayerToAlreadyTpedToList(p.getUniqueId(), playerChosen, alreadyTpedTo);
            p.sendMessage(ChatColor.BLUE+"You have been randomly teleported to " + ChatColor.WHITE + playerChosen + ChatColor.BLUE + "!");//send the message to the staff  that they've been teleported to the chosen player.
            logRTPO(p.getName(), playerChosen); //Log the rtpo if logging is enabled in /ProRTPO/config.yml.
            return true;
        }

        if (args[0].equalsIgnoreCase("clear")) { //resets the staff's list of players they've already tped to.
            clearAlreadyTpedToList(p.getUniqueId()); //clears the list of players the staff member has already tped to.
            p.sendMessage(ChatColor.BLUE + "You have cleared the list of people you've already tped to!");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) { //reloads the main config found in /ProRTPO/config.yml
            if (!p.hasPermission(ProRTPO.getInstance().getConfig().getString("permissions.prtpoReload"))) {
                p.sendMessage(ChatColor.RED + "You do not have permission to reload the config!!!");
                return true;
            }
            ProRTPO.getInstance().saveDefaultConfig(); //reloads config.yml
            ProRTPO.getInstance().reloadConfig(); //reloads config.yml
            ProRTPO.getInstance().refreshVariables();
            p.sendMessage(ChatColor.GREEN + "/ProRTPO/config.yml has been reloaded!");
            return true;
        }

        //if the staff types an argument unknown, we presume they are trying to find out what all they can do with /prtpo, and we send them a list of what they can do.
        p.sendMessage(ChatColor.RED + "Unknown command! Commands options: ");
        p.sendMessage(ChatColor.WHITE + "/prtpo: " + ChatColor.RED + "Tps you to someone random.");
        p.sendMessage(ChatColor.WHITE + "/prtpo clear: " + ChatColor.RED + "Clears the list that keeps track of everyone you've already randomly tped to!");
        if (p.hasPermission(ProRTPO.getInstance().getConfig().getString("permissions.prtpoReload"))) //if a staff doesn't have /prtpo reload perms, why bother telling them the command, knowwhatimsayin?
            p.sendMessage(ChatColor.WHITE + "/prtpo reload: " + ChatColor.RED + "Reloads /ProRTPO/config.yml.");
        return true;
    }

    public void addPlayerToAlreadyTpedToList(UUID staff, String tpedTo, List<String> alreadyTpedTo) { //add a player to the already rtpo[ed] to list, so the staff won't rtpo to them again until they've rtpo[ed] everyone.
        alreadyTpedTo.add(tpedTo);
        ProRTPO.getInstance().alreadyTpedTo.put(staff, alreadyTpedTo);
    }

    public List<String> getAlreadyTpedTo(UUID staff) { //get the list of people that the staff has already rtpo[ed] to, empty list returned if none is created already.
        if (ProRTPO.getInstance().alreadyTpedTo.containsKey(staff))
            return ProRTPO.getInstance().alreadyTpedTo.get(staff);
        return new ArrayList<>();
    }

    public void clearAlreadyTpedToList(UUID staff) { //clear the list of people that the staff has already rtpo[ed] to, so the staff can cycle through the player list again.
        ProRTPO.getInstance().alreadyTpedTo.remove(staff);
    }

    private List<String> getPlayerList(List<String> alreadyTpedTo, UUID staff) { // gets list of all players, and excludes players the staff has already tped to. Resets list if no one else to tp to.
        List<String> playerListWithoutPreviousTps = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!alreadyTpedTo.contains(p.getName()) && p.getUniqueId() != staff)
                playerListWithoutPreviousTps.add(p.getName());
        }
        if (playerListWithoutPreviousTps.size() == 0 && Bukkit.getOnlinePlayers().size() != 1) {
            clearAlreadyTpedToList(staff);
            return getPlayerList(new ArrayList(), staff);
        }
        return playerListWithoutPreviousTps;
    }

    private void logRTPO(String staff, String playerTpedTo) { //logs the random tpo if it is enabled to do so in config.yml.
        if (ProRTPO.getInstance().logRTPOS()) {
            Bukkit.getLogger().log(Level.INFO, "[ProRTPO] Staff member '" + staff + "' has randomly tped to '" + playerTpedTo + "'!");
        }
    }
}
