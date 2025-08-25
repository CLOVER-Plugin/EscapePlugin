package yd.kingdom.escapePlugin.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import yd.kingdom.escapePlugin.EscapePlugin;
import yd.kingdom.escapePlugin.job.JobType;
import yd.kingdom.escapePlugin.region.Region;
import yd.kingdom.escapePlugin.region.RegionManager;

import java.util.Optional;

public class RegionCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
        if (args.length < 1) {
            s.sendMessage("사용법: /영역 <플레이어>");
            return true;
        }
        Player tgt = Bukkit.getPlayer(args[0]);
        if (tgt == null) return false;
        JobType job = EscapePlugin.getInstance()
                .getJobManager()
                .getJob(tgt.getUniqueId());
        RegionManager rm = EscapePlugin.getInstance().getRegionManager();
        Optional<Region> reg = rm.getByJob(job);
        if (reg.isPresent()) {
            s.sendMessage(tgt.getName() + "님의 영역: " + reg.get().getName());
        } else {
            s.sendMessage("§c해당 직업에 대한 영역이 없습니다.");
        }
        return true;
    }
}