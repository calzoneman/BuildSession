
package us.calzoneman.BuildSession;

/**
 *
 * @author Calvin
 */
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class BuildSessionPlayerListener extends PlayerListener {
    private BuildSession plugin;
    
    public BuildSessionPlayerListener(BuildSession plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
        if(!plugin.sessionExists(event.getPlayer().getName())) return;
        if(event.getClickedBlock() != null && !event.getPlayer().hasPermission("buildsession.inventoryaccess")) {
            switch(event.getClickedBlock().getTypeId()) {
                case 23:
                case 54:
                case 61:
                case 62:
                    event.setCancelled(true);
                    event.getPlayer().sendMessage("You are not allowed to access storage blocks while in Build Session mode!");
                    break;
                default:
                    break;
            }
        }
    }
    
    @Override
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if(!plugin.sessionExists(event.getPlayer().getName())) return;
        if(!event.getPlayer().hasPermission("buildsession.itemdrop")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("You are not allowed to drop items while in Build Session mode!");
        }
    }
    
    @Override
    public void onPlayerJoin(PlayerJoinEvent event) {
        if(plugin.sessionExists(event.getPlayer().getName())) {
            event.getPlayer().sendMessage("You are in Build Session mode.  You may leave Build Session mode at any time by executing the command /endsession");
        }
    }
}
