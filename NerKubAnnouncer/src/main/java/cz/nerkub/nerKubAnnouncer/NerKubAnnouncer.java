package cz.nerkub.nerKubAnnouncer;

import cz.nerkub.nerKubAnnouncer.commands.AnnouncerCommand;
import cz.nerkub.nerKubAnnouncer.tasks.AnnouncementTask;
import cz.nerkub.nerKubAnnouncer.utils.ConfigUpdater;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class NerKubAnnouncer extends JavaPlugin {

	private static NerKubAnnouncer instance;
	private AnnouncementManager announcementManager;
	private AnnouncementTask currentTask;
	private CheckUpdatesGitHub checkUpdatesGitHub;

	@Override
	public void onEnable() {
		instance = this;

		// Config handling
		saveDefaultConfig();
		handleFileUpdate("config.yml", Collections.emptyList());
		handleFileUpdate("announcements.yml", Collections.singletonList("announcements"));

		printStartupLogo();

		this.announcementManager = new AnnouncementManager(this);
		startAnnouncementTask();

		checkUpdatesGitHub = new CheckUpdatesGitHub(this);
		checkUpdatesGitHub.checkForUpdates();

		getServer().getPluginManager().registerEvents(new JoinListener(checkUpdatesGitHub), this);
		getCommand("announcer").setExecutor(new AnnouncerCommand());

		getLogger().info("NerKubAnnouncer has been enabled successfully!");
	}

	private void handleFileUpdate(String fileName, List<String> ignoredSections) {
		File file = new File(getDataFolder(), fileName);

		if (!file.exists()) {
			saveResource(fileName, false);
			getLogger().info("Created new " + fileName);
		} else {
			ConfigUpdater.update(file, getResource(fileName), ignoredSections);
		}
	}

	private void printStartupLogo() {
		String[] logo = {
				"",
				"&3|\\   |  | /    &aPlugin: &6NerKub Announcer",
				"&3| \\  |  |/     &aVersion: &bv" + getDescription().getVersion(),
				"&3|  \\ |  |\\    &aAuthor: &3NerKub Studio",
				"&3|   \\|  | \\   &aPremium: &bThis plugin is a not premium resource.",
				"",
				"&3| Visit our Discord for more! &ahttps://discord.gg/YXm26egK6g",
				""
		};

		for (String line : logo) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', line));
		}
	}


	@Override
	public void onDisable() {
		getLogger().info("NerKubAnnouncer se vypíná...");
	}

	public static NerKubAnnouncer getInstance() {
		return instance;
	}

	public void setAnnouncementManager(AnnouncementManager manager) {
		this.announcementManager = manager;
	}

	public AnnouncementManager getAnnouncementManager() {
		return announcementManager;
	}

	public void startAnnouncementTask() {
		if (currentTask != null) {
			currentTask.cancel();
		}
		int interval = getConfig().getInt("announcement-interval", 60);
		getLogger().info("[DEBUG] Spouštím task s intervalem " + interval + "s");

		currentTask = new AnnouncementTask(this, announcementManager);
		currentTask.runTaskTimer(this, interval * 20L, interval * 20L);
	}



}