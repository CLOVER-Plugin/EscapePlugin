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

import java.util.*;

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

        // 1) 도우미 목록을 먼저 저장 (직업 변경 전에)
        Set<UUID> actorHelpers = new HashSet<>(jm.getHelpers(actorJobOld));
        Set<UUID> targetHelpers = new HashSet<>(jm.getHelpers(targetJobOld));

        // 2) 직업 스왑 (이 부분은 스레드 세이프)
        jm.setJob(actorId, targetJobOld);
        jm.setJob(target.getUniqueId(), actorJobOld);

        // 3) 도우미들의 직업도 함께 변경
        swapHelpers(jm, actorJobOld, targetJobOld, actorHelpers, targetHelpers);

        // 3) 실제 블록/인벤토리 작업과 메시지 전송은 메인 스레드로 옮기기
        Bukkit.getScheduler().runTask(EscapePlugin.getInstance(), () -> {
            // 기존 방식: 개별 플레이어의 아이템만 상자로 이동
            // depositItems(actor, actorJobOld);
            // depositItems(target, targetJobOld);
            
            // 새로운 방식: 아이템을 이전 직업의 상자로 이동
            // A(농부→광부)의 아이템은 농부 상자로, B(광부→농부)의 아이템은 광부 상자로
            
            // A 영역의 모든 사람들(노동자 + 도우미)의 아이템을 농부 상자로
            Set<UUID> actorRegionPlayers = new HashSet<>(actorHelpers);
            actorRegionPlayers.add(actorId);  // A 플레이어 추가
            depositAllRegionItems(actorJobOld, actorRegionPlayers);
            
            // B 영역의 모든 사람들(노동자 + 도우미)의 아이템을 광부 상자로
            Set<UUID> targetRegionPlayers = new HashSet<>(targetHelpers);
            targetRegionPlayers.add(target.getUniqueId());  // B 플레이어 추가
            depositAllRegionItems(targetJobOld, targetRegionPlayers);

            // 메인 플레이어들의 메시지
            actor.sendMessage("§a당신과 " + target.getName() + "님의 직업이 서로 변경되었습니다");
            actor.sendMessage(actor.getName() + "님의 새로운 직업은 §e" + targetJobOld.getDisplayName() + "§f입니다.");
            target.sendMessage("§a당신과 " + actor.getName() + "님의 직업이 서로 변경되었습니다");
            target.sendMessage(target.getName() + "님의 새로운 직업은 §e" + actorJobOld.getDisplayName() + "§f입니다.");
            
            // 도우미 변경 메시지 전송
            sendHelperChangeMessages(jm, actorJobOld, targetJobOld, actorHelpers, targetHelpers);
            
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

    /**
     * 해당 영역에 소속된 모든 플레이어(영역 권한 있는 노동자 + 영역에 속한 도우미)의 
     * 아이템을 상자로 이동시킵니다.
     */
    private void depositAllRegionItems(JobType job) {
        RegionManager rm = EscapePlugin.getInstance().getRegionManager();
        Optional<Region> opt = rm.getByJob(job);
        if (opt.isEmpty()) return;

        Region region = opt.get();
        Location chestLoc = region.getChestLocation();
        BlockState bs = chestLoc.getBlock().getState();
        if (!(bs instanceof Chest chest)) return;

        JobManager jm = EscapePlugin.getInstance().getJobManager();
        Set<UUID> affectedPlayers = new HashSet<>();

        // 1. 해당 직업을 가진 플레이어들 (영역 권한 있는 노동자)
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (jm.getJob(player.getUniqueId()) == job) {
                affectedPlayers.add(player.getUniqueId());
            }
        }

        // 2. 해당 직업 영역의 도우미들
        Set<UUID> helpers = jm.getHelpers(job);
        if (helpers != null) {
            affectedPlayers.addAll(helpers);
        }

        // 3. 모든 영향받는 플레이어의 아이템을 상자로 이동
        for (UUID playerId : affectedPlayers) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                depositPlayerItems(player, chest);
            }
        }

        // 4. 결과 메시지 전송
        if (!affectedPlayers.isEmpty()) {
            String message = String.format("§e%s 영역에 소속된 %d명의 플레이어 아이템이 상자로 이동되었습니다.", 
                job.getDisplayName(), affectedPlayers.size());
            
            for (UUID playerId : affectedPlayers) {
                Player player = Bukkit.getPlayer(playerId);
                if (player != null && player.isOnline()) {
                    player.sendMessage(message);
                }
            }
        }
    }

    /**
     * 지정된 플레이어들의 아이템을 특정 직업 영역의 상자로 이동시킵니다.
     * 강탈권 사용 시 새로 받은 직업의 상자로 아이템을 이동할 때 사용합니다.
     */
    private void depositAllRegionItems(JobType job, Set<UUID> affectedPlayers) {
        RegionManager rm = EscapePlugin.getInstance().getRegionManager();
        Optional<Region> opt = rm.getByJob(job);
        if (opt.isEmpty()) return;

        Region region = opt.get();
        Location chestLoc = region.getChestLocation();
        BlockState bs = chestLoc.getBlock().getState();
        if (!(bs instanceof Chest chest)) return;

        // 모든 영향받는 플레이어의 아이템을 상자로 이동
        for (UUID playerId : affectedPlayers) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                depositPlayerItems(player, chest);
            }
        }

        // 결과 메시지 전송
        if (!affectedPlayers.isEmpty()) {
            String message = String.format("§e%s 영역의 상자로 %d명의 플레이어 아이템이 이동되었습니다.", 
                job.getDisplayName(), affectedPlayers.size());
            
            for (UUID playerId : affectedPlayers) {
                Player player = Bukkit.getPlayer(playerId);
                if (player != null && player.isOnline()) {
                    player.sendMessage(message);
                }
            }
        }
    }

    /**
     * 개별 플레이어의 아이템을 상자로 이동시킵니다.
     */
    private void depositPlayerItems(Player player, Chest chest) {
        PlayerInventory inv = player.getInventory();
        int movedCount = 0;
        
        for (ItemStack is : inv.getContents()) {
            if (is == null) continue;
            
            // 제외할 아이템들 체크
            if (shouldExcludeItem(is)) continue;

            // 상자로 아이템 이동
            chest.getBlockInventory().addItem(is);
            inv.remove(is);
            movedCount++;
        }
        
        if (movedCount > 0) {
            player.sendMessage("§e" + movedCount + "개의 아이템이 상자로 이동되었습니다.");
        }
    }

    /**
     * 기존 방식: 개별 플레이어의 아이템만 상자로 이동 (하위 호환성 유지)
     */
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

    /**
     * 도우미들의 직업을 변경합니다.
     */
    private void swapHelpers(JobManager jm, JobType oldJob, JobType newJob, Set<UUID> oldHelpers, Set<UUID> newHelpers) {
        // 1. 기존 도우미들의 직업을 새로운 직업으로 변경
        if (oldHelpers != null && !oldHelpers.isEmpty()) {
            for (UUID helperId : oldHelpers) {
                jm.setJob(helperId, newJob);
            }
        }

        // 2. 새로운 도우미들의 직업을 기존 직업으로 변경
        if (newHelpers != null && !newHelpers.isEmpty()) {
            for (UUID helperId : newHelpers) {
                jm.setJob(helperId, oldJob);
            }
        }
    }

    /**
     * 도우미들에게 직업 변경 메시지를 전송합니다.
     */
    private void sendHelperChangeMessages(JobManager jm, JobType oldJob, JobType newJob, Set<UUID> oldHelpers, Set<UUID> newHelpers) {
        // 1. 기존 도우미들에게 메시지 전송
        if (oldHelpers != null && !oldHelpers.isEmpty()) {
            for (UUID helperId : oldHelpers) {
                Player helper = Bukkit.getPlayer(helperId);
                if (helper != null && helper.isOnline()) {
                    helper.sendMessage("§a당신의 직업이 §e" + oldJob.getDisplayName() + "§f에서 §e" + newJob.getDisplayName() + "§f로 변경되었습니다.");
                }
            }
        }

        // 2. 새로운 도우미들에게 메시지 전송
        if (newHelpers != null && !newHelpers.isEmpty()) {
            for (UUID helperId : newHelpers) {
                Player helper = Bukkit.getPlayer(helperId);
                if (helper != null && helper.isOnline()) {
                    helper.sendMessage("§a당신의 직업이 §e" + newJob.getDisplayName() + "§f에서 §e" + oldJob.getDisplayName() + "§f로 변경되었습니다.");
                }
            }
        }
    }
}