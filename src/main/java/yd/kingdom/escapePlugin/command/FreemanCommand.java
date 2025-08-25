package yd.kingdom.escapePlugin.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import yd.kingdom.escapePlugin.EscapePlugin;
import yd.kingdom.escapePlugin.job.JobManager;
import yd.kingdom.escapePlugin.job.JobType;

public class FreemanCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // 관리자 권한 체크
        if (!sender.hasPermission("missiongame.admin")) {
            sender.sendMessage("§c권한이 없습니다.");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("사용법: /자유민 <플레이어>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage("§c플레이어를 찾을 수 없습니다.");
            return true;
        }

        JobManager jm = EscapePlugin.getInstance().getJobManager();
        jm.setJob(target.getUniqueId(), JobType.FREEMAN);
        sender.sendMessage("§a" + target.getName() + "님을 자유민으로 설정했습니다.");
        target.sendMessage("§e관리자에 의해 자유민 권한이 부여되었습니다.");

        return true;
    }
}