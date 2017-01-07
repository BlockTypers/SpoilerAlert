package com.blocktyper.spoileralert.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.blocktyper.spoileralert.LocalizedMessageEnum;
import com.blocktyper.spoileralert.SpoilerAlertCalendar;
import com.blocktyper.spoileralert.SpoilerAlertPlugin;

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
			String message = plugin.getLocalizedMessage(LocalizedMessageEnum.DATE.getKey(), player);
			player.sendMessage(message + ": " + new SpoilerAlertCalendar(player.getWorld()).getDisplayDate());
		} catch (Exception e) {
			plugin.warning("error running '" + label + "':  " + e.getMessage());
			return false;
		}
		
		return true;
	}


}
