package me.ScienceMan.generatorplugin;

import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import net.md_5.bungee.api.ChatColor;

public class Generator {

	private static YamlConfiguration config = Main.getConfigs().getConfig("generator-pricing");
	private static int maxLevel = config.getInt("maxLevel");
	
	private Main plugin;
	private int level, lastRan;
	private Material material;
	private Inventory inventory;
	private Location dropLoc;
	
	public Generator(Main plugin, Location l, int level) {
		this.plugin = plugin;
		this.level = level;
		dropLoc = l;
		lastRan = 0;
		material = Material.valueOf(config.getString("level" + this.level + ".item"));
		updateInventory();
	}
	
	public static ItemMeta getMeta(int l) {
		ItemMeta meta = (new ItemStack(Material.FURNACE,1)).getItemMeta();
		meta.setDisplayName(ChatColor.DARK_AQUA + "Generator");
		String materialString = "";
		for(String s : config.getString("level" + l + ".item").split("_")) {
			materialString += s.charAt(0);
			materialString += s.substring(1).toLowerCase();
			materialString += " ";
		}
		meta.setLore(Arrays.asList(
				ChatColor.WHITE + "Level " + l + " Generator",
				ChatColor.WHITE + "Produces " + ChatColor.GOLD + "" + materialString.subSequence(0, materialString.length() - 1) + ChatColor.WHITE + " every " + ChatColor.GREEN + "" + config.getInt("level1.time") + ChatColor.WHITE + " seconds"));
		return meta;
	}
	
	public void updateInventory() {
		inventory = plugin.getServer().createInventory(null, 9, ChatColor.BLUE + "Generator Upgrade Menu");
		
		ItemStack empty = new ItemStack(Material.BLACK_STAINED_GLASS_PANE, 1);
		ItemMeta emptyMeta = empty.getItemMeta();
		emptyMeta.setDisplayName("*");
		empty.setItemMeta(emptyMeta);

		ItemStack currentIs = new ItemStack(Material.FURNACE, 1);
		currentIs.setItemMeta(getMeta(level));
		
		if(canLevelUp()) {
			ItemStack upgradeIs = new ItemStack(Material.valueOf(config.getString("level"+ (level + 1) + ".item")), 1);
			ItemMeta upgradeMeta = upgradeIs.getItemMeta();
			upgradeMeta.setDisplayName(ChatColor.DARK_AQUA + "Upgrade Generator");
			String materialString = "";
			for(String s : config.getString("level" + (level+1) + ".item").split("_")) {
				materialString += s.charAt(0);
				materialString += s.substring(1).toLowerCase();
				materialString += " ";
			}
			upgradeMeta.setLore(Arrays.asList(
					ChatColor.WHITE + "Upgrade to level " + (level + 1),
					ChatColor.WHITE + "Will produce " + ChatColor.GOLD + materialString.substring(0, materialString.length()-1) + ChatColor.WHITE + " every " + ChatColor.GREEN + config.getInt("level" + (level+1) + ".time") + ChatColor.WHITE + " seconds",
					ChatColor.WHITE + "Costs $" + config.getInt("level"+ (level + 1) + ".price")));
			upgradeIs.setItemMeta(upgradeMeta);
			
			ItemStack[] items = {empty, empty, empty, currentIs, empty, upgradeIs, empty, empty, empty};
			inventory.setContents(items);
		}
		else {
			ItemStack[] items = {empty, empty, empty, empty, currentIs, empty, empty, empty, empty};
			inventory.setContents(items);
		}
		
	}
	
	public void run() {
		lastRan++;
		if(lastRan >= config.getInt("level" + level + ".time")) {
			dropLoc.getWorld().dropItemNaturally(dropLoc, new ItemStack(material, 1)).setVelocity(new Vector());
			lastRan = 0;
		}
	}
	
	public ItemMeta getMeta() {
		return getMeta(level);
	}
	
	public int getLevel() {
		return level;
	}
	
	public boolean canLevelUp() {
		return level < maxLevel;
	}
	
	public double getLevelUpCost() {
		return config.getDouble("level" + (level+1));
	}
	
	public void openUpgradeMenu(Player player) {
		player.openInventory(inventory);
	}
	
	public void levelUp() {
		level++;
		material = Material.valueOf(config.getString("level" + level + ".item"));
		updateInventory();
	}
	
	public Inventory getInventory() {
		return inventory;
	}
	
}
