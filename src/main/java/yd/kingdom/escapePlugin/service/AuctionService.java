package yd.kingdom.escapePlugin.service;

import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import yd.kingdom.escapePlugin.EscapePlugin;
import yd.kingdom.escapePlugin.item.GameItem;
import yd.kingdom.escapePlugin.item.ItemUtil;
import yd.kingdom.escapePlugin.job.JobType;

public class AuctionService {
    // 고정 좌표 (x=25, y=-46, z=43)에서 위로 쏘아올릴 위치
    private static final Location FIREWORK_LOC = new Location(
            EscapePlugin.getInstance().getServer().getWorld("world"),
            25, -46, 43
    );

    public void handleEscape(Player p) {
        World world = FIREWORK_LOC.getWorld();

        // 1) 폭죽 연출: 지정 좌표에서 5발, 약간의 지연을 두고 발사
        for (int i = 0; i < 5; i++) {
            final int delay = i * 10;
            Bukkit.getScheduler().runTaskLater(EscapePlugin.getInstance(), () -> {
                Firework fw = (Firework) world.spawnEntity(FIREWORK_LOC, EntityType.FIREWORK_ROCKET);
                FireworkMeta meta = fw.getFireworkMeta();
                meta.addEffect(FireworkEffect.builder()
                        .withColor(Color.GREEN)
                        .with(FireworkEffect.Type.BALL_LARGE)
                        .build()
                );
                meta.setPower(1);  // 1~3: 비행력(높이)
                fw.setFireworkMeta(meta);
            }, delay);
        }

        // 2) 축하 메시지
        String msg = "§6축하합니다! " + p.getName() + "님이 탈출에 성공했습니다!";
        Bukkit.broadcastMessage(msg);

        // 3) 모든 플레이어에게 네더라이트 블록 깨는 소리 재생
        for (Player pl : Bukkit.getOnlinePlayers()) {
            pl.playSound(pl.getLocation(),
                    Sound.UI_TOAST_CHALLENGE_COMPLETE,
                    1.0f, 1.0f);
        }

        // 4) 권한(FREEMAN) 설정
        EscapePlugin.getInstance()
                .getJobManager()
                .setJob(p.getUniqueId(), JobType.FREEMAN);

        // 5) 1분 후 경매 알림
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.broadcastMessage("§e경매가 1분 후 시작됩니다!");
            }
        }.runTaskLater(EscapePlugin.getInstance(), 20L * 60);

        // 6) 아이템 소모는 EscapeItemListener에서 처리됨
    }
}