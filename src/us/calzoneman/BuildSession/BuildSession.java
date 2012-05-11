
package us.calzoneman.BuildSession;

/**
 *
 * @author Calvin
 */

import org.bukkit.Bukkit;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class BuildSession extends JavaPlugin {
    public static final Logger log = Logger.getLogger("Minecraft");

    private HashMap<String, Session> sessions = new HashMap<String, Session>();
    private BuildSessionSaver saver;

    @Override
    public void onEnable() {
        PluginManager pm = Bukkit.getServer().getPluginManager();
        BuildSessionPlayerListener pl = new BuildSessionPlayerListener(this);
        BuildSessionBlockListener bl = new BuildSessionBlockListener(this);
        this.saver = new BuildSessionSaver(this);
        EventExecutor ee = new EventExecutor() {
			@Override
			public void execute(Listener listener, Event event) throws EventException {
				if(event instanceof BlockBreakEvent) {
					((BuildSessionBlockListener) listener).onBlockBreak((BlockBreakEvent) event);
				}
				else if(event instanceof BlockPlaceEvent) {
					((BuildSessionBlockListener) listener).onBlockPlace((BlockPlaceEvent) event);
				}
				else if(event instanceof PlayerInteractEvent) {
					((BuildSessionPlayerListener) listener).onPlayerInteract((PlayerInteractEvent) event);
				}
				else if(event instanceof PlayerInteractEntityEvent) {
					((BuildSessionPlayerListener) listener).onPlayerInteractEntity((PlayerInteractEntityEvent) event);
				}
				else if(event instanceof PlayerDropItemEvent) {
					((BuildSessionPlayerListener) listener).onPlayerDropItem((PlayerDropItemEvent) event);
				}
				else if(event instanceof PlayerPickupItemEvent) {
					((BuildSessionPlayerListener) listener).onPlayerPickupItem((PlayerPickupItemEvent) event);
				}
				else if(event instanceof PlayerJoinEvent) {
					((BuildSessionPlayerListener) listener).onPlayerJoin((PlayerJoinEvent) event);
				}
			}
		};
		pm.registerEvent(BlockBreakEvent.class, bl, EventPriority.NORMAL, ee, this);
		pm.registerEvent(BlockPlaceEvent.class, bl, EventPriority.NORMAL, ee, this);
		pm.registerEvent(PlayerInteractEvent.class, pl, EventPriority.HIGH, ee, this);
		pm.registerEvent(PlayerInteractEntityEvent.class, pl, EventPriority.HIGH, ee, this);
		pm.registerEvent(PlayerDropItemEvent.class, pl, EventPriority.HIGH, ee, this);
		pm.registerEvent(PlayerPickupItemEvent.class, pl, EventPriority.NORMAL, ee, this);
		pm.registerEvent(PlayerJoinEvent.class, pl, EventPriority.NORMAL, ee, this);

        sessions = saver.load(getSavePath());
        // Schedule a save every 2 minutes in case of server failure
        Bukkit.getScheduler().scheduleAsyncRepeatingTask(this, new BuildSessionSaver(this), 2400, 2400);
        log.info("[BuildSession] Plugin Enabled");
    }

    @Override
    public void onDisable() {
        saver.run();
        log.info("[BuildSession] Plugin Disabled");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(label.equals("session")) {
            if(args.length == 0 && sender instanceof Player && ((Player)sender).hasPermission("buildsession.session.self")) {
                if(!sessionExists(sender.getName())) {
                    initSession((Player)sender);
                }
                else {
                    sender.sendMessage("You are already in a build session.");
                }
            }
            else if(args.length == 0  && !(sender instanceof Player)) {
                sender.sendMessage("This command must be executed by a player or with a player provided as an argument");
            }
            else if(args.length == 0 && sender instanceof Player && !((Player)sender).hasPermission("buildsession.session.self")) {
                sender.sendMessage("You don't have permission to begin a build session!");
            }
            else if(args.length == 1 && (sender instanceof Player && ((Player)sender).hasPermission("buildsession.session.others") || !(sender instanceof Player))) {
                Player ply = Bukkit.getServer().getPlayer(args[0]);
                if(ply != null) {
                    if(!sessionExists(ply.getName())) {
                        initSession(ply);
                        sender.sendMessage("Build Session initiated for " + args[0]);
                    }
                    else {
                        sender.sendMessage(args[0] + " is already in a build session");
                    }
                }
                else {
                    sender.sendMessage("Unable to find player " + args[0]);
                }
            }
            else if(args.length == 1 && sender instanceof Player && !((Player)sender).hasPermission("buildsession.session.others")) {
                sender.sendMessage("You don't have permission to begin a build session for someone else!");
            }
            else {
                sender.sendMessage("Usage: /session - Begins a build session for the player executing the command");
                sender.sendMessage("/session [player] - Begins a build session for [player]");
            }
        }
        else if(label.equals("endsession")) {
            if(args.length == 0 && sender instanceof Player && ((Player)sender).hasPermission("buildsession.endsession.self")) {
                if(sessionExists(sender.getName())) {
                    endSession((Player)sender);
                }
                else {
                    sender.sendMessage("You are not currently in a build session.");
                }
            }
            else if(args.length == 0 && !(sender instanceof Player)) {
                sender.sendMessage("This command must be executed by a player or with a player provided as an argument");
            }
            else if(args.length == 0 && sender instanceof Player && !((Player)sender).hasPermission("buildsession.endsession.self")) {
                sender.sendMessage("You don't have permission to end a build session!");
            }
            else if(args.length == 1 && (sender instanceof Player && ((Player)sender).hasPermission("buildsession.endsession.others") || !(sender instanceof Player))) {
                Player ply = Bukkit.getServer().getPlayer(args[0]);
                if(ply != null) {
                    if(sessionExists(ply.getName())) {
                        endSession(ply);
                        sender.sendMessage("Build Session ended for " + args[0]);
                    }
                    else {
                        sender.sendMessage(args[0] + " is not currently in a build session.");
                    }
                }
                else {
                    sender.sendMessage("Unable to find player " + args[0]);
                }
            }
            else if(args.length == 1 && sender instanceof Player && !((Player)sender).hasPermission("buildsession.endsession.others")) {
                sender.sendMessage("You don't have permission to end a build session for someone else!");
            }
            else {
                sender.sendMessage("Usage: /endsession - Ends a build session for the player executing the command");
                sender.sendMessage("/endsession [player] - Ends a build session for [player]");
            }
        }
        return true;
    }
    
    public String getSavePath() {
    	return getDataFolder().getPath() + "/sessions.txt";
    }

    public HashMap<String, Session> getSessions() {
        return this.sessions;
    }

    public void initSession(Player ply) {
        Session s = new Session(ply);
        if(s.start()) {
            sessions.put(ply.getName(), s);
        }
    }

    public boolean sessionExists(String name) {
        return sessions.containsKey(name);
    }

    public void endSession(Player ply) {
        if(sessionExists(ply.getName()) && sessions.get(ply.getName()).end()) {
            sessions.remove(ply.getName());
        }
    }
}
