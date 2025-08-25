package yd.kingdom.escapePlugin.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import yd.kingdom.escapePlugin.EscapePlugin;
import yd.kingdom.escapePlugin.job.JobManager;
import yd.kingdom.escapePlugin.job.JobType;

public class JobRemoveCommand implements CommandExecutor {
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // 권한 체크
        if (!sender.hasPermission("escape.admin")) {
            sender.sendMessage("§c권한이 없습니다.");
            return true;
        }
        
        // 인수 개수 체크
        if (args.length != 1) {
            sender.sendMessage("§c사용법: /직업해제 <플레이어>");
            return true;
        }
        
        String targetPlayerName = args[0];
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
        
        // 대상 플레이어가 온라인인지 체크
        if (targetPlayer == null) {
            sender.sendMessage("§c플레이어 '" + targetPlayerName + "'을(를) 찾을 수 없습니다.");
            return true;
        }
        
        // JobManager 가져오기
        JobManager jobManager = EscapePlugin.getInstance().getJobManager();
        
        // 현재 직업 확인
        JobType currentJob = jobManager.getJob(targetPlayer.getUniqueId());
        
        // 이미 자유민인지 체크
        if (currentJob == JobType.FREEMAN) {
            sender.sendMessage("§e" + targetPlayer.getName() + "은(는) 이미 자유민입니다.");
            return true;
        }
        
        // 직업 해제 (자유민으로 설정)
        jobManager.setJob(targetPlayer.getUniqueId(), JobType.FREEMAN);
        
        // 성공 메시지 전송
        sender.sendMessage("§a" + targetPlayer.getName() + "의 직업을 해제했습니다.");
        targetPlayer.sendMessage("§a당신의 직업이 해제되었습니다. 이제 자유민입니다.");
        
        // 서버에 알림 (선택사항)
        Bukkit.broadcastMessage("§e" + targetPlayer.getName() + "의 직업이 해제되었습니다.");
        
        return true;
    }
}
