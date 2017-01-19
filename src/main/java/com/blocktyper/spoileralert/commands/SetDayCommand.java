package com.blocktyper.spoileralert.commands;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.blocktyper.spoileralert.SpoilerAlertCalendar;
import com.blocktyper.spoileralert.SpoilerAlertPlugin;

public class SetDayCommand implements CommandExecutor {

	private SpoilerAlertPlugin plugin;

	Map<String, Map<String, Long>> playerWorldReturnMap;

	public SetDayCommand(SpoilerAlertPlugin plugin) {
		this.plugin = plugin;
		playerWorldReturnMap = new HashMap<String, Map<String, Long>>();
		plugin.getCommand("spoil-day").setExecutor(this);
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		// the default mode is to treat the first argument as a number or a
		// number surrounded by parenthesis
		// control flow is passed to handleAlternateFirstArgument the first
		// argument is not a number or
		// a number surrounded by parenthesis
		try {
			if (!(sender instanceof Player)) {
				return false;
			}

			Player player = (Player) sender;

			if (!player.isOp()) {
				player.sendMessage(ChatColor.RED + "Not authorized");
				return false;
			}

			double daysToProgress = 1.0;
			boolean isRelative = false;

			if (args != null && args.length > 0) {

				String firstArg = args[0];

				if (firstArg != null) {

					if (firstArg.startsWith("(") && firstArg.endsWith(")")) {
						isRelative = true;
						firstArg = firstArg.substring(1, firstArg.length() - 1);
					}

					try {
						daysToProgress = Double.parseDouble(firstArg);
					} catch (Exception e) {
						return handleAlternateFirstArgument(args, player, label);
					}

				}
			} else {
				return handleNoArgs(player, label);
			}

			if (Math.abs(daysToProgress) > 100000) {
				player.sendMessage(ChatColor.RED
						+ "The absolute value of the number of days must be less than 100,000 (because of reasons)");
				return false;
			}

			Double valueToProgress = daysToProgress * SpoilerAlertCalendar.TICKS_IN_A_DAY;

			if (isRelative) {

				if (player.getWorld().getFullTime() < SpoilerAlertCalendar.TICKS_IN_A_DAY) {
					if (valueToProgress < 0) {
						valueToProgress = player.getWorld().getFullTime() * -1.0;
					} else {
						valueToProgress = valueToProgress - player.getWorld().getFullTime();
					}
				} else {
					double remainder = player.getWorld().getFullTime() % SpoilerAlertCalendar.TICKS_IN_A_DAY;
					valueToProgress = valueToProgress - remainder;
				}
			}

			Long fullTime = player.getWorld().getFullTime() + Long.valueOf(valueToProgress.longValue());

			if (fullTime < 0) {
				fullTime = 0L;
			}

			player.getWorld().setFullTime(fullTime);

			player.sendMessage("/time set " + fullTime);

			return true;
		} catch (Exception e) {
			plugin.warning("error during " + label + " command");
			return false;
		}

	}

	private boolean handleNoArgs(Player player, String label) {
		return handleHelpArgument(player, label);
	}

	private boolean handleAlternateFirstArgument(String[] args, Player player, String label) {
		if (args == null || args.length < 1 || args[0] == null) {
			plugin.debugInfo("Null or empty args");
			return false;
		}

		if (args[0].equals("day")) {

			plugin.debugInfo("'day' 1st arg");
			return handleDayArgument(args, player);
		} else if (args[0].equals("return")) {

			plugin.debugInfo("'return' 1st arg");
			return handleReturnArgument(args, player);
		} else if (args[0].equals("help")) {

			plugin.debugInfo("'help' 1st arg");
			return handleHelpArgument(player, label);
		} else {

			player.sendMessage(ChatColor.RED + "argument not recognized.  See help:");
			handleHelpArgument(player, label);
			return false;
		}
	}

	private boolean validateSecondArg(String[] args, Player player) {
		if (args.length < 2 || args[1] == null) {
			plugin.debugInfo("no 2nd arg");
			return false;
		}

		return true;
	}

	private boolean handleHelpArgument(Player player, String label) {
		player.sendMessage(ChatColor.GREEN + "command examples: ");

		player.sendMessage("  - " + ChatColor.GREEN + "/" + label + " 3 " + ChatColor.WHITE
				+ "Moves world's fulltime forward exactly " + SpoilerAlertCalendar.TICKS_IN_A_DAY + "x3 units");
		player.sendMessage("  - " + ChatColor.GREEN + "/" + label + " -1 " + ChatColor.WHITE
				+ "Moves world's fulltime backwards exactly " + SpoilerAlertCalendar.TICKS_IN_A_DAY + "x1 units");
		player.sendMessage("  - " + ChatColor.GREEN + "/" + label + " (5) " + ChatColor.WHITE
				+ "Moves world's fulltime forward to the exact start of the day 5 from *now");
		player.sendMessage("  - " + ChatColor.GREEN + "/" + label + " (-2) " + ChatColor.WHITE
				+ "Moves world's fulltime backwards to the exact start of the day 2 *ago");
		player.sendMessage("  - " + ChatColor.GREEN + "/" + label + " day 1 " + ChatColor.WHITE
				+ "Moves to day 1 through 7. And snapshots your current time for use by the '" + label
				+ " return' command.");
		player.sendMessage("  - " + ChatColor.GREEN + "/" + label + " return " + ChatColor.WHITE
				+ "Moves the world's full time to where the user was before they ran the '" + label + " day' command.");

		return true;
	}

	private boolean handleDayArgument(String[] args, Player player) {

		if (!validateSecondArg(args, player)) {
			player.sendMessage(ChatColor.RED + "supply a number. it must be 1-7");
			return false;
		}

		try {
			int dayNumber = Integer.parseInt(args[1]);

			if (dayNumber < 1 || dayNumber > 7) {
				player.sendMessage(ChatColor.RED + "number must be 1-7");
				return false;
			} else {
				setReturnValueForPlayer(player);
				int newFulltime = (dayNumber - 1) * SpoilerAlertCalendar.TICKS_IN_A_DAY;
				player.sendMessage("/time set " + newFulltime);
				player.getWorld().setFullTime(newFulltime);
				return true;
			}

		} catch (NumberFormatException e) {
			player.sendMessage(ChatColor.RED + "error while parsing [" + args[1] + "] as an integer");
			plugin.debugInfo("issue parsing user input [" + args[1] + "] as integer: " + e.getMessage());
			return false;
		}
	}

	private boolean handleReturnArgument(String[] args, Player player) {
		Long returnValue = getReturnValueForPlayer(player);
		if (returnValue != null) {
			player.getWorld().setFullTime(returnValue);
			player.sendMessage("/time set " + returnValue);
			setReturnValueForPlayer(player, null);
			return true;
		}
		player.sendMessage("no return value is presently set");
		return false;
	}

	private void setReturnValueForPlayer(Player player, Long value) {
		if (!playerWorldReturnMap.containsKey(player.getName()) || playerWorldReturnMap.get(player.getName()) == null) {
			playerWorldReturnMap.put(player.getName(), new HashMap<String, Long>());
		}
		playerWorldReturnMap.get(player.getName()).put(player.getWorld().getName(), value);
	}

	private void setReturnValueForPlayer(Player player) {
		player.sendMessage(ChatColor.ITALIC + "#" + player.getWorld().getFullTime());
		player.getWorld().playSound(player.getLocation(), Sound.BLOCK_WOODEN_TRAPDOOR_CLOSE, 1, 1);
		setReturnValueForPlayer(player, player.getWorld().getFullTime());
	}

	private Long getReturnValueForPlayer(Player player) {
		if (!playerWorldReturnMap.containsKey(player.getName()) || playerWorldReturnMap.get(player.getName()) == null) {
			playerWorldReturnMap.put(player.getName(), new HashMap<String, Long>());
		}
		return playerWorldReturnMap.get(player.getName()).get(player.getWorld().getName());
	}

}
