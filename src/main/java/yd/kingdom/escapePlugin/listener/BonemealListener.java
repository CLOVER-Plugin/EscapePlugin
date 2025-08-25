package yd.kingdom.escapePlugin.listener;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import yd.kingdom.escapePlugin.EscapePlugin;
import yd.kingdom.escapePlugin.job.JobType;
import yd.kingdom.escapePlugin.service.HiddenDropManager;

public class BonemealListener implements Listener {
    @EventHandler
    public void onBoneMeal(PlayerInteractEvent e) {
        // 1) 메인 핸드 + 우클릭만
        if (e.getHand() != EquipmentSlot.HAND) return;
        Action act = e.getAction();
        if (act != Action.RIGHT_CLICK_BLOCK && act != Action.RIGHT_CLICK_AIR) return;

        // 2) 뼛가루 확인
        ItemStack in = e.getItem();
        if (in == null || in.getType() != Material.BONE_MEAL) return;

        // 3) 클릭 블록이 잔디블록이어야
        Block b = e.getClickedBlock();
        if (b == null || b.getType() != Material.GRASS_BLOCK) return;

        // 4) FLOWER 직업이거나 FREEMAN일 때만
        JobType job = EscapePlugin.getInstance()
                .getJobManager()
                .getJob(e.getPlayer().getUniqueId());
        if (job != JobType.FLOWER && job != JobType.FREEMAN) return;

        // 5) 기본 뼛가루 효과 실행
        e.setCancelled(true);
        b.applyBoneMeal(e.getBlockFace());

        // 6) 아이템 소모 (applyBoneMeal이 true면 소모됨, false면 소모 안 될 수 있으니 수동 감소)
        in.setAmount(in.getAmount() - 1);

        // 7) 1틱 뒤 반경 2칸 내 모든 꽃을 POPPY로 교체
        Bukkit.getScheduler().runTask(EscapePlugin.getInstance(), () -> {
            int radius = 5;
            int y = b.getY();
            var world = b.getWorld();
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    Block ground = world.getBlockAt(b.getX() + dx, y, b.getZ() + dz);
                    Block above  = ground.getRelative(BlockFace.UP);
                    if (Tag.FLOWERS.isTagged(above.getType())) {
                        above.setType(Material.POPPY);
                    }
                }
            }
        });
    }
}