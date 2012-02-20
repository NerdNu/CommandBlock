package nu.nerd.commandblock;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandBlock extends JavaPlugin implements Listener {

	HashMap<String, String> blockedCommands = new HashMap<String, String>();

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		try {
			loadBlockedCommands();
		} catch (IOException e) {
			getLogger().log(Level.INFO, "Failed to load blocked commands");
		}
	}

	@EventHandler (priority = EventPriority.LOWEST)
	public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
		String command;
		if (event.getMessage().contains(" "))
			command = event.getMessage().split(" ")[0];
		else
			command = event.getMessage();
		if (!command.startsWith("/"))
			return;

		command = command.substring(1);
		Iterator<String> localIterator = blockedCommands.keySet().iterator();
		while (localIterator.hasNext()) {
			String description = (String) localIterator.next();
			String regexStr = (String) blockedCommands.get(description);

			if (command.matches(regexStr)) {
				event.getPlayer().sendMessage(
						ChatColor.RED + "That command is disabled.");
				event.setCancelled(true);
				break;
			}
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {

		if (sender instanceof Player) {
			
			if (!sender.hasPermission("commandblock.reload")) {
				return false;
			}
			
		} else if (!sender.isOp()) {
			return false;
		}
		
		try {
			CommandBlock.this.loadBlockedCommands();
		} catch (IOException localIOException) {
			getLogger().log(Level.INFO, "Failed to load blocked commands.");
		}
		sender.sendMessage(ChatColor.GREEN + "Reloaded Banned Commands.");
		return true;
	}

	public void loadBlockedCommands() throws IOException {
		getLogger().log(Level.INFO, "Loading commands.");

		HashMap<String, String> cb = new HashMap<String, String>();
		if (!getDataFolder().exists()) {
			getDataFolder().mkdirs();
		}

		String configPath = getDataFolder().getAbsolutePath() + "/settings.txt";

		File configFile = new File(configPath);
		if (!configFile.exists()) {
			configFile.createNewFile();
		}

		BufferedReader reader = new BufferedReader(new FileReader(configPath));

		String line = "";

		while ((line = reader.readLine()) != null) {
			if (line.trim().length() == 0) 
				continue;
			if (line.trim().startsWith("#"))
				continue;

			String[] arrayOfString = line.split("::");
			if (arrayOfString.length != 2)
				getLogger().log(Level.INFO, "Invalid Command String");

			String description = "";
			String commandStr = "";
			commandStr = arrayOfString[0];
			description = arrayOfString[1];
			cb.put(description, commandStr);
		}
		reader.close();
		this.blockedCommands.clear();
		this.blockedCommands.putAll(cb);
		cb = null;
	}
}
