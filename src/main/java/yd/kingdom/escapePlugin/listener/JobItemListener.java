package yd.kingdom.escapePlugin.listener;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import yd.kingdom.escapePlugin.EscapePlugin;
import yd.kingdom.escapePlugin.job.JobManager;
import yd.kingdom.escapePlugin.job.JobType;

public class JobItemListener implements Listener {

    @EventHandler
    public void onUse(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        ItemStack it = e.getItem();
        if (it == null || !it.hasItemMeta() || !it.getItemMeta().hasDisplayName()) return;

        String name = it.getItemMeta().getDisplayName();
        if (!name.endsWith(" 권한 토큰]")) return;

        // "[농부 권한 토큰]" → "농부"
        String display = name.substring(name.indexOf('[')+1, name.indexOf(" 권한 토큰]"));
        JobType job = JobType.fromDisplayName(display);
        if (job == null) return;

        // 1) 직업 권한 설정
        EscapePlugin.getInstance().getJobManager()
                .setJob(e.getPlayer().getUniqueId(), job);

        // 2) 아이템 소모: 직접 수량 감소
        if (it.getAmount() > 1) {
            it.setAmount(it.getAmount() - 1);
        } else {
            e.getPlayer().getInventory().remove(it);
        }

        e.setCancelled(true);  // 기본 동작 방지
        e.getPlayer().sendMessage("§a" + job.getDisplayName() + " 직업 및 영역 권한을 획득했습니다!");
        e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
    }
}