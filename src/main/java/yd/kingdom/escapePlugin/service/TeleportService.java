package yd.kingdom.escapePlugin.service;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import yd.kingdom.escapePlugin.EscapePlugin;
import yd.kingdom.escapePlugin.item.GameItem;
import yd.kingdom.escapePlugin.item.ItemUtil;

import java.util.*;

public class TeleportService {
    private final List<Location> points = new ArrayList<>();
    private UUID lastTeleported = null;

    // 스폰 위치 상수
    private final Location spawnLocation;

    public TeleportService() {
        JavaPlugin plugin = EscapePlugin.getInstance();
        // teleportPoints 로딩
        for (Map<?,?> m : plugin.getConfig().getMapList("teleportPoints")) {
            String world = (String) m.get("world");
            double x = ((Number) m.get("x")).doubleValue();
            double y = ((Number) m.get("y")).doubleValue();
            double z = ((Number) m.get("z")).doubleValue();
            points.add(new Location(plugin.getServer().getWorld(world), x, y, z));
        }
        // 스폰 위치 설정 (world 이름은 config의 teleportPoints와 같은 world 사용)
        this.spawnLocation = new Location(
                plugin.getServer().getWorld("world"),
                10, -46, 43
        );
    }

    public void handleEntry(Player p) {
        UUID id = p.getUniqueId();

        // 1) 이전 텔포 유저가 있으면 스폰으로 복귀
        if (lastTeleported != null && !lastTeleported.equals(id)) {
            Player prev = Bukkit.getPlayer(lastTeleported);
            if (prev != null && prev.isOnline()) {
                prev.teleport(spawnLocation);
                prev.sendMessage("§c스폰으로 자동 복귀되었습니다.");
            }
        }

        // 2) 현재 유저만 랜덤 위치로 텔포
        Location dest = points.get(new java.util.Random().nextInt(points.size()));
        p.teleport(dest);
        p.sendMessage("§a입장권 사용! 지정 위치로 텔포 되었습니다.");

        // 3) 상태 저장
        lastTeleported = id;

        // 4) 아이템 소모는 EntryItemListener에서 처리됨
    }

    public void clearLastTeleported() {
        this.lastTeleported = null;
    }
}