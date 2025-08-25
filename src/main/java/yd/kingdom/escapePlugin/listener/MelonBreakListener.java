package yd.kingdom.escapePlugin.listener;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import yd.kingdom.escapePlugin.EscapePlugin;
import yd.kingdom.escapePlugin.job.JobType;
import yd.kingdom.escapePlugin.service.HiddenDropManager;

public class MelonBreakListener implements Listener {
    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if (e.getBlock().getType() != Material.MELON) return;
        JobType job = EscapePlugin.getInstance()
                .getJobManager()
                .getJob(e.getPlayer().getUniqueId());
        if (job == JobType.WATERMELON || job == JobType.FREEMAN) {
            HiddenDropManager.attempt(JobType.WATERMELON, e.getBlock().getLocation());
        }
    }
}