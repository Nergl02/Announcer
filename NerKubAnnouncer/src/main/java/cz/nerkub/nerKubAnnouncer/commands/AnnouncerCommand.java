package cz.nerkub.nerKubAnnouncer.commands;

import cz.nerkub.nerKubAnnouncer.AnnouncementManager;
import cz.nerkub.nerKubAnnouncer.NerKubAnnouncer;
import cz.nerkub.nerKubAnnouncer.utils.MessageFormatter;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class AnnouncerCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.hasPermission("announcer.admin")) {
			sender.sendMessage("§cNemáš oprávnění!");
			return true;
		}

		NerKubAnnouncer plugin = NerKubAnnouncer.getInstance();

		// /announcer reload
		if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
			plugin.reloadConfig();
			plugin.setAnnouncementManager(new AnnouncementManager(plugin));
			plugin.startAnnouncementTask();
			sender.sendMessage("§aPlugin byl úspěšně reloadnut.");
			return true;
		}

		// /announcer list
		if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
			sender.sendMessage("§7Dostupné zprávy:");
			for (String key : plugin.getAnnouncementManager().getKeys()) {
				sender.sendMessage(" §8- §f" + key);
			}
			return true;
		}

		// /announcer send <key>
		if (args.length >= 2 && args[0].equalsIgnoreCase("send")) {
			String key = args[1];
			AnnouncementManager.Announcement a = plugin.getAnnouncementManager().getByKey(key);

			if (a == null) {
				sender.sendMessage("§cZpráva s názvem §f" + key + " §cneexistuje.");
				return true;
			}

			String type = a.getType();
			List<String> rawLines = a.getMessages();

			if (args.length == 3) {
				Player target = Bukkit.getPlayerExact(args[2]);
				if (target == null) {
					sender.sendMessage("§cHráč §f" + args[2] + " §cnenalezen.");
					return true;
				}
				sendTo(target, type, rawLines);
				sender.sendMessage("§aZpráva §f" + key + " §abyla odeslána hráči §f" + target.getName() + "§a.");
			} else {
				for (Player p : Bukkit.getOnlinePlayers()) {
					sendTo(p, type, rawLines);
				}
				sender.sendMessage("§aZpráva §f" + key + " §abyla odeslána všem hráčům.");
			}
			return true;
		}


		// nápověda
		sender.sendMessage("§7Použití:");
		sender.sendMessage(" §8/announcer reload §7- načte nové zprávy");
		sender.sendMessage(" §8/announcer list §7- vypíše názvy zpráv");
		sender.sendMessage(" §8/announcer send <jméno> §7- odešle vybranou zprávu");
		return true;
	}

	private void sendTo(Player player, String type, List<String> rawLines) {
		if (type.equalsIgnoreCase("actionbar")) {
			String combined = String.join(" ", rawLines); // akce je krátká, spojujeme
			String formatted = MessageFormatter.format(combined);
			player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(formatted));
		} else {
			int delay = 0;
			for (String line : rawLines) {
				String processed = line;
				List<BaseComponent[]> componentsList = MessageFormatter.parseToMultipleComponents(processed);
				for (BaseComponent[] comp : componentsList) {
					new org.bukkit.scheduler.BukkitRunnable() {
						@Override
						public void run() {
							player.spigot().sendMessage(comp);
						}
					}.runTaskLater(NerKubAnnouncer.getInstance(), delay++);
				}
			}
		}
	}


}
