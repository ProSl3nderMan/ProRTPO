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
        if (!p.hasPermission(ProRTPO.getInstance().getConfig().getString("permissions.prtpo"))) {
            p.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
            return true;
        }

        StaffConfig SC = new StaffConfig(p.getName());
        if (args.length == 0) { //randomly tp the staff to any player
            if (Bukkit.getOnlinePlayers().size() == 1) { //if the staff member is the only one online, it'll stop the command from processing further.
                p.sendMessage(ChatColor.RED + "There are currently no players on except you!");
                return true;
            }
            List<String> alreadyTpedTo = SC.getAlreadyTpedTo(); //get list of players that the staff member has already tped to.
            List<String> possibleTps = getPlayerList(alreadyTpedTo, p.getName(), SC); //get list of all players excluding the players the staff member has already tped to.

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

            SC.addPlayerToAlreadyTpedToList(playerChosen, alreadyTpedTo);
            p.sendMessage(ChatColor.BLUE+"You have been randomly teleported to " + ChatColor.WHITE + playerChosen + ChatColor.BLUE + "!");//send the message to the staff  that they've been teleported to the chosen player.
            logRTPO(p.getName(), playerChosen); //Log the rtpo if logging is enabled in /ProRTPO/config.yml.
            return true;
        }

        if (args[0].equalsIgnoreCase("clear")) { //resets the staff's list of players they've already tped to.
            SC.clearAlreadyTpedToList(); //clears the list of players the staff member has already tped to.
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

    private List<String> getPlayerList(List<String> alreadyTpedTo, String staffName, StaffConfig SC) { // gets list of all players, and excludes players the staff has already tped to. Resets list if no one else to tp to.
        List<String> playerListWithoutPreviousTps = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!alreadyTpedTo.contains(p.getName()) && p.getName() != staffName)
                playerListWithoutPreviousTps.add(p.getName());
        }
        if (playerListWithoutPreviousTps.size() == 0 && Bukkit.getOnlinePlayers().size() != 1) {
            SC.clearAlreadyTpedToList();
            return getPlayerList(new ArrayList(), staffName, SC);
        }
        return playerListWithoutPreviousTps;
    }

    private void logRTPO(String staff, String playerTpedTo) { //logs the random tpo if it is enabled to do so in config.yml.
        if (ProRTPO.getInstance().getConfig().getBoolean("logRTPO")) {
            Bukkit.getLogger().log(Level.INFO, "[ProRTPO] Staff member '" + staff + "' has randomly tped to '" + playerTpedTo + "'!");
        }
    }
}
