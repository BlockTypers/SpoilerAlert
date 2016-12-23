package com.blocktyper.spoileralert;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpoilDateCommand implements CommandExecutor {

	private static String COMMAND_SPOIL_DATE = "spoil-date";


	private SpoilerAlertPlugin plugin;

	public SpoilDateCommand(SpoilerAlertPlugin plugin) {
		this.plugin = plugin;
		plugin.getCommand(COMMAND_SPOIL_DATE).setExecutor(this);
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		try {
			if (!(sender instanceof Player)) {
				return false;
			}

			Player player = (Player) sender;
			
			player.sendMessage("Date: " + new SpoilerAlertCalendar(player.getWorld()).getDisplayDate());

		} catch (Exception e) {
			plugin.warning("error running '" + label + "':  " + e.getMessage());
			return false;
		}
		
		return true;
	}


}
