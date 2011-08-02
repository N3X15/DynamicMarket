package com.gmail.haloinverse.DynamicMarket;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class Items {
    
    private HashMap<String, String> itemsData;
    private iProperty ItemsFile;
    
    public Items(String itemFileName, DynamicMarket thisPlugin) {
        // TODO: Handle cases where the plugin loads, but items.db doesn't.
        // TODO: Add user command for reloading items.db.
        // TODO: Make item file format more generic.
        ItemsFile = new iProperty(itemFileName);
        // Following code originally in setupItems from SimpleShop
        Map<String, String> mappedItems = null;
        itemsData = new HashMap<String, String>();
        
        try {
            mappedItems = ItemsFile.returnMap();
        } catch (Exception ex) {
            DynamicMarket.log.info(Messaging.bracketize(new StringBuilder().append(DynamicMarket.name).append(" Flatfile").toString()) + " could not grab item list!");
        }
        
        Iterator<String> it;
        
        if (mappedItems != null) {
            for (it = mappedItems.keySet().iterator(); it.hasNext();) {
                Object item = it.next();
                String id = (String) item;
                String itemName = mappedItems.get(item);
                itemsData.put(id, itemName);
            }
        }
    }
    
    public String name(String idString) {
        if (itemsData.containsKey(idString)) {
            return ((String) itemsData.get(idString));
        }
        if ((!(idString.contains(","))) && (itemsData.containsKey(idString + ",0"))) {
            return ((String) itemsData.get(idString + ",0"));
        }
        
        return ("UNKNOWN");
    }
    
    public String name(ItemClump itemData) {
        // Fetches the item name given an ItemClump.
        return name(Integer.toString(itemData.itemId) + (itemData.subType != 0 ? "," + Integer.toString(itemData.subType) : ""));
    }
    
    public void setName(String id, String name) {
        itemsData.put(id, name);
        ItemsFile.setString(id, name);
    }
    
    public static boolean has(Player player, ItemClump scanItem, int numBundles) {
        return (hasAmount(player, scanItem) >= (scanItem.count * numBundles));
    }
    
    public static int hasAmount(Player player, ItemClump scanItem) {
        PlayerInventory inventory = player.getInventory();
        ItemStack[] items = inventory.getContents();
        int amount = 0;
        
        for (ItemStack item : items) {
            if ((item != null) && (item.getTypeId() == scanItem.itemId) && (item.getDurability() == (byte) scanItem.subType)) {
                amount += item.getAmount();
            }
        }
        return amount;
    }
    
    public static boolean has(Player player, int itemId, int itemType,
            int amount) {
        return (hasAmount(player, new ItemClump(itemId, itemType)) >= amount);
    }
    
    public void remove(Player player, ItemClump item, int amount) {
        // TODO: Clean this up once Bukkit's inventory.removeItem(ItemStack[]) code handles subtypes.
        // Note that this removes (item.count * amount) items from the player's inventory.
        // This is to streamline removing multiples of bundle counts.
        PlayerInventory inventory = player.getInventory();
        ItemStack[] items = inventory.getContents();
        ItemStack thisItem;
        int toRemove = item.count * amount;
        
        for (int invSlot = 35; (invSlot >= 0) && (toRemove > 0); --invSlot) {
            thisItem = items[invSlot];
            if ((items[invSlot] != null) && (thisItem.getTypeId() == item.itemId) && (thisItem.getDurability() == (byte) item.subType)) {
                toRemove -= thisItem.getAmount();
                inventory.clear(invSlot);
            }
        }
        
        if (toRemove < 0) // removed too many! put some back!
        {
            inventory.addItem(new ItemStack[] { new ItemStack(item.itemId, -toRemove, (byte) item.subType) });
        }
    }
    
    public ItemClump nameLookup(String item) {
        // Given an item name (with optional ",<subtype>", returns an ItemClump loaded with the id and subtype.
        // If name is not found, returns null.
        
        int itemId = -1;
        int itemSubtype = 0;
        ItemClump returnedItem = null;
        
        for (String id : itemsData.keySet()) {
            if (((String) itemsData.get(id)).equalsIgnoreCase(item)) {
                if (id.contains(",")) {
                    itemId = Integer.valueOf(id.split(",")[0]).intValue();
                    itemSubtype = Integer.valueOf(id.split(",")[1]).intValue();
                } else {
                    itemId = Integer.valueOf(id).intValue();
                    itemSubtype = 0;
                }
                returnedItem = new ItemClump(itemId, itemSubtype);
                break;
            }
        }
        
        if (returnedItem != null) {
            return returnedItem;
        }
        
        // No exact match found: Try partial-name matching.
        String itemLower = item.toLowerCase();
        for (String id : itemsData.keySet()) {
            if (((String) itemsData.get(id)).toLowerCase().contains(itemLower)) {
                if (id.contains(",")) {
                    itemId = Integer.valueOf(id.split(",")[0]).intValue();
                    itemSubtype = Integer.valueOf(id.split(",")[1]).intValue();
                } else {
                    itemId = Integer.valueOf(id).intValue();
                    itemSubtype = 0;
                }
                returnedItem = new ItemClump(itemId, itemSubtype);
                break;
            }
        }
        
        return returnedItem;
    }
    
    public static boolean validateType(int id, int type) {
        if (type == 0) {
            return true;
        }
        if (type < 0) {
            return false;
        }
        
        switch (id) {
            case 35:
            case 63:
            case 351:
                return (type <= 15);
            case 17:
                return (type <= 2);
            case 53:
            case 64:
            case 67:
            case 71:
            case 77:
            case 86:
            case 91:
                return (type <= 3);
            case 66:
                return (type <= 9);
            case 68:
                return ((type >= 2) && (type <= 5));
            default:
                return false;
        }
    }
    
    public static boolean checkID(int id) // ? Confirms that the given id is known by bukkit as an item id.
    {
        for (Material item : Material.values()) {
            if (item.getId() == id) {
                return true;
            }
        }
        
        return false;
    }
}
