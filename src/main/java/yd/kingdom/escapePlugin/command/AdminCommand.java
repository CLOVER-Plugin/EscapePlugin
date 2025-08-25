package yd.kingdom.escapePlugin.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import yd.kingdom.escapePlugin.EscapePlugin;
import yd.kingdom.escapePlugin.job.JobManager;

public class AdminCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
        if (!(s instanceof Player) || !s.hasPermission("missiongame.admin")) return false;
        if (args.length < 1) {
            s.sendMessage("사용법: /관리자 <플레이어>");
            return true;
        }
        Player tgt = Bukkit.getPlayer(args[0]);
        if (tgt == null) return false;
        JobManager jm = EscapePlugin.getInstance().getJobManager();
        jm.addAdmin(tgt.getUniqueId());
        s.sendMessage("§a" + tgt.getName() + "님을 관리자로 지정했습니다.");
        return true;
    }
}