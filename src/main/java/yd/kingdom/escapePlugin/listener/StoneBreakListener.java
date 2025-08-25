package yd.kingdom.escapePlugin.listener;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import yd.kingdom.escapePlugin.EscapePlugin;
import yd.kingdom.escapePlugin.job.JobType;
import yd.kingdom.escapePlugin.service.HiddenDropManager;

import java.util.Collection;

public class StoneBreakListener implements Listener {

    /**
     * 광부(MINERAL) 직업인 플레이어가 돌(STONE) 또는 에메랄드 원석(EMERALD_ORE)을 캘 때
     * Fortune 인챈트 적용 후 드랍 개수에서 1개를 뺀 만큼만 드랍하고,
     * 그 뒤에 HiddenDropManager 를 호출합니다.
     */
    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        Material type = e.getBlock().getType();
        if (type != Material.STONE && type != Material.EMERALD_ORE) return;

        JobType job = EscapePlugin.getInstance()
                .getJobManager()
                .getJob(e.getPlayer().getUniqueId());
        if (job != JobType.MINERAL && job != JobType.FREEMAN) return;

        // 1) 기본 드랍 억제
        e.setDropItems(false);

        // 2) Fortune 적용된 드랍 계산
        ItemStack tool = e.getPlayer().getInventory().getItemInMainHand();
        Collection<ItemStack> drops = e.getBlock().getDrops(tool);

        Location dropLoc = e.getBlock().getLocation().add(0.5, 0.5, 0.5);
        World world = e.getBlock().getWorld();

        for (ItemStack drop : drops) {
            int originalAmount = drop.getAmount();
            int toDrop = originalAmount - 1;  // 1개를 차감
            if (toDrop > 0) {
                ItemStack newDrop = new ItemStack(drop.getType(), toDrop);
                // 자연스럽게 떨어뜨리기
                world.dropItemNaturally(dropLoc, newDrop);
            }
        }

        // 3) 히든 드랍 시도
        HiddenDropManager.attempt(job, e.getBlock().getLocation());
    }
}