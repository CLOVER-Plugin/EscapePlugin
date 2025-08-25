package yd.kingdom.escapePlugin.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import yd.kingdom.escapePlugin.EscapePlugin;
import yd.kingdom.escapePlugin.job.JobType;

public class HelperRemoveCommand implements CommandExecutor {
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // 권한 체크
        if (!sender.hasPermission("escape.admin")) {
            sender.sendMessage("§c권한이 없습니다.");
            return true;
        }
        
        // 인수 개수 체크
        if (args.length != 1) {
            sender.sendMessage("§c사용법: /도우미해제 <플레이어>");
            return true;
        }
        
        String targetPlayerName = args[0];
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
        
        // 대상 플레이어가 온라인인지 체크
        if (targetPlayer == null) {
            sender.sendMessage("§c플레이어 '" + targetPlayerName + "'을(를) 찾을 수 없습니다.");
            return true;
        }
        
        // 현재 직업 확인
        JobType currentJob = EscapePlugin.getInstance().getJobManager().getJob(targetPlayer.getUniqueId());
        
        // 모든 직업에서 도우미 권한 제거
        for (JobType jobType : JobType.values()) {
            if (jobType != JobType.FREEMAN && jobType != JobType.ADMIN) {
                EscapePlugin.getInstance().getJobManager().removeHelper(targetPlayer.getUniqueId(), jobType);
            }
        }
        
        // FREEMAN으로 직업 변경
        EscapePlugin.getInstance().getJobManager().setJob(targetPlayer.getUniqueId(), JobType.FREEMAN);
        
        // 성공 메시지 전송
        sender.sendMessage("§a" + targetPlayer.getName() + "의 도우미 권한을 해제하고 자유민으로 설정했습니다.");
        targetPlayer.sendMessage("§a당신의 도우미 권한이 해제되었습니다. 이제 자유민입니다.");
        
        // 디버깅: 현재 상태 확인 (주석처리)
        // JobType newJob = EscapePlugin.getInstance().getJobManager().getJob(targetPlayer.getUniqueId());
        // sender.sendMessage("§e디버깅: " + targetPlayer.getName() + "의 이전 직업 = " + currentJob);
        // sender.sendMessage("§e디버깅: " + targetPlayer.getName() + "의 현재 직업 = " + newJob);
        
        return true;
    }
}
