package it.hydr4.oraxennature.growth;

import org.bukkit.Location;

public class TrackedBlock {

    private final Location location;
    private final GrowableBlock growableBlock;
    private long lastGrowthTime;

    public TrackedBlock(Location location, GrowableBlock growableBlock) {
        this.location = location;
        this.growableBlock = growableBlock;
        this.lastGrowthTime = System.currentTimeMillis();
    }

    public Location getLocation() {
        return location;
    }

    public GrowableBlock getGrowableBlock() {
        return growableBlock;
    }

    public long getLastGrowthTime() {
        return lastGrowthTime;
    }

    public void setLastGrowthTime(long lastGrowthTime) {
        this.lastGrowthTime = lastGrowthTime;
    }
}
