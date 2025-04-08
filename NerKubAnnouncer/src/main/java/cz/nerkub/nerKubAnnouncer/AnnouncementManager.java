package cz.nerkub.nerKubAnnouncer;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class AnnouncementManager {

	private final Map<String, Announcement> announcements = new LinkedHashMap<>();
	private final List<String> keys = new ArrayList<>();
	private int index = 0;
	private final boolean randomOrder;

	public AnnouncementManager(NerKubAnnouncer plugin) {
		File file = new File(plugin.getDataFolder(), "announcements.yml");
		YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
		this.randomOrder = plugin.getConfig().getBoolean("random-order", false);

		ConfigurationSection section = config.getConfigurationSection("announcements");
		if (section != null) {
			for (String key : section.getKeys(false)) {
				ConfigurationSection msgSec = section.getConfigurationSection(key);
				if (msgSec == null) continue;

				String type = msgSec.getString("type", "chat");
				List<String> messages = msgSec.getStringList("messages");

				// 🔊 Načtení zvuku (pokud je definován)
				SoundData sound = null;
				ConfigurationSection soundSec = msgSec.getConfigurationSection("sound");
				if (soundSec != null) {
					String name = soundSec.getString("name", "");
					float volume = (float) soundSec.getDouble("volume", 1.0);
					float pitch = (float) soundSec.getDouble("pitch", 1.0);
					sound = new SoundData(name, volume, pitch);
				}

				announcements.put(key, new Announcement(type, messages, sound));
				keys.add(key);
			}
		}
	}

	public Announcement getNext() {
		if (announcements.isEmpty()) {
			Bukkit.getLogger().warning("[NerKubAnnouncer] Žádné načtené zprávy!");
			return null;
		}

		if (randomOrder) {
			String randomKey = keys.get(new Random().nextInt(keys.size()));
			Bukkit.getLogger().info("[NerKubAnnouncer] Náhodně vybrána zpráva: " + randomKey);
			return announcements.get(randomKey);
		}

		if (index >= keys.size()) index = 0;
		String nextKey = keys.get(index++);
		Bukkit.getLogger().info("[NerKubAnnouncer] Další zpráva: " + nextKey);
		return announcements.get(nextKey);
	}

	public static class Announcement {
		private final String type;
		private final List<String> messages;
		private final SoundData sound; // 🔊 nový atribut

		public Announcement(String type, List<String> messages, SoundData sound) {
			this.type = type;
			this.messages = messages;
			this.sound = sound;
		}

		public String getType() {
			return type;
		}

		public List<String> getMessages() {
			return messages;
		}

		public SoundData getSound() {
			return sound;
		}
	}

	public Set<String> getKeys() {
		return announcements.keySet();
	}

	public Announcement getByKey(String key) {
		return announcements.get(key);
	}
}
