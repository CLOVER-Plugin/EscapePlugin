package yd.kingdom.escapePlugin.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import yd.kingdom.escapePlugin.EscapePlugin;
import yd.kingdom.escapePlugin.item.GameItem;
import yd.kingdom.escapePlugin.item.ItemUtil;

public class EntryItemListener implements Listener {
    @EventHandler
    public void onUse(PlayerInteractEvent e) {
        // 1) 메인 핸드만
        if (e.getHand() != EquipmentSlot.HAND) return;

        // 2) 우클릭 블록 또는 우클릭 공중만 처리
        Action act = e.getAction();
        if (act != Action.RIGHT_CLICK_BLOCK && act != Action.RIGHT_CLICK_AIR) return;

        // 3) 입장권 아이템인지 확인
        ItemStack item = e.getItem();
        if (!ItemUtil.isType(item, GameItem.ENTRY)) return;

        // 4) 사용 취소 & 텔레포트
        e.setCancelled(true);
        
        // 입장권 아이템 소모
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            e.getPlayer().getInventory().setItemInMainHand(null);
        }
        
        EscapePlugin.getInstance().getTeleportService().handleEntry(e.getPlayer());
    }
}