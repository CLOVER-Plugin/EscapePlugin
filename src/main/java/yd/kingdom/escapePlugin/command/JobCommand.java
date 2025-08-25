package yd.kingdom.escapePlugin.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import yd.kingdom.escapePlugin.EscapePlugin;
import yd.kingdom.escapePlugin.job.JobType;

public class JobCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
        if (args.length < 1) {
            s.sendMessage("사용법: /직업 <플레이어>");
            return true;
        }
        Player tgt = Bukkit.getPlayer(args[0]);
        if (tgt == null) return false;
        JobType job = EscapePlugin.getInstance().getJobManager().getJob(tgt.getUniqueId());
        s.sendMessage(tgt.getName() + "님의 직업: " + job.getDisplayName());
        return true;
    }
}