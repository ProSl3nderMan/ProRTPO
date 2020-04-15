package me.prosl3nderman.prortpo;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerLeaveEvent implements Listener {

    @EventHandler
    public void onStaffLeave(PlayerQuitEvent e) {
        if (ProRTPO.getInstance().alreadyTpedTo.containsKey(e.getPlayer().getUniqueId()))
            ProRTPO.getInstance().alreadyTpedTo.remove(e.getPlayer().getUniqueId());
    }
}
