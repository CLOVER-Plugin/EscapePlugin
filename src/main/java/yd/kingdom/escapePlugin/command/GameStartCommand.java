package yd.kingdom.escapePlugin.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class GameStartCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // 권한 체크(선택)
        if (!sender.hasPermission("missiongame.admin")) {
            sender.sendMessage("§c권한이 없습니다.");
            return true;
        }

        Bukkit.broadcastMessage("§a§l[게임시작]§r 게임이 시작되었습니다!");
        return true;
    }
}