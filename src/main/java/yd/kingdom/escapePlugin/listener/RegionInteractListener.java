package yd.kingdom.escapePlugin.listener;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import yd.kingdom.escapePlugin.EscapePlugin;
import yd.kingdom.escapePlugin.job.JobManager;
import yd.kingdom.escapePlugin.job.JobType;
import yd.kingdom.escapePlugin.region.RegionManager;

import java.util.UUID;

public class RegionInteractListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        checkAndCancel(e.getBlock().getLocation(), e.getPlayer().getUniqueId(),
                () -> e.setCancelled(true));
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        checkAndCancel(e.getBlock().getLocation(), e.getPlayer().getUniqueId(),
                () -> e.setCancelled(true));
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getClickedBlock() != null) {
            checkAndCancel(e.getClickedBlock().getLocation(), e.getPlayer().getUniqueId(),
                    () -> e.setCancelled(true));
        }
    }

    private void checkAndCancel(Location loc, UUID uuid, Runnable cancel) {
        RegionManager rm = EscapePlugin.getInstance().getRegionManager();
        JobManager   jm = EscapePlugin.getInstance().getJobManager();
        JobType       my = jm.getJob(uuid);

        rm.getByLocation(loc).ifPresent(region -> {
            JobType regionJob = region.getJob();
            Player player = Bukkit.getPlayer(uuid);

            // 디버깅: 권한 체크 과정 출력
            //player.sendMessage("§e디버깅: 현재 직업 = " + my + ", 영역 직업 = " + regionJob);
            //player.sendMessage("§e디버깅: FREEMAN 여부 = " + (my == JobType.FREEMAN));
            //player.sendMessage("§e디버깅: 도우미 권한 여부 = " + jm.isHelper(uuid, regionJob));
            //player.sendMessage("§e디버깅: 자신 직업 영역 여부 = " + (my == regionJob));

            // 1) Helper 권한이 있는 직업 영역이면 통과 (FREEMAN도 도우미 권한 가능)
            if (jm.isHelper(uuid, regionJob)) return;

            // 2) 자신의 직업 영역이면 통과
            if (my == regionJob) return;

            // 3) FREEMAN이면 통과 (도우미 권한이 없는 경우에만)
            if (my == JobType.FREEMAN) return;

            // 그 외 모두 차단
            cancel.run();
            player.sendMessage("§c해당 영역에 대한 권한이 없습니다!");
        });
    }
}