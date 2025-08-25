package yd.kingdom.escapePlugin.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import yd.kingdom.escapePlugin.EscapePlugin;
import yd.kingdom.escapePlugin.job.JobType;
import yd.kingdom.escapePlugin.service.HiddenDropManager;

public class FlowerBreakListener implements Listener {
    // 플레이어가 직접 부쉈을 때
    @EventHandler
    public void onFlowerBreak(BlockBreakEvent e) {
        if (e.getBlock().getType() != Material.POPPY) return;

        JobType job = EscapePlugin.getInstance()
                .getJobManager()
                .getJob(e.getPlayer().getUniqueId());
        if (job != JobType.FLOWER && job != JobType.FREEMAN) return;

        HiddenDropManager.attempt(job, e.getBlock().getLocation());
    }

    // 물이 흐르면서 꽃을 부술 때
    @EventHandler
    public void onWaterFlowBreak(BlockFromToEvent e) {
        // 1) 물 블록이 흐르는 이벤트만
        if (e.getBlock().getType() != Material.WATER) return;

        // 2) 물이 흘러들어가는 위치에 꽃(POPPI)이 있는지
        Block to = e.getToBlock();
        if (to.getType() != Material.POPPY) return;

        // 3) FLOWER 직업으로 간주하고 히든 드랍 시도
        HiddenDropManager.attempt(JobType.FLOWER, to.getLocation());
    }
}