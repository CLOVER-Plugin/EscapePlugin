package yd.kingdom.escapePlugin.listener;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import yd.kingdom.escapePlugin.EscapePlugin;
import yd.kingdom.escapePlugin.job.JobType;
import yd.kingdom.escapePlugin.service.HiddenDropManager;

public class TurtleEggBreakListener implements Listener {
    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if (e.getBlock().getType() != Material.TURTLE_EGG) return;
        JobType job = EscapePlugin.getInstance().getJobManager().getJob(e.getPlayer().getUniqueId());
        if (job == JobType.TURTLE || job == JobType.FREEMAN) {
            HiddenDropManager.attempt(job, e.getBlock().getLocation());
        }
    }
}