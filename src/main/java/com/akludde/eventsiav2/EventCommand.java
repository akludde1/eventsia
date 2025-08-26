package com.akludde.eventsiav2.commands;

import com.akludde.eventsiav2.Eventsiav2;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

public class EventCommand implements TabExecutor {

    private final Eventsiav2 plugin;
    private final FileConfiguration config;

    private boolean eventRunning = false;
    private final Set<UUID> inEvent = new HashSet<>();
    private final Set<UUID> blacklist = new HashSet<>();
    private final Map<UUID, Location> previousLocations = new HashMap<>();

    public EventCommand(Eventsiav2 plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    public Set<UUID> getInEvent() {
        return Collections.unmodifiableSet(inEvent);
    }

    private String getPrefix() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("prefix", "&6[Event] ")) + " ";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(getPrefix() + ChatColor.RED + "Only players can join the event!");
                return true;
            }

            if (!eventRunning) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cNo Event Is Active Right Now!"));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return true;
            }

            teleportToEvent(player);
            return true;
        }

        return handleSubCommands(sender, args);
    }

    private boolean handleSubCommands(CommandSender sender, String[] args) {
        switch (args[0].toLowerCase()) {
            case "message":
                if (!sender.hasPermission("event.startrun")) return true;
                sendPreEventMessage();
                sender.sendMessage(getPrefix() + ChatColor.GREEN + "Event message sent.");
                return true;

            case "start":
                if (!sender.hasPermission("event.startrun")) return true;
                startEvent();
                return true;

            case "stop":
                if (!sender.hasPermission("event.startrun")) return true;
                stopEvent();
                sender.sendMessage(getPrefix() + ChatColor.GREEN + "Event stopped!");
                return true;

            case "leave":
                if (!(sender instanceof Player player)) return true;
                leaveEvent(player);
                return true;

            case "setworld":
                if (!sender.hasPermission("event.startrun")) return true;
                if (args.length < 2) { sender.sendMessage(getPrefix() + ChatColor.RED + "Usage: /event setworld <world>"); return true; }
                setWorld(sender, args[1]);
                return true;

            case "setcoords":
                if (!sender.hasPermission("event.startrun")) return true;
                if (args.length < 4) { sender.sendMessage(getPrefix() + ChatColor.RED + "Usage: /event setcoords <x> <y> <z>"); return true; }
                setCoords(sender, args[1], args[2], args[3]);
                return true;

            case "blacklist":
                if (!sender.hasPermission("event.startrun")) return true;
                if (args.length < 2) { sender.sendMessage(getPrefix() + ChatColor.RED + "Usage: /event blacklist <player>"); return true; }
                modifyBlacklist(args[1], true, sender);
                return true;

            case "unblacklist":
                if (!sender.hasPermission("event.startrun")) return true;
                if (args.length < 2) { sender.sendMessage(getPrefix() + ChatColor.RED + "Usage: /event unblacklist <player>"); return true; }
                modifyBlacklist(args[1], false, sender);
                return true;
        }
        return false;
    }

    private void sendPreEventMessage() {
        List<String> lines = config.getStringList("broadcast-message");
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (String line : lines) {
                if (line.contains("HERE")) {
                    String[] parts = line.split("HERE");
                    TextComponent part1 = new TextComponent(ChatColor.translateAlternateColorCodes('&', parts[0]));
                    TextComponent here = new TextComponent(ChatColor.GREEN + "HERE");
                    here.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/event"));
                    part1.addExtra(here);
                    if (parts.length > 1) part1.addExtra(new TextComponent(ChatColor.translateAlternateColorCodes('&', parts[1])));
                    player.spigot().sendMessage(part1);
                } else {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
                }
            }
        }
    }

    private void startEvent() {
        if (eventRunning) {
            plugin.getServer().broadcastMessage(getPrefix() + ChatColor.RED + "Event is already running!");
            return;
        }

        eventRunning = true;
        plugin.getServer().broadcastMessage(getPrefix() + ChatColor.GREEN + "Event has started!");
    }

    private void stopEvent() {
        eventRunning = false;

        for (UUID uuid : new HashSet<>(inEvent)) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) leaveEvent(p);
        }
        inEvent.clear();
    }

    private void teleportToEvent(Player player) {
        if (player == null || !player.isOnline()) return;

        if (blacklist.contains(player.getUniqueId())) {
            player.sendMessage(getPrefix() + ChatColor.RED + "You are blacklisted from this event.");
            return;
        }

        Location loc = getEventLocation();
        if (loc == null) {
            player.sendMessage(getPrefix() + ChatColor.RED + "Event location not set!");
            return;
        }

        previousLocations.put(player.getUniqueId(), player.getLocation());
        player.teleport(loc);
        player.setGameMode(GameMode.SURVIVAL);
        inEvent.add(player.getUniqueId());
        player.sendMessage(getPrefix() + ChatColor.GREEN + "Teleported to event!");
    }

    private void leaveEvent(Player player) {
        if (!inEvent.contains(player.getUniqueId())) {
            player.sendMessage(getPrefix() + ChatColor.RED + "You are not in the event.");
            return;
        }

        Location prev = previousLocations.get(player.getUniqueId());
        if (prev != null) player.teleport(prev);
        player.setGameMode(GameMode.SURVIVAL);
        inEvent.remove(player.getUniqueId());
        previousLocations.remove(player.getUniqueId());
        player.sendMessage(getPrefix() + ChatColor.GREEN + "You left the event.");
    }

    private void setWorld(CommandSender sender, String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) { sender.sendMessage(getPrefix() + ChatColor.RED + "World not found!"); return; }
        config.set("event-world", worldName);
        plugin.saveConfig();
        sender.sendMessage(getPrefix() + ChatColor.GREEN + "Event world set to " + worldName);
    }

    private void setCoords(CommandSender sender, String xs, String ys, String zs) {
        try {
            int x = Integer.parseInt(xs);
            int y = Integer.parseInt(ys);
            int z = Integer.parseInt(zs);
            config.set("event-coordinates.x", x);
            config.set("event-coordinates.y", y);
            config.set("event-coordinates.z", z);
            plugin.saveConfig();
            sender.sendMessage(getPrefix() + ChatColor.GREEN + "Event coordinates set to " + x + "," + y + "," + z);
        } catch (NumberFormatException e) {
            sender.sendMessage(getPrefix() + ChatColor.RED + "Invalid coordinates!");
        }
    }

    private void modifyBlacklist(String playerName, boolean add, CommandSender sender) {
        Player target = Bukkit.getPlayerExact(playerName);
        if (target == null) { sender.sendMessage(getPrefix() + ChatColor.RED + "Player not found!"); return; }
        if (add) {
            blacklist.add(target.getUniqueId());
            leaveEvent(target);
            sender.sendMessage(getPrefix() + ChatColor.GREEN + "Player " + playerName + " blacklisted.");
        } else {
            blacklist.remove(target.getUniqueId());
            sender.sendMessage(getPrefix() + ChatColor.GREEN + "Player " + playerName + " removed from blacklist.");
        }
    }

    private Location getEventLocation() {
        String worldName = config.getString("event-world");
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;

        int x = config.getInt("event-coordinates.x");
        int y = config.getInt("event-coordinates.y");
        int z = config.getInt("event-coordinates.z");
        return new Location(world, x + 0.5, y, z + 0.5);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return Arrays.asList("message","start","stop","leave","setworld","setcoords","blacklist","unblacklist");
        return Collections.emptyList();
    }
}


