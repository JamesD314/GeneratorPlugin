package me.ScienceMan.generatorplugin;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import net.md_5.bungee.api.ChatColor;


public class GeneratorManager{
	
	private Main plugin;
	private ArrayList<Location> generators;
	private BukkitTask runnable;
	
	public GeneratorManager(Main plugin) {
		this.plugin = plugin;
		
		loadGenerators();

		runnable = new BukkitRunnable() {
			@Override
			public void run() {
				for(int i = 0; i < Main.getGeneratorManger().getGenerators().size(); i++) {
					((Generator)generators.get(i).getBlock().getMetadata("generator").get(0).value()).run();
				}
			}
		}.runTaskTimer(plugin, 40, 20);
	}
	
	public Location get(Inventory inv) {
		for(Location l : generators) {
			if(l.getBlock().hasMetadata("generator") && ((Generator)l.getBlock().getMetadata("generator").get(0).value()).getInventory() == inv) {
				return l;
			}
		}
		return null;
	}
	
	public void add(Location l, Location dropLocation, int level, int lastRunTime) {
		if(level > Generator.getMaxLevel())
			level = Generator.getMaxLevel();
		Generator g = new Generator(plugin, dropLocation, level, lastRunTime);
		l.getBlock().setMetadata("generator", new FixedMetadataValue(plugin, g));
		generators.add(l);
	}
	
	public boolean has(Location l) {
		return generators.contains(l);
	}
	
	public ArrayList<Location> getGenerators(){
		return generators;
	}
	
	public boolean remove(Block b) {
		Generator g = (Generator)b.getMetadata("generator").get(0).value();
		b.setType(Material.AIR);
		ItemStack is = new ItemStack(Material.FURNACE, 1);
		is.setItemMeta(g.getMeta());
		b.getWorld().dropItemNaturally(b.getLocation(), is);
		return generators.remove(b.getLocation());
	}
	
	public void save() {
		Main.getConfig("generators").set("generators", null);
		Main.getConfig("generators").createSection("generators");
		for(int i = 0; i < generators.size(); i++){
			Location l = generators.get(i);
			Generator g = (Generator)l.getBlock().getMetadata("generator").get(0).value();
			String s = "generators.generator" + i;
			Main.getConfig("generators").createSection(s);
			Main.getConfig("generators").createSection(s + ".world");
			Main.getConfig("generators").set(s + ".world", l.getWorld().getName());
			Main.getConfig("generators").createSection(s + ".x");
			Main.getConfig("generators").set(s + ".x", l.getX());
			Main.getConfig("generators").createSection(s + ".y");
			Main.getConfig("generators").set(s + ".y", l.getY());
			Main.getConfig("generators").createSection(s + ".z");
			Main.getConfig("generators").set(s + ".z", l.getZ());
			Main.getConfig("generators").createSection(s + ".dx");
			Main.getConfig("generators").set(s + ".dx", g.getDropLocation().getX());
			Main.getConfig("generators").createSection(s + ".dz");
			Main.getConfig("generators").set(s + ".dz", g.getDropLocation().getZ());
			Main.getConfig("generators").createSection(s + ".level");
			Main.getConfig("generators").set(s + ".level", g.getLevel());
			Main.getConfig("generators").createSection(s + ".lastRunTime");
			Main.getConfig("generators").set(s + ".lastRunTime", g.getLastRunTime());
		}
		Main.saveConfig("generators");
		
	}
	
	private void loadGenerators() {
		generators = new ArrayList<>();
		YamlConfiguration c = Main.getConfig("generators");
		if(c.contains("generators")) {
			int i = 0;
			String key = "generators.generator" + i;
			while(c.contains(key)) {
				Location l = new Location(plugin.getServer().getWorld(c.getString(key + ".world")), c.getDouble(key + ".x"), c.getDouble(key + ".y"), c.getDouble(key + ".z"));
				add(l, new Location(l.getWorld(), c.getDouble(key + ".dx"), l.getY(), c.getDouble(key + ".dz")), c.getInt("generators.generator" + i + ".level"), c.getInt("generators.generator" + i + ".lastRunTime"));
				i++;
				key = "generators.generator" + i;
			}
			plugin.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "Loaded " + generators.size() + " generators from config");
		}
		else {
			plugin.getServer().getConsoleSender().sendMessage(ChatColor.LIGHT_PURPLE + "No generators found in config. Continuing...");
		}
	}
	
	public void reload() {
		for(Location l : generators) {
			Generator g = (Generator)l.getBlock().getMetadata("generator").get(0).value();
			for(Player p : plugin.getServer().getOnlinePlayers()) {
				if(p.getOpenInventory().getTopInventory().equals(g.getInventory())){
					p.closeInventory();
				}
			}
		}
		save();
		loadGenerators();
	}
}
