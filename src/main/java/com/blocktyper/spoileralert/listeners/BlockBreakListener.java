package com.blocktyper.spoileralert.listeners;

import java.util.Collection;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import com.blocktyper.spoileralert.SpoilerAlertCalendar;
import com.blocktyper.spoileralert.SpoilerAlertPlugin;

public class BlockBreakListener extends SpoilerAlertListenerBase {

	public BlockBreakListener(SpoilerAlertPlugin plugin) {
		super(plugin);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public void onBlockBreakEvent(BlockBreakEvent event) {
		Collection<ItemStack> drops = event.getBlock()
				.getDrops(plugin.getPlayerHelper().getItemInHand(event.getPlayer()));

		if (drops == null || drops.isEmpty()) {
			plugin.debugInfo("[onBlockBreakEvent] drops == null || drops.isEmpty()");
			return;
		}

		SpoilerAlertCalendar expirationDate = getExistingExpirationDate(event.getBlock());
		Long daysSourceExpired = null;

		if (expirationDate != null) {
			plugin.debugInfo("[onBlockBreakEvent] expirationDate !=  null: " + expirationDate.getDisplayDate());
			daysSourceExpired = getDaysExpired(expirationDate.getDisplayDate(), event.getBlock().getWorld());
			daysSourceExpired = daysSourceExpired != null ? daysSourceExpired + 1 : null;
			plugin.debugInfo("[onBlockBreakEvent] daysSourceExpired: " + (daysSourceExpired != null ? daysSourceExpired : "null"));
			
			for (ItemStack drop : drops) {
				ItemStack newDrop = setExpirationDate(drop, event.getBlock().getWorld(), daysSourceExpired);
				event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), newDrop);
			}
			
			event.getBlock().setType(Material.AIR);
			event.setCancelled(true);
			
		}else{
			plugin.debugInfo("[onBlockBreakEvent] expirationDate == null");
		}
	}

}
