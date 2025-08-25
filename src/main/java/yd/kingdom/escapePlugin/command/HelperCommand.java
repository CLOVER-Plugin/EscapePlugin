package yd.kingdom.escapePlugin.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import yd.kingdom.escapePlugin.EscapePlugin;
import yd.kingdom.escapePlugin.job.JobType;

public class HelperCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
        if (!s.hasPermission("escape.admin")) return false;
        if (args.length < 2) {
            s.sendMessage("사용법: /도우미 <플레이어> <직업>");
            return true;
        }
        Player tgt = Bukkit.getPlayer(args[0]);
        if (tgt == null) {
            s.sendMessage("§c플레이어를 찾을 수 없습니다.");
            return true;
        }
        JobType job = JobType.fromDisplayName(args[1]);
        if (job == null) {
            s.sendMessage("§c유효한 직업이 아닙니다.");
            return true;
        }
        // 기존 FREEMAN 권한 제거 (모든 직업 도우미 권한)
        JobType oldJob = EscapePlugin.getInstance().getJobManager().getJob(tgt.getUniqueId());
        if (oldJob == JobType.FREEMAN) {
            EscapePlugin.getInstance().getJobManager().removeFreemanHelper(tgt.getUniqueId());
        }
        
        // 도우미 권한 부여 및 직업 변경
        EscapePlugin.getInstance().getJobManager().addHelper(tgt.getUniqueId(), job);
        EscapePlugin.getInstance().getJobManager().setJob(tgt.getUniqueId(), job);
        
        s.sendMessage("§a" + tgt.getName() + "님을 " + job.getDisplayName() + "로 지정했습니다.");
        tgt.sendMessage("§a당신이 " + job.getDisplayName() + "로 지정되었습니다.");
        
        // 디버깅: 도우미 권한과 직업이 제대로 설정되었는지 확인 (주석처리)
        // boolean isHelper = EscapePlugin.getInstance().getJobManager().isHelper(tgt.getUniqueId(), job);
        // JobType newJob = EscapePlugin.getInstance().getJobManager().getJob(tgt.getUniqueId());
        // s.sendMessage("§e디버깅: " + tgt.getName() + "의 " + job.getDisplayName() + " 도우미 권한 = " + isHelper);
        // s.sendMessage("§e디버깅: " + tgt.getName() + "의 현재 직업 = " + newJob);
        return true;
    }
}