
package us.calzoneman.BuildSession;

/**
 *
 * @author Calvin
 */

import org.bukkit.Material;
import org.bukkit.entity.StorageMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class BuildSessionPlayerListener implements Listener {
    private BuildSession plugin;

    public BuildSessionPlayerListener(BuildSession plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if(!plugin.sessionExists(event.getPlayer().getName())) return;
        if(event.getItem() != null) {
        	if(event.getItem().getTypeId() == Material.EXP_BOTTLE.getId() && !event.getPlayer().hasPermission("buildsession.expbottle")) {
	        	event.setCancelled(true);
	        	event.getPlayer().sendMessage("You are not allowed to use experience bottles while in Build Session mode!");
	        	return;
	        }
        	else if(event.getItem().getTypeId() == Material.MONSTER_EGG.getId() && !event.getPlayer().hasPermission("buildsession.mobegg")) {
	        	event.getPlayer().sendMessage("You are not allowed to use mob eggs while in Build Session mode!");
	        	event.setCancelled(true);
	        	return;
	        }
        	else if(event.getItem().getTypeId() == Material.FIREBALL.getId() && !event.getPlayer().hasPermission("buildsession.fireball")) {
	        	event.getPlayer().sendMessage("You are not allowed to use fireballs while in Build Session mode!");
	        	event.setCancelled(true);
	        	return;
	        }
        	else if(event.getItem().getTypeId() == Material.EYE_OF_ENDER.getId() && !event.getPlayer().hasPermission("buildsession.endereye")) {
	        	event.getPlayer().sendMessage("You are not allowed to use eyes of ender while in Build Session mode!");
	        	event.setCancelled(true);
	        	return;
	        }
        }
        if(event.getClickedBlock() != null && !event.getPlayer().hasPermission("buildsession.inventoryaccess") && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            switch(event.getClickedBlock().getTypeId()) {
                case 23:
                case 54:
                case 61:
                case 62:
                case 84:
                case 116:
                case 117:
                    event.setCancelled(true);
                    event.getPlayer().sendMessage("You are not allowed to access storage blocks while in Build Session mode!");
                    break;
                default:
                    break;
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if(!plugin.sessionExists(event.getPlayer().getName())) return;
        if(!event.getPlayer().hasPermission("buildsession.inventoryaccess")) {
            if(event.getRightClicked() instanceof StorageMinecart) {
                event.setCancelled(true);
                event.getPlayer().sendMessage("You are not allowed to access storage while in Build Session Mode");
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if(!plugin.sessionExists(event.getPlayer().getName())) return;
        if(!event.getPlayer().hasPermission("buildsession.itemdrop")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("You are not allowed to drop items while in Build Session mode!");
        }
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if(!plugin.sessionExists(event.getPlayer().getName())) return;
        if(!event.getPlayer().hasPermission("buildsession.itemdrop")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if(plugin.sessionExists(event.getPlayer().getName())) {
            plugin.getSessions().get(event.getPlayer().getName()).setPlayer(event.getPlayer());
            event.getPlayer().sendMessage("You are in Build Session mode.  You may leave Build Session mode at any time by executing the command /endsession");
        }
    }
}
