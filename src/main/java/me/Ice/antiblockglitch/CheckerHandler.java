package me.Ice.antiblockglitch;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Set;

public class CheckerHandler implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        BlockChecker checker = BlockChecker.getBlockChecker(e.getPlayer());
        checker.runBlockChecker();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        BlockChecker checker = BlockChecker.getBlockChecker(e.getPlayer());
        checker.cancelBlockChecker();
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        if (p.hasPermission("antiblockglitch.override")) {
            return;
        }

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager manager = container.get(BukkitAdapter.adapt(p.getWorld()));
        if (manager == null) return;

        Location loc = e.getBlockPlaced().getLocation();
        ApplicableRegionSet regions = manager.getApplicableRegions(BlockVector3.at(loc.getX(), loc.getY(), loc.getZ()));
        Set<ProtectedRegion> set = regions.getRegions();

        boolean cancel = false;
        for (ProtectedRegion rg : set) {
            StateFlag.State blockPlaceFlag = rg.getFlags().getOrDefault(Flags.BLOCK_PLACE, StateFlag.State.ALLOW);
            StateFlag.State buildFlag = rg.getFlags().getOrDefault(Flags.BUILD, StateFlag.State.ALLOW);

            if (blockPlaceFlag == StateFlag.State.DENY || buildFlag == StateFlag.State.DENY) {
                if (!rg.getMembers().contains(p.getUniqueId())) {
                    cancel = true;
                    break;
                }
            }
        }

        if (cancel) {
            e.setCancelled(true);
            e.getBlockPlaced().setType(Material.AIR);
            BlockChecker checker = BlockChecker.getBlockChecker(p);
            p.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "(!) You are not allowed to place blocks here!");

            Location lastBlock = checker.getLastStoodBlock();
            if (!loc.equals(lastBlock)) {
                Location safeLoc = lastBlock.clone().add(0, 1, 0);
                if (!safeLoc.getBlock().getType().isSolid()) {
                    p.teleport(safeLoc);
                }
            }
        }
    }
}
