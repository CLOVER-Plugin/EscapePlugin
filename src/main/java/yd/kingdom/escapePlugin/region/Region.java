package yd.kingdom.escapePlugin.region;

import org.bukkit.Location;
import yd.kingdom.escapePlugin.job.JobType;

public class Region {
    private final String name;
    private final JobType job;
    private final Location minCorner, maxCorner;
    private final Location chestLocation;

    public Region(String name, JobType job, Location min, Location max, Location chest) {
        this.name = name;
        this.job = job;
        this.minCorner = min;
        this.maxCorner = max;
        this.chestLocation = chest;
    }

    public boolean contains(Location loc) {
        if (!loc.getWorld().equals(minCorner.getWorld())) return false;

        double x = loc.getX(), y = loc.getY(), z = loc.getZ();
        double minX = Math.min(minCorner.getX(), maxCorner.getX());
        double maxX = Math.max(minCorner.getX(), maxCorner.getX());
        double minY = Math.min(minCorner.getY(), maxCorner.getY());
        double maxY = Math.max(minCorner.getY(), maxCorner.getY());
        double minZ = Math.min(minCorner.getZ(), maxCorner.getZ());
        double maxZ = Math.max(minCorner.getZ(), maxCorner.getZ());

        return x >= minX && x <= maxX
                && y >= minY && y <= maxY
                && z >= minZ && z <= maxZ;
    }

    public JobType getJob() { return job; }
    public String getName() { return name; }
    public Location getChestLocation() { return chestLocation; }
}