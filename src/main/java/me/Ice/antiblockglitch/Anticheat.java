package me.Ice.antiblockglitch;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;

public class Anticheat extends JavaPlugin {

    private static Anticheat instance;
    private String denyMessage;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();  // Saves config if not present
        reloadDenyMessage();

        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new CheckerHandler(), this);
    }

    @Override
    public void onDisable() {
        instance = null;
    }

    public static Anticheat getInstance() {
        return instance;
    }

    public void reloadDenyMessage() {
        FileConfiguration config = getConfig();
        denyMessage = config.getString("deny-message", "&c&l(!) You are not allowed to place blocks here!");
    }

    public String getDenyMessage() {
        return denyMessage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("antiblockreload")) {
            reloadConfig();
            reloadDenyMessage();
            sender.sendMessage(ChatColor.GREEN + "AntiBlockGlitch config reloaded!");
            return true;
        }
        return false;
    }

}
