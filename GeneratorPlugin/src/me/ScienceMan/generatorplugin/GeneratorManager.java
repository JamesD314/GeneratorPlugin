package me.ScienceMan.generatorplugin;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import net.md_5.bungee.api.ChatColor;


public class GeneratorManager{
	
	private Main plugin;
	private ConfigManager configs;
	private ArrayList<Location> generators;
	private BukkitTask runnable;
	
	public GeneratorManager(Main plugin) {
		this.plugin = plugin;
		this.configs = Main.getConfigs();
		this.generators = new ArrayList<>();
		loadGenerators();
		runnable = new BukkitRunnable() {
			@Override
			public void run() {
				for(int i = 0; i < generators.size(); i++)
					((Generator)generators.get(i).getBlock().getMetadata("generator").get(0).value()).run();
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
	
	public void add(Location l, Vector v, int level) {
		Location newL = new Location(l.getWorld(), l.getX() + v.getX(), l.getY(), l.getZ() + v.getZ());
		Generator g = new Generator(plugin, newL, level);
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
		configs.getConfig("generators").set("generators", null);
		configs.getConfig("generators").createSection("generators");
		for(int i = 0; i < generators.size(); i++){
			Location l = generators.get(i);
			String s = "generators.generator" + i;
			configs.getConfig("generators").createSection(s);
			configs.getConfig("generators").createSection(s + ".world");
			configs.getConfig("generators").set(s + ".world", l.getWorld().getName());
			configs.getConfig("generators").createSection(s + ".x");
			configs.getConfig("generators").set(s + ".x", l.getX());
			configs.getConfig("generators").createSection(s + ".y");
			configs.getConfig("generators").set(s + ".y", l.getY());
			configs.getConfig("generators").createSection(s + ".z");
			configs.getConfig("generators").set(s + ".z", l.getZ());
			configs.getConfig("generators").createSection(s + ".level");
			configs.getConfig("generators").set(s + ".level", ((Generator)(l.getBlock().getMetadata("generator").get(0).value())).getLevel());
			configs.getConfig("generators").createSection(s + ".dx");
			configs.getConfig("generators").set(s + ".dx", l.getDirection().getX());
			configs.getConfig("generators").createSection(s + ".dy");
			configs.getConfig("generators").set(s + ".dy", l.getDirection().getY());
			configs.getConfig("generators").createSection(s + ".dz");
			configs.getConfig("generators").set(s + ".dz", l.getDirection().getZ());
		}
		configs.saveConfig("generators");
	}
	
	private void loadGenerators() {
		YamlConfiguration c = configs.getConfig("generators");
		if(c.contains("generators")) {
			int i = 0;
			String key = "generators.generator" + i;
			while(c.contains(key)) {
				Location l = new Location(plugin.getServer().getWorld(c.getString(key + ".world")), c.getDouble(key + ".x"), c.getDouble(key + ".y"), c.getDouble(key + ".z"));
				add(l, new Vector(c.getDouble(key + ".dx"), c.getDouble(key + ".dy"), c.getDouble(key + ".dz")), c.getInt("generators.generator" + i + ".level"));
				i++;
				key = "generators.generator" + i;
			}
			plugin.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "Loaded " + generators.size() + " generators from config");
		}
		else {
			plugin.getServer().getConsoleSender().sendMessage(ChatColor.LIGHT_PURPLE + "No generators found in config. Continuing...");
		}
	}
	
	public void stopRunnable() {
		runnable.cancel();
	}
}
