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
        e.getPlayer().sendMessage("t");
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        BlockChecker checker = BlockChecker.getBlockChecker(e.getPlayer());
        checker.cancelBlockChecker();
    }

    @EventHandler
    public void onRightClick(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        if (p.hasPermission("antiblockglitch.override")) {
            return;
        }
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager manager = container.get(BukkitAdapter.adapt(p.getWorld()));
        Location loc = e.getBlockPlaced().getLocation();
        ApplicableRegionSet regions = manager.getApplicableRegions(BlockVector3.at(loc.getX(), loc.getY(), loc.getZ()));
        Set<ProtectedRegion> set = regions.getRegions();
        boolean cancel = false;
        for (ProtectedRegion rg : set) {
            try {
                if (rg.getFlags().get(Flags.BLOCK_PLACE) == StateFlag.State.DENY) {
                    if (!rg.getMembers().contains(p.getUniqueId())) {
                        cancel = true;
                    }
                }
            } catch (NullPointerException ex) {
                try {
                    if (rg.getFlags().get(Flags.BUILD) == StateFlag.State.DENY) {
                        if (!rg.getMembers().contains(p.getUniqueId())) {
                            cancel = true;
                        }
                    }
                } catch (NullPointerException ex1) {

                }
            }
        }
        if (cancel) {
            e.setCancelled(true);
            e.getBlockPlaced().setType(Material.AIR);
            BlockChecker checker = BlockChecker.getBlockChecker(p);
            p.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "(!) You are not allowed to place blocks here!");
            Location block = new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ());
            Location lastBlock = new Location(checker.getLastStoodBlock().getWorld(), checker.getLastStoodBlock().getX(), checker.getLastStoodBlock().getY(), checker.getLastStoodBlock().getZ());
            if (block == lastBlock) {
                return;
            }
            p.teleport(checker.getLastStoodBlock().add(0, 1, 0));
        }
    }

}
