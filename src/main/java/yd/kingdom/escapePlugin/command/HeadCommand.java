package yd.kingdom.escapePlugin.command;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import yd.kingdom.escapePlugin.EscapePlugin;
import yd.kingdom.escapePlugin.job.JobType;
import yd.kingdom.escapePlugin.service.SimpleSkinLoader;
import yd.kingdom.escapePlugin.service.ProfileResolver;
import org.bukkit.profile.PlayerProfile;

import java.util.ArrayList;
import java.util.List;

public class HeadCommand implements CommandExecutor {
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // 권한 체크
        if (!sender.hasPermission("escape.admin")) {
            sender.sendMessage("§c권한이 없습니다.");
            return true;
        }
        
        // 인수 개수 체크
        if (args.length != 1) {
            sender.sendMessage("§c사용법: /머리 <플레이어>");
            return true;
        }
        
        String targetPlayerName = args[0];
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
        
        // 대상 플레이어가 온라인인지 체크
        if (targetPlayer == null) {
            sender.sendMessage("§c플레이어 '" + targetPlayerName + "'을(를) 찾을 수 없습니다.");
            return true;
        }
        
        // 모든 직업의 머리를 지급
        List<ItemStack> heads = createAllJobHeads();
        
        // 플레이어에게 머리들 지급
        for (ItemStack head : heads) {
            targetPlayer.getInventory().addItem(head);
        }
        
        // 성공 메시지 전송
        sender.sendMessage("§a" + targetPlayer.getName() + "에게 모든 직업의 머리를 지급했습니다.");
        targetPlayer.sendMessage("§a모든 직업의 머리를 받았습니다! 총 " + heads.size() + "개의 머리입니다.");
        
        return true;
    }
    
    private List<ItemStack> createAllJobHeads() {
        List<ItemStack> heads = new ArrayList<>();
        
        // config에서 히든 드롭 설정을 가져와서 동일한 머리 생성
        heads.add(createJobHead(JobType.WATERMELON));
        heads.add(createJobHead(JobType.MINERAL));
        heads.add(createJobHead(JobType.WOOD));
        heads.add(createJobHead(JobType.FLOWER));
        heads.add(createJobHead(JobType.TURTLE));
        heads.add(createJobHead(JobType.FISHING));
        
        return heads;
    }
    
    private ItemStack createJobHead(JobType jobType) {
        // config에서 해당 직업의 owner 정보를 가져와서 설정
        String owner = getHeadOwner(jobType);
        if (owner != null && !owner.isEmpty()) {
            // Profile API를 우선적으로 사용하여 머리 생성
            return createPlayerHeadWithProfileAPI(owner);
        }
        
        // 기본 머리 반환
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§7알 수 없는 머리");
            skull.setItemMeta(meta);
        }
        return skull;
    }
    
    private ItemStack createPlayerHeadWithProfileAPI(String playerName) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        if (meta == null) return skull;

        try {
            // Mojang API를 우선적으로 사용하여 스킨이 적용된 머리 생성
            ItemStack mojangHead = SimpleSkinLoader.createPlayerHead(playerName);
            if (mojangHead != null) {
                return mojangHead;
            }
        } catch (Exception e) {
            // EscapePlugin.getInstance().getLogger().warning("HeadCommand: Mojang API 실패: " + playerName + " - " + e.getMessage());
        }

        // 최후의 폴백
        meta.setDisplayName("§7" + playerName + "의 머리");
        skull.setItemMeta(meta);
        return skull;
    }
    

    
    private String getHeadOwner(JobType jobType) {
        // config에서 해당 직업의 owner 정보를 가져오기
        String configKey = jobType.name().toLowerCase();
        return EscapePlugin.getInstance().getConfig()
                .getString("hidden." + configKey + ".owner", "");
    }
}
