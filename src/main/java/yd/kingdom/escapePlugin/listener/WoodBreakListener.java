package yd.kingdom.escapePlugin.listener;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import yd.kingdom.escapePlugin.EscapePlugin;
import yd.kingdom.escapePlugin.job.JobType;
import yd.kingdom.escapePlugin.service.HiddenDropManager;

public class WoodBreakListener implements Listener {
    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        Block b = e.getBlock();
        // 로그 계열이면
        if (!b.getType().toString().endsWith("_LOG")) return;
        JobType job = EscapePlugin.getInstance().getJobManager().getJob(e.getPlayer().getUniqueId());
        if (job == JobType.WOOD || job == JobType.FREEMAN) {
            HiddenDropManager.attempt(JobType.WOOD, b.getLocation());
        }
    }
}