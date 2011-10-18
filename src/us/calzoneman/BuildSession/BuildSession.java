
package us.calzoneman.BuildSession;

/**
 *
 * @author Calvin
 */

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.File;
import org.bukkit.GameMode;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;

public class BuildSession extends JavaPlugin {
    public static final Logger log = Logger.getLogger("Minecraft");
    
    private ArrayList<String> sessions = new ArrayList<String>(); // Stores who is in a session
    private HashMap<String, ItemStack[]> inventories = new HashMap<String, ItemStack[]>(); // Stores pre-session inventories
    
    @Override
    public void onEnable() {
        PluginManager pm = Bukkit.getServer().getPluginManager();
        BuildSessionPlayerListener pl = new BuildSessionPlayerListener(this);
        BuildSessionBlockListener bl = new BuildSessionBlockListener(this);
        pm.registerEvent(Type.PLAYER_INTERACT, pl, Priority.High, this);
        pm.registerEvent(Type.PLAYER_DROP_ITEM, pl, Priority.Normal, this);
        pm.registerEvent(Type.BLOCK_BREAK, bl, Priority.Normal, this);
        pm.registerEvent(Type.BLOCK_PLACE, bl, Priority.Normal, this);
        loadSessions("plugins/BuildSession/sessions.txt");
        loadInventories("plugins/BuildSession/inv/");
        log.info("[BuildSession] Plugin Enabled");
    }
    
    @Override
    public void onDisable() {
        saveSessions("plugins/BuildSession/sessions.txt");
        saveInventories("plugins/BuildSession/inv/");
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
    
    public void initSession(Player ply) {
        sessions.add(ply.getName());
        inventories.put(ply.getName(), ply.getInventory().getContents());
        ply.getInventory().clear();
        ply.setGameMode(GameMode.CREATIVE);
        ply.sendMessage("You are in Build Session mode.  You may leave Build Session mode at any time by executing the command /endsession");
    } 
    
    public boolean sessionExists(String name) {
        return sessions.contains(name);
    }
    
    public void endSession(Player ply) {
        sessions.remove(ply.getName());
        ply.getInventory().setContents(inventories.get(ply.getName()));
        inventories.remove(ply.getName());
        ply.setGameMode(GameMode.SURVIVAL);
        ply.sendMessage("You have left Build Session mode.  Your original inventory has been restored.");
    }
    
    public void saveSessions(String filename) {
        try {
            PrintWriter out = new PrintWriter(new FileWriter(filename));
            for(String s : sessions) {
                out.println(s);
            }
            out.flush();
            out.close();
            log.info("[BuildSession] Saved sessions to disk");
        }
        catch(Exception e) {
            log.severe("[BuildSession] Failed to flush sessions to disk: ");
            log.severe(e.toString());
        }
        
    }
    
    public void loadSessions(String filename) {
        try {
            BufferedReader in = new BufferedReader(new FileReader(filename));
            String line = in.readLine();
            while(line != null) {
                if(!line.equals("")) {
                    sessions.add(line);
                }
                line = in.readLine();
            }
            in.close();
            log.info("[BuildSession] Loaded session data from disk");
        }
        catch(Exception e) {
            log.severe("[BuildSession] Failed to load session data: ");
            log.severe(e.toString());
        }
    }
    
    public void saveInventories(String foldername) {
        try {
            // Clean out the folder first
            File dir = new File(foldername);
            for(File f : dir.listFiles()) {
                f.delete();
            }
            for(String s : inventories.keySet()) {
                PrintWriter out = new PrintWriter(new FileWriter(foldername + s + ".inv"));
                for(String str : serializeInventory(inventories.get(s))) {
                    out.println(str);
                }
                out.flush();
                out.close();
            }
            log.info("[BuildSession] Saved inventories to disk");
        }
        catch(Exception e) {
            log.severe("[BuildSession] Failed to flush inventories to disk: ");
            log.severe(e.toString());
            log.severe(e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void loadInventories(String foldername) {
        try {
            File dir = new File(foldername);
            String[] files = dir.list();
            for(String file : files) {
                if(file.endsWith(".inv")) {
                    BufferedReader in = new BufferedReader(new FileReader(foldername + file));
                    String line = in.readLine();
                    ArrayList<String> lines = new ArrayList<String>();
                    while(line != null) {
                        if(line.equals("")) continue;
                        lines.add(line);
                        line = in.readLine();
                    }
                    ItemStack[] inventory = deserializeInventory(lines);
                    inventories.put(file.substring(0, file.indexOf(".inv")), inventory);
                }
            }
            log.info("[BuildSession] Loaded inventories from disk");
        }
        catch(Exception e) {
            log.severe("[BuildSession] Failed to load inventories from disk: ");
            log.severe(e.toString());
        }
    }
    
    public ArrayList<String> serializeInventory(ItemStack[] it) {
        ArrayList<String> result = new ArrayList<String>();
        for(ItemStack i : it) {
            if(i == null || i.getAmount() == 0) continue;
            result.add(i.getTypeId() + ":" + (i.getData() != null ? i.getData().getData() : 0) + ";" + i.getDurability() + ";" + i.getAmount());
        }
        return result;
    }
    
    public ItemStack[] deserializeInventory(ArrayList<String> serialized) {
        ItemStack[] it = new ItemStack[36];
        int i = 0;
        try {
            for(String s : serialized) {
                String[] stuff = s.split(";");
                if(stuff.length < 3) continue;
                if(!stuff[0].contains(":")) continue;
                String[] iddata = stuff[0].split(":");
                ItemStack stack = new ItemStack(Integer.parseInt(iddata[0]), Integer.parseInt(stuff[2]), Short.parseShort(stuff[1]), Byte.parseByte(iddata[1]));
                it[i] = stack;
                i++;
            }
        }
        catch(Exception e) {
            log.severe("[BuildSession] Failed to deserialize inventory: ");
            log.severe(e.toString());
        }
        return it;
    }
    
    
}
