package cz.nerkub.nerKubAnnouncer;

import cz.nerkub.nerKubAnnouncer.commands.AnnouncerCommand;
import cz.nerkub.nerKubAnnouncer.tasks.AnnouncementTask;
import cz.nerkub.nerKubAnnouncer.utils.ConfigUpdater;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class NerKubAnnouncer extends JavaPlugin {

	private static NerKubAnnouncer instance;
	private AnnouncementManager announcementManager;
	private AnnouncementTask currentTask;

	@Override
	public void onEnable() {
		instance = this;
		saveDefaultConfig();
		saveResource("announcements.yml", false);
		ConfigUpdater.update(new File(getDataFolder(), "config.yml"), getResource("config.yml"));
		ConfigUpdater.update(new File(getDataFolder(), "announcements.yml"), getResource("announcements.yml"));

		this.announcementManager = new AnnouncementManager(this);
		startAnnouncementTask(); // 🔄 správné spuštění tasku

		this.getCommand("announcer").setExecutor(new AnnouncerCommand());

		getLogger().info("NerKubAnnouncer byl úspěšně zapnut!");
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