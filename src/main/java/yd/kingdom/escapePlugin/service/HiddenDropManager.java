package yd.kingdom.escapePlugin.service;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import yd.kingdom.escapePlugin.EscapePlugin;
import yd.kingdom.escapePlugin.job.JobType;
import yd.kingdom.escapePlugin.service.SimpleSkinLoader;

import java.util.HashMap;
import java.util.Map;

public class HiddenDropManager {
    private record Config(double chance, String owner) {}
    private static final Map<JobType, Config> map = new HashMap<>();

    /** onEnable() 에서 한 번만 로드 */
    public static void load() {
        ConfigurationSection sec = EscapePlugin.getInstance().getConfig().getConfigurationSection("hidden");
        if (sec == null) return;
        for (String key : sec.getKeys(false)) {
            JobType job = JobType.valueOf(key.toUpperCase());
            ConfigurationSection h = sec.getConfigurationSection(key);
            double chance = h.getDouble("chance", 0.01);
            String owner  = h.getString("owner", "");
            map.put(job, new Config(chance, owner));
        }
    }

    /** 직업별로 확률 체크 후 머리 드랍 */
    public static void attempt(JobType job, Location loc) {
        Config c = map.get(job);
        if (c == null) return;
        if (Math.random() < c.chance) {
            // Mojang API를 우선적으로 사용하여 스킨이 적용된 머리 생성
            ItemStack skull = SimpleSkinLoader.createPlayerHead(c.owner);
            
            // 머리 드롭
            loc.getWorld().dropItemNaturally(loc, skull);
            
            // EscapePlugin.getInstance().getLogger().info("히든 드롭 완료: " + job + " 직업, 플레이어: " + c.owner);
        }
    }
    

}