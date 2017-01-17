package com.blocktyper.spoileralert.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import com.blocktyper.spoileralert.PerishableBlock;
import com.blocktyper.spoileralert.SpoilerAlertCalendar;
import com.blocktyper.spoileralert.SpoilerAlertPlugin;

public class BlockPlaceListener extends SpoilerAlertListenerBase {

	public BlockPlaceListener(SpoilerAlertPlugin plugin) {
		super(plugin);
	}

	/*
	 * ON PLACE BLOCK
	 */
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public void onBlockPlaceEvent(BlockPlaceEvent event) {

		PerishableBlock perishableBlock = createPerishableBlock(event.getBlock(), null);
		clearPerishableBlock(perishableBlock.getId());

		ItemStack itemInHand = plugin.getPlayerHelper().getItemInHand(event.getPlayer());

		if (itemInHand == null) {
			plugin.debugInfo("[onBlockPlaceEvent] itemInHand == null");
			return;
		}

		if (itemInHand.getItemMeta() == null || itemInHand.getItemMeta().getDisplayName() == null) {
			plugin.debugInfo(
					"[onBlockPlaceEvent] cakeItem.getItemMeta() == null || cakeItem.getItemMeta().getDisplayName() == null");
			return;
		}

		String expirationString = getExpirationDateText(itemInHand, event.getPlayer());

		if (expirationString == null) {
			return;
		}

		SpoilerAlertCalendar expirationDate = SpoilerAlertCalendar.getSpoilersCalendarFromDateString(expirationString);

		if (expirationDate == null) {
			return;
		}

		plugin.debugInfo("[onBlockPlaceEvent] place block (itemInHand): " + itemInHand.getItemMeta().getDisplayName());
		plugin.debugInfo("[onBlockPlaceEvent] place block (block): " + event.getBlock().getType().name());
		expirationDate.addDays(-1);
		perishableBlock.setExpirationDate(expirationDate.getNbtDateString());
		addPerishableBlock(perishableBlock);
	}

	private void clearPerishableBlock(String id) {
		try {
			initPerishableBlockRepo();
			perishableBlockRepo.getMap().put(id, null);
			updatePerishableBlockRepo();
		} catch (Exception e) {
			plugin.warning("Unexpected error while clearing perishable block: " + e.getMessage());
		}
	}

	private boolean addPerishableBlock(PerishableBlock perishableBlock) {
		try {
			initPerishableBlockRepo();
			perishableBlockRepo.getMap().put(perishableBlock.getId(), perishableBlock);
			updatePerishableBlockRepo();
		} catch (Exception e) {
			plugin.warning("Unexpected error while saving perishable block: " + e.getMessage());
			return false;

		}

		return true;
	}

}
