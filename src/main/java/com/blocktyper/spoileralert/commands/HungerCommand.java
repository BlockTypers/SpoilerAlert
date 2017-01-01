package com.blocktyper.spoileralert.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.blocktyper.spoileralert.SpoilerAlertPlugin;

public class HungerCommand implements CommandExecutor {

	private static String COMMAND_SPOIL_DATE = "spoil-hunger";

	private SpoilerAlertPlugin plugin;

	public HungerCommand(SpoilerAlertPlugin plugin) {
		this.plugin = plugin;
		plugin.getCommand(COMMAND_SPOIL_DATE).setExecutor(this);
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		try {
			if (!(sender instanceof Player)) {
				return false;
			}

			Player player = (Player) sender;

			player.setFoodLevel(player.getFoodLevel() / 2);
			player.setSaturation(0);

		} catch (Exception e) {
			plugin.warning("error running '" + label + "':  " + e.getMessage());
			return false;
		}

		return true;
	}

}
