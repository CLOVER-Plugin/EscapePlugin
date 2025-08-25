package yd.kingdom.escapePlugin.listener;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitTask;
import yd.kingdom.escapePlugin.EscapePlugin;
import yd.kingdom.escapePlugin.item.GameItem;
import yd.kingdom.escapePlugin.item.ItemUtil;
import yd.kingdom.escapePlugin.job.JobManager;
import yd.kingdom.escapePlugin.job.JobType;
import yd.kingdom.escapePlugin.region.Region;
import yd.kingdom.escapePlugin.region.RegionManager;

import java.util.Optional;
import java.util.UUID;

public class StealItemListener implements Listener {
    private UUID acting = null;
    private ItemStack consumedItem = null;  // 소모된 아이템을 임시 보관
    private BukkitTask timeoutTask = null;  // 시간 초과 태스크

    @EventHandler
    public void onUse(PlayerInteractEvent e) {
        // 메인 핸드 + 우클릭만 처리
        if (e.getHand() != EquipmentSlot.HAND) return;
        Action act = e.getAction();
        if (act != Action.RIGHT_CLICK_AIR && act != Action.RIGHT_CLICK_BLOCK) return;

        // 강탈권인지 확인
        ItemStack item = e.getItem();
        if (!ItemUtil.isType(item, GameItem.STEAL)) return;

        e.setCancelled(true);
        
        // 강탈권 아이템 임시 소모 (성공 시에만 영구 소모)
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
            consumedItem = ItemUtil.create(GameItem.STEAL);  // 1개 아이템 생성하여 보관
        } else {
            e.getPlayer().getInventory().setItemInMainHand(null);
            consumedItem = item.clone();  // 원본 아이템 복사하여 보관
        }
        
        acting = e.getPlayer().getUniqueId();
        e.getPlayer().sendMessage("§e강탈할 플레이어 닉네임을 입력해주세요:");
        e.getPlayer().sendMessage("§7(30초 후 자동 취소됩니다)");
        
        // 30초 후 자동 취소
        timeoutTask = Bukkit.getScheduler().runTaskLater(EscapePlugin.getInstance(), () -> {
            if (acting != null && acting.equals(e.getPlayer().getUniqueId())) {
                Player player = Bukkit.getPlayer(acting);
                if (player != null) {
                    player.sendMessage("§c입력 시간이 초과되어 강탈권 사용이 취소되었습니다.");
                    restoreConsumedItem(player);
                }
                acting = null;
                consumedItem = null;
            }
        }, 20L * 30); // 30초
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if (acting == null || !e.getPlayer().getUniqueId().equals(acting)) return;
        e.setCancelled(true);

        UUID actorId = acting;
        acting = null;  // 중복 방지
        
        // 시간 초과 태스크 취소
        if (timeoutTask != null) {
            timeoutTask.cancel();
            timeoutTask = null;
        }

        Player actor  = Bukkit.getPlayer(actorId);
        Player target = Bukkit.getPlayer(e.getMessage());
        if (actor == null || target == null) {
            if (actor != null) {
                actor.sendMessage("§c해당 플레이어를 찾을 수 없습니다.");
                // 실패 시 아이템 복원
                restoreConsumedItem(actor);
            }
            return;
        }

        JobManager jm         = EscapePlugin.getInstance().getJobManager();
        JobType actorJobOld  = jm.getJob(actorId);
        JobType targetJobOld = jm.getJob(target.getUniqueId());

        // 1) 직업 스왑 (이 부분은 스레드 세이프)
        jm.setJob(actorId, targetJobOld);
        jm.setJob(target.getUniqueId(), actorJobOld);

        // 2) 실제 블록/인벤토리 작업과 메시지 전송은 메인 스레드로 옮기기
        Bukkit.getScheduler().runTask(EscapePlugin.getInstance(), () -> {
            depositItems(actor, actorJobOld);
            depositItems(target, targetJobOld);

            actor.sendMessage("§a당신과 " + target.getName() + "님의 직업이 서로 변경되었습니다");
            actor.sendMessage(actor.getName() + "님의 새로운 직업은 §e" + targetJobOld.getDisplayName() + "§f입니다.");
            target.sendMessage("§a당신과 " + actor.getName() + "님의 직업이 서로 변경되었습니다");
            target.sendMessage(target.getName() + "님의 새로운 직업은 §e" + actorJobOld.getDisplayName() + "§f입니다.");
            
            // 성공적으로 완료되었으므로 소모된 아이템 초기화
            consumedItem = null;
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        // 플레이어가 로그아웃하면 강탈권 사용 상태 초기화
        if (acting != null && acting.equals(e.getPlayer().getUniqueId())) {
            acting = null;
            consumedItem = null;
            if (timeoutTask != null) {
                timeoutTask.cancel();
                timeoutTask = null;
            }
        }
    }

    private void depositItems(Player player, JobType oldJob) {
        RegionManager rm = EscapePlugin.getInstance().getRegionManager();
        Optional<Region> opt = rm.getByJob(oldJob);
        if (opt.isEmpty()) return;

        Location chestLoc = opt.get().getChestLocation();
        BlockState bs = chestLoc.getBlock().getState();
        if (!(bs instanceof Chest chest)) return;

        PlayerInventory inv = player.getInventory();
        for (ItemStack is : inv.getContents()) {
            if (is == null) continue;
            
            // 제외할 아이템들 체크
            if (shouldExcludeItem(is)) continue;

            // 상자로 아이템 이동
            chest.getBlockInventory().addItem(is);
            inv.remove(is);
        }
        
        player.sendMessage("§e" + oldJob.getDisplayName() + " 영역의 상자에 아이템을 보관했습니다.");
    }
    
    /**
     * 강탈권 사용 시 제외할 아이템인지 확인
     */
    private boolean shouldExcludeItem(ItemStack item) {
        Material type = item.getType();
        
        // 1. 다이아몬드 원석만 제외 (다이아몬드 도구/갑옷은 포함)
        if (type == Material.DIAMOND) return true;
        
        // 2. 특정 게임 아이템들 제외
        if (ItemUtil.isType(item, GameItem.ENTRY)) return true;    // 입장권
        if (ItemUtil.isType(item, GameItem.ESCAPE)) return true;   // 탈출권
        if (ItemUtil.isType(item, GameItem.STEAL)) return true;    // 강탈권
        
        // 3. 자수정 제외
        if (type == Material.AMETHYST_SHARD) return true;
        
        // 나머지 모든 아이템은 상자로 이동 (다이아몬드 도구/갑옷 포함)
        return false;
    }
    
    /**
     * 소모된 강탈권 아이템을 플레이어에게 복원
     */
    private void restoreConsumedItem(Player player) {
        if (consumedItem != null) {
            player.getInventory().addItem(consumedItem);
            consumedItem = null;  // 복원 후 초기화
        }
    }
}