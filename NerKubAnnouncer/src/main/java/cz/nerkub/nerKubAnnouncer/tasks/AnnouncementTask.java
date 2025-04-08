package cz.nerkub.nerKubAnnouncer.tasks;

import cz.nerkub.nerKubAnnouncer.AnnouncementManager;
import cz.nerkub.nerKubAnnouncer.NerKubAnnouncer;
import cz.nerkub.nerKubAnnouncer.utils.MessageFormatter;
import cz.nerkub.nerKubAnnouncer.SoundData;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class AnnouncementTask extends BukkitRunnable {

	private final NerKubAnnouncer plugin;
	private final AnnouncementManager manager;

	public AnnouncementTask(NerKubAnnouncer plugin, AnnouncementManager manager) {
		this.plugin = plugin;
		this.manager = manager;
	}

	@Override
	public void run() {
		AnnouncementManager.Announcement announcement = manager.getNext();
		if (announcement == null) return;

		String type = announcement.getType();
		List<String> rawLines = announcement.getMessages();
		SoundData sound = announcement.getSound(); // ⬅️ Nově získáme i sound

		if (type.equalsIgnoreCase("actionbar")) {
			String combined = String.join(" ", rawLines);
			String formatted = MessageFormatter.format(combined);
			for (Player player : Bukkit.getOnlinePlayers()) {
				player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(formatted));
				playSoundIfPresent(player, sound); // 🔊
			}
		} else {
			for (Player player : Bukkit.getOnlinePlayers()) {
				int delay = 0;
				for (String line : rawLines) {
					List<BaseComponent[]> parts = MessageFormatter.parseToMultipleComponents(line);
					for (BaseComponent[] comp : parts) {
						final BaseComponent[] copy = comp;
						new BukkitRunnable() {
							@Override
							public void run() {
								player.spigot().sendMessage(copy);
							}
						}.runTaskLater(plugin, delay++);
					}
				}
				playSoundIfPresent(player, sound); // 🔊
			}
		}
	}

	private void playSoundIfPresent(Player player, SoundData sound) {
		if (sound == null) return;

		try {
			Sound bukkitSound = Sound.valueOf(sound.getName().toUpperCase());
			player.playSound(player.getLocation(), bukkitSound, sound.getVolume(), sound.getPitch());
		} catch (IllegalArgumentException ex) {
			Bukkit.getLogger().warning("[NerKubAnnouncer] Invalid sound name: " + sound.getName());
		}
	}
}
