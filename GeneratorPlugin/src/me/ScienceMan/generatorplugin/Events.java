package me.ScienceMan.generatorplugin;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import net.md_5.bungee.api.ChatColor;

public class Events implements Listener{
	Main plugin;
	public Events(Main plugin) {
		this.plugin = plugin;
	}
	@EventHandler
	public void onPlace(BlockPlaceEvent e) {
		if(e.getItemInHand().hasItemMeta() && e.getItemInHand().getItemMeta().hasLore()) {
			List<String> lore = e.getItemInHand().getItemMeta().getLore();
			if(ChatColor.stripColor(lore.get(0)).contains("Generator")) {
				Main.getGeneratorManger().add(e.getBlock().getLocation(), e.getPlayer().getFacing().getOppositeFace().getDirection(),
						Integer.parseInt(ChatColor.stripColor(lore.get(0).split(" ")[1])));
			}
		}
	}
	
	@EventHandler
	public void onBreak(BlockBreakEvent e) {
		if(Main.getGeneratorManger().getGenerators().contains(e.getBlock().getLocation())) {
			Main.getGeneratorManger().remove(e.getBlock());
		}
	}
	
	@EventHandler
	public void onEntityExplosion(EntityExplodeEvent e){
		for(int i = 0; i < e.blockList().size(); i++) {
			Block b = e.blockList().get(i);
			if(Main.getGeneratorManger().getGenerators().contains(b.getLocation())) {
				Main.getGeneratorManger().remove(b);
				e.blockList().remove(i);
				i--;
			}
		}
	}
	
	@EventHandler
	public void playerInteract(PlayerInteractEvent e) {
		// If generator was right-clicked
		if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && !e.getPlayer().isSneaking() &&	Main.getGeneratorManger().getGenerators().contains(e.getClickedBlock().getLocation())){
			e.setUseInteractedBlock(Result.DENY);
			Generator g = (Generator)e.getClickedBlock().getMetadata("generator").get(0).value();
			g.openUpgradeMenu(e.getPlayer());
		}
	}
	
	@EventHandler
	public void InvenClick(InventoryClickEvent e) {
		if(e.getView().getTitle().equals(ChatColor.BLUE + "Generator Upgrade Menu")) {
			e.setCancelled(true);
			if(e.getCurrentItem() != null && e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.DARK_AQUA + "Upgrade Generator")) {
				Location l = Main.getGeneratorManger().get(e.getClickedInventory());
				if(l != null) {
					Generator g = (Generator)l.getBlock().getMetadata("generator").get(0).value();
					Player p = (Player)e.getWhoClicked();
					if(g.canLevelUp()) {
						if(Main.getEconomy().has(p, g.getLevelUpCost())) {
							g.levelUp();
							l.getBlock().setMetadata("generator", new FixedMetadataValue(plugin, g));
							p.closeInventory();
							p.sendMessage(ChatColor.GREEN + "Successfully upgraded your generator");
						}
						else {
							p.closeInventory();
							p.sendMessage(ChatColor.RED + "You do not have the required funds to make this upgrade");
						}
					}
					else {
						p.closeInventory();
						p.sendMessage(ChatColor.RED + "The generator is at the highest possible level");
					}
				}
			}
		}
	}
}
