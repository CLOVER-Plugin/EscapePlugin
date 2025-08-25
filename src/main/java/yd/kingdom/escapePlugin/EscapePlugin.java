package yd.kingdom.escapePlugin;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import yd.kingdom.escapePlugin.command.*;
import yd.kingdom.escapePlugin.command.JobRemoveCommand;
import yd.kingdom.escapePlugin.command.HeadCommand;
import yd.kingdom.escapePlugin.job.JobManager;
import yd.kingdom.escapePlugin.listener.*;
import yd.kingdom.escapePlugin.region.RegionManager;
import yd.kingdom.escapePlugin.service.AuctionService;
import yd.kingdom.escapePlugin.service.HiddenDropManager;
import yd.kingdom.escapePlugin.service.SkinPreloaderService;
import yd.kingdom.escapePlugin.service.TeleportService;

public final class EscapePlugin extends JavaPlugin {

    private static EscapePlugin instance;
    private JobManager jobManager;
    private RegionManager regionManager;
    private TeleportService teleportService;
    private AuctionService auctionService;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        HiddenDropManager.load();
        SkinPreloaderService.preloadAllSkins();

        jobManager = new JobManager();
        regionManager = new RegionManager();
        teleportService = new TeleportService();
        auctionService = new AuctionService();

        getCommand("관리자").setExecutor(new AdminCommand());
        getCommand("아이템").setExecutor(new ItemCommand());
        getCommand("직업").setExecutor(new JobCommand());
        getCommand("영역").setExecutor(new RegionCommand());
        getCommand("도우미").setExecutor(new HelperCommand());
        getCommand("도우미해제").setExecutor(new HelperRemoveCommand());
        getCommand("자유민").setExecutor(new FreemanCommand());
        getCommand("게임시작").setExecutor(new GameStartCommand());
        getCommand("직업해제").setExecutor(new JobRemoveCommand());
        getCommand("머리").setExecutor(new HeadCommand());

        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new EntryItemListener(), this);
        pm.registerEvents(new EscapeItemListener(), this);
        pm.registerEvents(new RegionInteractListener(), this);
        pm.registerEvents(new StealItemListener(), this);
        pm.registerEvents(new JobItemListener(), this);

        pm.registerEvents(new MelonBreakListener(), this);
        pm.registerEvents(new StoneBreakListener(), this);
        pm.registerEvents(new WoodBreakListener(), this);
        pm.registerEvents(new TurtleEggBreakListener(), this);
        pm.registerEvents(new FishingListener(), this);
        pm.registerEvents(new BonemealListener(), this);
        pm.registerEvents(new FlowerBreakListener(), this);

    }

    public static EscapePlugin getInstance() {
        return instance;
    }
    public JobManager getJobManager() {
        return jobManager;
    }
    public RegionManager getRegionManager() {
        return regionManager;
    }
    public TeleportService getTeleportService() {
        return teleportService;
    }
    public AuctionService getAuctionService() {
        return auctionService;
    }
}
