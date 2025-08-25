package yd.kingdom.escapePlugin.command;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import yd.kingdom.escapePlugin.item.GameItem;
import yd.kingdom.escapePlugin.item.ItemUtil;
import yd.kingdom.escapePlugin.job.JobType;

import java.util.List;
import java.util.Map;

public class ItemCommand implements CommandExecutor {
    // 직업별 양털 색상 매핑
    private static final Map<JobType, Material> JOB_WOOL = Map.of(
            JobType.WATERMELON, Material.PINK_WOOL,
            JobType.FLOWER,     Material.RED_WOOL,
            JobType.TURTLE,     Material.WHITE_WOOL,
            JobType.FISHING,    Material.BLUE_WOOL,
            JobType.WOOD,       Material.BROWN_WOOL,
            JobType.MINERAL,    Material.LIGHT_GRAY_WOOL
    );

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
        if (!s.hasPermission("missiongame.admin")) return false;
        if (args.length < 2) {
            s.sendMessage("§c사용법: /아이템 <입장권|탈출권|강탈권|직업명> <플레이어>");
            return false;
        }

        String key = args[0];
        Player tgt = Bukkit.getPlayer(args[1]);
        if (tgt == null) {
            s.sendMessage("§c플레이어를 찾을 수 없습니다.");
            return true;
        }

        // 1) 특수권 아이템 (입장/탈출/강탈)
        GameItem g = GameItem.fromString(key);
        if (g != null) {
            tgt.getInventory().addItem(ItemUtil.create(g));
            s.sendMessage("§a" + tgt.getName() + "님에게 " + g.getDisplay() + "을(를) 지급했습니다.");
            return true;
        }

        // 2) 직업 토큰 (양털)
        JobType job = JobType.fromDisplayName(key);
        if (job != null && JOB_WOOL.containsKey(job)) {
            Material wool = JOB_WOOL.get(job);
            ItemStack it = new ItemStack(wool, 1);
            ItemMeta meta = it.getItemMeta();
            meta.setDisplayName("§6[" + job.getDisplayName() + " 권한 토큰]");
            meta.setLore(List.of(
                    "§7우클릭 시 '" + job.getDisplayName() + "' 직업 권한을 획득합니다.",
                    "§7(해당 아이템은 사용 시 사라집니다.)"
            ));
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            it.setItemMeta(meta);

            tgt.getInventory().addItem(it);
            s.sendMessage("§a" + tgt.getName() + "님에게 " + job.getDisplayName() + " 토큰을 지급했습니다.");
            return true;
        }

        s.sendMessage("§c유효한 아이템 또는 직업명이 아닙니다.");
        return true;
    }
}