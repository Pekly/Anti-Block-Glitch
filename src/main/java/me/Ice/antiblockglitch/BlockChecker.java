package me.Ice.antiblockglitch;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

public class BlockChecker {

    private static final HashMap<Player, BlockChecker> blockCheckers = new HashMap<>();
    private final Player player;
    private BukkitRunnable runnable;
    private Location lastBlock;

    private BlockChecker(Player p) {
        this.player = p;
    }

    public static BlockChecker getBlockChecker(Player p) {
        return blockCheckers.computeIfAbsent(p, BlockChecker::new);
    }

    public void runBlockChecker() {
        lastBlock = player.getLocation();
        runnable = new BukkitRunnable() {
            public void run() {
                Location loc = player.getLocation();
                loc.setY((int) loc.getY());
                if (loc.subtract(0, 1, 0).getBlock().getType() == Material.AIR) {
                    return;
                }
                setLastStoodBlock(loc);
            }
        };
        runnable.runTaskTimer(Anticheat.getInstance(), 0, 1);
    }

    public void cancelBlockChecker() {
        if (runnable != null) {
            runnable.cancel();
        }
    }

    public Location getLastStoodBlock() {
        return lastBlock;
    }

    public void setLastStoodBlock(Location loc) {
        lastBlock = loc;
    }
}
