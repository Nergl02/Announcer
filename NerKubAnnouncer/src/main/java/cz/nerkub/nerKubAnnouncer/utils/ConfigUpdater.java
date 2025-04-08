package cz.nerkub.nerKubAnnouncer.utils;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;

public class ConfigUpdater {

	public static void update(File file, InputStream defaultStream) {
		if (defaultStream == null) return;

		FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
		FileConfiguration userConfig = YamlConfiguration.loadConfiguration(file);

		boolean updated = mergeSections(userConfig, defaultConfig);

		if (updated) {
			try {
				userConfig.save(file);
				System.out.println("[NerKubAnnouncer] Updated config: " + file.getName());
			} catch (Exception e) {
				System.err.println("[NerKubAnnouncer] Failed to save updated config: " + file.getName());
				e.printStackTrace();
			}
		}
	}

	private static boolean mergeSections(ConfigurationSection user, ConfigurationSection defaults) {
		boolean changed = false;
		Set<String> keys = defaults.getKeys(true);

		for (String key : keys) {
			if (!user.contains(key)) {
				user.set(key, defaults.get(key));
				changed = true;
			}
		}
		return changed;
	}
}
