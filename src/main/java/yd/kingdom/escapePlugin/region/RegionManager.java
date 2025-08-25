package yd.kingdom.escapePlugin.region;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import yd.kingdom.escapePlugin.EscapePlugin;
import yd.kingdom.escapePlugin.job.JobType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RegionManager {
    private final List<Region> regions = new ArrayList<>();

    public RegionManager() {
        ConfigurationSection sec = EscapePlugin.getInstance()
                .getConfig()
                .getConfigurationSection("regions");
        if (sec == null) return;

        for (String key : sec.getKeys(false)) {
            ConfigurationSection r = sec.getConfigurationSection(key);
            if (r == null) continue;

            JobType job = JobType.valueOf(r.getString("job", "FREEMAN"));
            Location minLoc   = parseLocationSection(r.getConfigurationSection("min"));
            Location maxLoc   = parseLocationSection(r.getConfigurationSection("max"));
            Location chestLoc = parseLocationSection(r.getConfigurationSection("chest"));

            if (minLoc != null && maxLoc != null && chestLoc != null) {
                regions.add(new Region(key, job, minLoc, maxLoc, chestLoc));
            } else {
                EscapePlugin.getInstance().getLogger()
                        .warning("Region '" + key + "' is missing min/max/chest configuration!");
            }
        }
    }

    private Location parseLocationSection(ConfigurationSection s) {
        if (s == null) return null;
        String world = s.getString("world");
        if (world == null || Bukkit.getWorld(world) == null) return null;
        double x = s.getDouble("x"), y = s.getDouble("y"), z = s.getDouble("z");
        return new Location(Bukkit.getWorld(world), x, y, z);
    }

    public Optional<Region> getByLocation(Location loc) {
        return regions.stream()
                .filter(r -> r.contains(loc))
                .findFirst();
    }

    /** 추가: 직업에 해당하는 Region을 찾아 반환 */
    public Optional<Region> getByJob(JobType job) {
        return regions.stream()
                .filter(r -> r.getJob() == job)
                .findFirst();
    }
}