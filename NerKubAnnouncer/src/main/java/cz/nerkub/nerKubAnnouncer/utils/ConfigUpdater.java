package cz.nerkub.nerKubAnnouncer.utils;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ConfigUpdater {

	/**
	 * Update missing keys from default config to user config.
	 * Will NOT overwrite existing values.
	 *
	 * @param file target file to update
	 * @param defaultStream internal resource stream
	 */
	public static void update(File file, InputStream defaultStream) {
		update(file, defaultStream, Collections.emptyList());
	}

	/**
	 * Same as update() but allows ignoring some sections (like "announcements")
	 *
	 * @param file file to update
	 * @param defaultStream resource file
	 * @param ignoredSections list of top-level keys to ignore
	 */
	public static void update(File file, InputStream defaultStream, List<String> ignoredSections) {
		if (defaultStream == null) return;

		FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
		FileConfiguration userConfig = YamlConfiguration.loadConfiguration(file);

		boolean updated = mergeSections(userConfig, defaultConfig, ignoredSections);

		if (updated) {
			try {
				userConfig.save(file);
				System.out.println("[NerKubAnnouncer] Auto-updated config: " + file.getName());
			} catch (Exception e) {
				System.err.println("[NerKubAnnouncer] Failed to save updated config: " + file.getName());
				e.printStackTrace();
			}
		}
	}

	private static boolean mergeSections(ConfigurationSection user, ConfigurationSection defaults, List<String> ignoredSections) {
		boolean changed = false;

		Set<String> keys = defaults.getKeys(true);

		for (String key : keys) {
			// Skip ignored sections
			if (ignoredSections.stream().anyMatch(key::startsWith)) continue;

			if (!user.contains(key)) {
				user.set(key, defaults.get(key));
				changed = true;
			}
		}
		return changed;
	}
}
