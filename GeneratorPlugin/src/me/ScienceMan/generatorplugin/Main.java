package me.ScienceMan.generatorplugin;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;

public class Main extends JavaPlugin{
	
	private static GeneratorManager generators;
	private static ConfigManager configs;
	private static Commands commands;
	private static Events events;
	private static Economy economy;
	
	@Override
	public void onEnable() {
	    if (!setupEconomy()) {
	        getPluginLoader().disablePlugin(this);
	        getServer().getConsoleSender().sendMessage(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
	    }
		
		// Loads configs
		configs = new ConfigManager(this);
		configs.loadConfig("pricing");
		configs.loadConfig("generators");
		
		generators = new GeneratorManager(this);
		
		// Loads events
		events = new Events(this);
		getServer().getPluginManager().registerEvents(events, this);
		
		// Loads commands
        commands = new Commands();
		for(int i = 0; i < commands.commands.length; i++)
			getCommand(commands.commands[i]).setExecutor(commands);
		
		getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "Generator Plugin has been enabled");
	}
	
	@Override
	public void onDisable() {
		generators.stopRunnable();
		generators.save();
		// Sends a confirmation that the plugin has been disabled
		getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "Generator Plugin has been disabled");
	}
	
	private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager()
                .getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }
	
	public static Economy getEconomy() {
		return economy;
	}
	
	public static YamlConfiguration getConfig(String key) {
		return configs.getConfig(key);
	}
	
	public static GeneratorManager getGeneratorManger() {
		return generators;
	}
	
	public static void saveConfig(String key) {
		configs.saveConfig(key);
	}
	
	public static void reloadConfigs() {
		configs.reloadConfigs();
		Main.getGeneratorManger().reload();
	}
}
