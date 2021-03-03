package me.Ice.antiblockglitch;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

public class BlockChecker {

    private static Anticheat pl = Anticheat.getInstance();
    private static HashMap<Player, BlockChecker> blockCheckers = new HashMap<>();

    private Player player;
    private BukkitRunnable runnable;
    private Location lastBlock;

    private BlockChecker(Player p) {
        player = p;
    }

    public static BlockChecker getBlockChecker(Player p) {
        if (blockCheckers.containsKey(p)) {
            return blockCheckers.get(p);
        }
        else {
            return new BlockChecker(p);
        }
    }

    public void runBlockChecker() {
        Location lastBlock = player.getLocation();
        BukkitRunnable blockChecker = new BukkitRunnable() {
            public void run() {
                Location loc = player.getPlayer().getLocation();
                loc.setY((int) loc.getY());
                if (loc.subtract(0, 1, 0).getBlock().getType().equals(Material.AIR)) {
                    return;
                }
                setLastStoodBlock(loc);
            }
        };
        blockChecker.runTaskTimer(pl, 0, 1);
    }

    public void cancelBlockChecker() {
        runnable.cancel();
    }

    public Location getLastStoodBlock() {
        return lastBlock;
    }

    public void setLastStoodBlock(Location loc) {
        lastBlock = loc;
    }

}
