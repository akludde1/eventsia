package com.akludde.eventsiav2;

import com.akludde.eventsiav2.commands.EventCommand;
import com.akludde.eventsiav2.listeners.EventListeners;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Eventsiav2 extends JavaPlugin {

    private EventCommand eventCommand;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.eventCommand = new EventCommand(this);
        getCommand("event").setExecutor(eventCommand);
        getCommand("event").setTabCompleter(eventCommand);

        Bukkit.getPluginManager().registerEvents(new EventListeners(this, eventCommand), this);

        sendAsciiBanner();
        getLogger().info("Eventsiav2 plugin enabled!");
    }

    private void sendAsciiBanner() {
        String[] banner = {
                "███████╗██╗░░░██╗███████╗███╗░░██╗████████╗░██████╗██╗░█████╗░",
                "██╔════╝██║░░░██║██╔════╝████╗░██║╚══██╔══╝██╔════╝██║██╔══██╗",
                "█████╗░░╚██╗░██╔╝█████╗░░██╔██╗██║░░░██║░░░╚█████╗░██║███████║",
                "██╔══╝░░░╚████╔╝░██╔══╝░░██║╚████║░░░██║░░░░╚═══██╗██║██╔══██║",
                "███████╗░░╚██╔╝░░███████╗██║░╚███║░░░██║░░░██████╔╝██║██║░░██║",
                "╚══════╝░░░╚═╝░░░╚══════╝╚═╝░░╚══╝░░░╚═╝░░░╚═════╝░╚═╝╚═╝░░╚═╝"
        };
        for (String line : banner) getServer().getConsoleSender().sendMessage(line);
    }

    public EventCommand getEventCommand() {
        return eventCommand;
    }
}
