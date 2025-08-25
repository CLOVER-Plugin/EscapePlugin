package yd.kingdom.escapePlugin.listener;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import yd.kingdom.escapePlugin.EscapePlugin;
import yd.kingdom.escapePlugin.item.GameItem;
import yd.kingdom.escapePlugin.item.ItemUtil;

public class EscapeItemListener implements Listener {
    @EventHandler
    public void onUse(PlayerInteractEvent e) {
        // 1) 메인 핸드 + 우클릭만 처리
        if (e.getHand() != EquipmentSlot.HAND) return;
        Action act = e.getAction();
        if (act != Action.RIGHT_CLICK_AIR && act != Action.RIGHT_CLICK_BLOCK) return;

        // 2) 탈출권인지 확인
        ItemStack item = e.getItem();
        if (!ItemUtil.isType(item, GameItem.ESCAPE)) return;

        e.setCancelled(true);
        
        // 탈출권 아이템 소모
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            e.getPlayer().getInventory().setItemInMainHand(null);
        }
        
        var player = e.getPlayer();

        // 3) 탈출 이펙트 및 권한 해제
        EscapePlugin.getInstance()
                .getAuctionService()
                .handleEscape(player);

        // 4) 로비로 워프 (Essentials)
        Location spawn = new Location(
                player.getWorld(),
                10, -46, 43
        );
        player.teleport(spawn);
        player.sendMessage("§a스폰으로 이동되었습니다.");

        EscapePlugin.getInstance().getTeleportService().clearLastTeleported();
    }
}