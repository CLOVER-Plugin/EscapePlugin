package yd.kingdom.escapePlugin.item;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemUtil {
    public static ItemStack create(GameItem type) {
        ItemStack it = new ItemStack(Material.PAPER);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + type.getDisplay());

        List<String> lore = new ArrayList<>();
        switch (type) {
            case ENTRY -> {
                lore.add(ChatColor.WHITE + "우클릭 시 보물찾기맵 랜덤 지정 위치로 텔레포트 됩니다.");
                lore.add(ChatColor.RED + "다른 플레이어가 사용하면 이전 위치로 자동 복귀됩니다.");
            }
            case ESCAPE -> {
                lore.add(ChatColor.WHITE + "우클릭 시 해당 플레이어를 탈출시킵니다.");
                lore.add(ChatColor.WHITE + "탈출 후 도우미로서 타 플레이어를 도와주게 됩니다.");
                lore.add(ChatColor.RED + "사용 후 플레이어는 로비에서 경매 대상으로 취급됩니다.");
            }
            case STEAL -> {
                lore.add(ChatColor.WHITE + "우클릭 시 직업을 강탈할 플레이어의 닉네임을 채팅창에 적습니다.");
                lore.add(ChatColor.WHITE + "닉네임 입력 후 직업이 서로 스왑됩니다.");
                lore.add(ChatColor.RED + "다이아몬드와 입장권만 인벤토리에 보관되고");
                lore.add(ChatColor.RED + "나머지 직업 관련 아이템들은 이전 직업의 상자로 회수됩니다.");
                lore.add(ChatColor.YELLOW + "※ 해당 영역에 소속된 모든 플레이어(노동자+도우미)의");
                lore.add(ChatColor.YELLOW + "   아이템이 이전 직업의 상자로 이동됩니다.");
                lore.add(ChatColor.BLUE + "※ 도우미들도 함께 직업이 변경됩니다.");
            }
        }
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        it.setItemMeta(meta);
        return it;
    }

    public static boolean isType(ItemStack it, GameItem type) {
        if (it == null || !it.hasItemMeta()) return false;
        return it.getItemMeta().getDisplayName().contains(type.getDisplay());
    }
}