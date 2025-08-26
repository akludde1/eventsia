package com.akludde.eventsiav2.listeners;

import com.akludde.eventsiav2.Eventsiav2;
import com.akludde.eventsiav2.commands.EventCommand;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.Sound;

public class EventListeners implements Listener {

    private final Eventsiav2 plugin;
    private final EventCommand eventCommand;

    public EventListeners(Eventsiav2 plugin, EventCommand eventCommand) {
        this.plugin = plugin;
        this.eventCommand = eventCommand;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (eventCommand.getInEvent().contains(event.getEntity().getUniqueId())) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> event.getEntity().setGameMode(GameMode.SPECTATOR), 1L);
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if (eventCommand.getInEvent().contains(event.getPlayer().getUniqueId())) {
            event.getPlayer().setGameMode(GameMode.SPECTATOR);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

     // Only send message to OPs
        if (player.isOp()) {
            player.sendMessage(ChatColor.GRAY + "------------------ " +
                    ChatColor.AQUA + ChatColor.BOLD + "ᴇᴠᴇɴᴛѕɪᴀ" + ChatColor.GRAY + " ------------------");
            player.sendMessage(ChatColor.GREEN + "➤ " + ChatColor.WHITE +
                    "ʏᴏᴜ ɴᴇᴇᴅ ᴛᴏ ᴅᴏ " + ChatColor.AQUA + ChatColor.BOLD + "⁄ᴇᴠᴇɴᴛ ѕᴇᴛᴄᴏᴏʀᴅѕ" + ChatColor.WHITE +
                    " ᴀɴᴅ " + ChatColor.AQUA + ChatColor.BOLD + "⁄ᴇᴠᴇɴᴛ ѕᴇᴛᴡᴏʀʟᴅ");
            player.sendMessage(ChatColor.GREEN + "➤ " + ChatColor.WHITE +
                    "ʙᴇꜰᴏʀᴇ ʏᴏᴜ ᴅᴏ " + ChatColor.AQUA + ChatColor.BOLD + "⁄ᴇᴠᴇɴᴛ ѕᴛᴀʀᴛ" + ChatColor.WHITE +
                    " ᴀɴᴅ " + ChatColor.AQUA + ChatColor.BOLD + "⁄ᴇᴠᴇɴᴛ!");
            player.sendMessage(ChatColor.GRAY + "------------------------------------------------");

            // Play firework twinkle sound
            player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 1.0f, 1.0f);
        }
    }
}
