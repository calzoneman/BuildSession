package us.calzoneman.BuildSession;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Calvin
 * If a LICENSE file is enclosed, please refer to it for licensing information
 */
public class BuildSessionSaver implements Runnable {
    private final BuildSession plugin;

    public BuildSessionSaver(BuildSession plugin) {
        this.plugin = plugin;
    }
    @Override
    public void run() {
        save(plugin.getSessions(), plugin.getSavePath());
    }

    public void save(HashMap<String, Session> sessions, String filename) {
        JSONObject all = new JSONObject();
        if(!plugin.getDataFolder().exists()) {
    		plugin.getDataFolder().mkdirs();
    	}
        for(String key : sessions.keySet()) {
            try {
                all.put(key, sessions.get(key).toJSONObject());
            }
            catch(JSONException ex) {
                BuildSession.log.severe("JSON Save Error: ");
                ex.printStackTrace();
            }
        }
        try {
        	if(!new File(filename).exists()) {
        		new File(filename).createNewFile();
        	}
            PrintWriter pw = new PrintWriter(filename);
            pw.write(all.toString(4));
            pw.close();
        }
        catch (IOException ex) {
            BuildSession.log.severe("Save Error: ");
            ex.printStackTrace();
        }
        catch (JSONException ex) {
            BuildSession.log.severe("JSON Error: ");
            ex.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
	public HashMap<String, Session> load(String filename) {
        HashMap<String, Session> sessions = new HashMap<String, Session>();
        try {
        	if(!plugin.getDataFolder().exists()) {
        		plugin.getDataFolder().mkdirs();
        	    return sessions;
        	}
        	if(!new File(filename).exists()) {
        		new File(filename).createNewFile();
        		return sessions;
        	}
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String input = "";
            String line = "";
            while(line != null) {
                line = br.readLine();
                input += line;
            }
            br.close();
            JSONObject in = new JSONObject(input);
            Iterator<String> iter = (Iterator<String>) in.keys();
            while(iter.hasNext()) {
                String pname = iter.next();
                Session s = new Session(pname);
                s.loadJSONObject(in.getJSONObject(pname));
                sessions.put(pname, s);
            }
        }
        catch(Exception ex) {
            BuildSession.log.severe("Load error: ");
            ex.printStackTrace();
        }
        return sessions;
    }

    public void serialize(ItemStack[] inv, String filename) {
        JSONArray invArray = new JSONArray();
        String output = "";
        try {
            for(ItemStack it : inv) {
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
                    invArray.put(item);
                }
            }
            output = invArray.toString(4);
        }
        catch(JSONException ex) {
            BuildSession.log.severe("JSON Error: ");
            ex.printStackTrace();
        }

        try {
            PrintWriter pw = new PrintWriter(filename);
            pw.print(output);
            pw.close();
        }
        catch(IOException ex) {
            BuildSession.log.severe("Save Error: ");
            ex.printStackTrace();
        }
    }

    public ItemStack[] deserialize(String filename) {
        String input = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line = "";
            while(line != null) {
                line = br.readLine();
                input += line;
            }
        }
        catch(IOException ex) {
            BuildSession.log.severe("Load Error: ");
            ex.printStackTrace();
        }
        ItemStack[] inventory = new ItemStack[36];
        try {
            JSONArray invArray = new JSONArray(input);
            for(int i = 0; i < invArray.length() && i < inventory.length; i++) {
                JSONObject item = invArray.getJSONObject(i);
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
                    inventory[i] = it;
                }
            }
        }
        catch(JSONException ex) {
            BuildSession.log.severe("JSON Load Error: ");
            ex.printStackTrace();
        }
        return inventory;
    }
}
