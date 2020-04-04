package me.ScienceMan.generatorplugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.bukkit.configuration.file.YamlConfiguration;

import net.md_5.bungee.api.ChatColor;


public class ConfigManager{
	private Main plugin;
	private static HashMap<String, YamlConfiguration> configs = new HashMap<>();
	
	public ConfigManager(Main p) {
		plugin = p;
	}
	
	public void loadConfig(String key) {
		if(configs.containsKey(key))
			configs.remove(key);
		
		if(key.endsWith(".yml"))
			key = key.substring(0, key.length() - 4);
		
		if(!plugin.getDataFolder().exists())
			plugin.getDataFolder().mkdir();
		
		File file = new File(plugin.getDataFolder(), key + ".yml");
		
		if(!file.exists()) {
			plugin.saveResource(key + ".yml", false);
			plugin.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "Created " + key + ".yml");
		}
		
		YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
		
		configs.put(key, config);
	}
	
	// Returns null if not found
	public YamlConfiguration getConfig(String key){
		if(key.endsWith(".yml"))
			key = key.substring(0, key.length() - 4);
		if(configs.containsKey(key))
			return configs.get(key);
		plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "Could not find " + key + " in me.ScienceMan.generatorplugin.ConfigManager.files");
		return null;
	}
	
	public boolean saveConfig(String key) {
		if(key.endsWith(".yml"))
			key = key.substring(0, key.length() - 4);
		if(configs.containsKey(key)) {
			try {
				configs.get(key).save(new File(plugin.getDataFolder(), key + ".yml"));
				plugin.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "Successfully saved " + key + ".yml");
				return true;
			}
			catch(IOException e) {
				plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "Failed to save " + key + ".yml due to \n\t" + e.getMessage());
				return false;
			}
		}
		else {
			plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "Failed to save file due to nonexistant key in in me.ScienceMan.generatorplugin.ConfigManager.configs");
			return false;
		}
	}
	
	public void reloadConfigs() {
		configs.forEach( (String key, YamlConfiguration c) -> {
			if(key.endsWith(".yml"))
				key = key.substring(0, key.length() - 4);
			if(configs.containsKey(key)) {
				configs.replace(key, YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), key + ".yml")));
			}
			else {
				plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "Failed to save file due to nonexistant key in in me.ScienceMan.generatorplugin.ConfigManager.configs");
			}
		});
	}
}
