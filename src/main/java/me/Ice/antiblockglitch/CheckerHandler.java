package me.Ice.antiblockglitch;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionContainer;
import com.sk89q.worldguard.protection.managers.RegionManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class CheckerHandler implements Listener {

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("antiblockglitch.override")) return;

        Location location = event.getBlockPlaced().getLocation();
        World world = location.getWorld();
        if (world == null) return;

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager manager = container.get(BukkitAdapter.adapt(world));
        if (manager == null) return;

        ApplicableRegionSet regions = manager.getApplicableRegions(BlockVector3.at(location.getX(), location.getY(), location.getZ()));

        boolean cancel = regions.getRegions().stream().anyMatch(region -> 
            (region.getFlags().getOrDefault(Flags.BLOCK_PLACE, StateFlag.State.ALLOW) == StateFlag.State.DENY ||
            region.getFlags().getOrDefault(Flags.BUILD, StateFlag.State.ALLOW) == StateFlag.State.DENY) &&
            !region.getMembers().contains(player.getUniqueId()));

        if (cancel) {
            event.setCancelled(true);
            event.getBlockPlaced().setType(Material.AIR);

            BlockChecker checker = BlockChecker.getBlockChecker(player);
            Location lastBlock = checker.getLastStoodBlock();
            if (lastBlock == null || lastBlock.equals(location)) return;

            Bukkit.getScheduler().runTaskLater(Anticheat.getInstance(), () -> {
                if (player.isOnline()) {
                    player.teleport(lastBlock.add(0, 1, 0));
                }
            }, 1L);

            String denyMessage = ChatColor.translateAlternateColorCodes('&', Anticheat.getInstance().getDenyMessage());
            player.sendMessage(denyMessage);
        }
    }
}
