package us.calzoneman.BuildSession;

import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Calvin
 * If a LICENSE file is enclosed, please refer to it for licensing information
 */
public class Session {
    private Player player;
    private String pname;
    private ItemStack[] savedInventory;
    private long startTime;

    public Session(Player player) {
        this.player = player;
        this.pname = player.getName();
    }

    public Session(String pname) {
        this.pname = pname;
        this.player = Bukkit.getServer().getPlayer(pname);
    }

    public boolean start() {
        try {
            savedInventory = player.getInventory().getContents();
            player.getInventory().clear();
            player.setGameMode(GameMode.CREATIVE);
            startTime = System.nanoTime();
            player.sendMessage("You are in Build Session mode.  You may leave Build Session mode at any time by executing the command /endsession");
            return true;
        }
        catch(Exception ex) {
            return false;
        }
    }

    public boolean end() {
        try {
            if(player == null) player = Bukkit.getServer().getPlayer(pname);
            player.setGameMode(GameMode.SURVIVAL);
            player.getInventory().clear();
            player.getInventory().setContents(savedInventory);
            player.sendMessage("You have left Build Session mode.  Your original inventory has been restored.");
            return true;
        }
        catch(Exception ex) {
            return false;
        }
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public ItemStack[] getSavedInventory() {
        return savedInventory;
    }

    public void setSavedInventory(ItemStack[] savedInventory) {
        this.savedInventory = savedInventory;
    }

    public JSONObject toJSONObject() {
        JSONObject session = new JSONObject();
        JSONArray inv = new JSONArray();
        try {
            for(ItemStack it : savedInventory) {
                if(it != null) {
                    JSONObject item = new JSONObject();
                    item.put("id", it.getTypeId());
                    item.put("data", it.getData() != null ? it.getData().getData() : 0);
                    item.put("amount", it.getAmount());
                    item.put("durability", it.getDurability());
                    JSONObject enchantments = new JSONObject();
                    Map<Enchantment, Integer> itEnchantments = it.getEnchantments();
                    for(Enchantment ench : itEnchantments.keySet()) {
                        enchantments.put(ench.getName(), itEnchantments.get(ench));
                    }
                    item.put("enchantments", enchantments);
                    inv.put(item);
                }
            }
            session.put("player", pname);
            session.put("start", startTime);
            session.put("inventory", inv);
        }
        catch(JSONException ex) {
            BuildSession.log.severe("Error when exporting session to JSON: ");
            ex.printStackTrace();
        }
        return session;
    }

    public void loadJSONObject(JSONObject session) {
        try {
            this.player = Bukkit.getServer().getPlayer(session.getString("player"));
            this.pname = session.getString("player");
            this.startTime = session.getLong("start");
            JSONArray inv = session.getJSONArray("inventory");
            this.savedInventory = new ItemStack[36];
            for(int i = 0; i < inv.length() && i < savedInventory.length; i++) {
                JSONObject item = inv.getJSONObject(i);
                if(item != null) {
                    ItemStack it = new ItemStack(item.getInt("id"),
                                                    item.getInt("amount"),
                                                    (short)item.getInt("durability"),
                                                    (byte)item.getInt("data"));
                    JSONObject enchs = item.getJSONObject("enchantments");
                    if(enchs != null) {
                        String[] keys = JSONObject.getNames(enchs);
                        if(keys != null && keys.length > 0) {
                            for(String key : keys) {
                                it.addUnsafeEnchantment(Enchantment.getByName(key), enchs.getInt(key));
                            }
                        }
                    }
                    savedInventory[i] = it;
                }
            }
        }
        catch(JSONException ex) {
            BuildSession.log.severe("JSON load error: ");
            ex.printStackTrace();
        }
    }
}
