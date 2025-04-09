package main.java.org.matejko.discordshell;

import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DiscordShell extends JavaPlugin implements Listener {
    private static DiscordShell plugin;
    private Logger log;
    private String pluginName;
    private PluginDescriptionFile pdf;
    private DiscordBot discordBot;
    private Config config;
    private DiscordListener discordListener;
    private Integer taskID = null;
    private BlacklistManager blacklistManager;
    
    @Override
    public void onEnable() {
        plugin = this;
        log = this.getServer().getLogger();
        pdf = this.getDescription();
        pluginName = pdf.getName();
        log.info("[DiscordShell] is starting up!");
        this.blacklistManager = new BlacklistManager(this);
        config = new Config(plugin);
        // Load Token from config
        String softToken = config.getString("token", "INSERT_TOKEN_HERE");
        if (softToken == null || softToken.equalsIgnoreCase("INSERT_TOKEN_HERE") || softToken.isEmpty()) {
            logInfo(Level.WARNING, "Failed to find a Discord token in the config file, shutting down.");
            Bukkit.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }
        // Parse intents from config
        List<String> rawIntentList = config.getStringList("intents", Arrays.asList("GUILD_MEMBERS", "DIRECT_MESSAGES", "MESSAGE_CONTENT"));
        ArrayList<GatewayIntent> intents = new ArrayList<>();
        for (String str : rawIntentList) {
            GatewayIntent intent = null;
            try {
                intent = GatewayIntent.valueOf(str);
            } catch (IllegalArgumentException ignored) {}
            if (intent != null) intents.add(intent);
        }
        // Start Discord Bot
        logInfo(Level.INFO, "Starting internal Discord Bot.");
        try {
            discordBot = new DiscordBot(this);
            discordBot.startBot(softToken, intents);
        } catch (Exception e) {
            logInfo(Level.WARNING, e + ": " + e.getMessage());
            Bukkit.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }
        discordListener = new DiscordListener(plugin);
        discordBot.jda.addEventListener(discordListener);
    }
    @Override
    public void onDisable() {
        logInfo(Level.INFO, "Disabling plugin.");
        if (discordBot != null) {
            discordBot.jda.removeEventListener(discordListener);
            // Check if taskID is not null before cancelling the task
            if (taskID != null) {
                Bukkit.getServer().getScheduler().cancelTask(taskID);
            }
            
            discordBot.discordBotStop();
        }
        logInfo(Level.INFO, "Disabled.");
    }

    public void logInfo(Level level, String s) {
        log.log(level, "[" + pluginName + "] " + s);
    }
    public Config getConfig() {
        return config;
    }
    public DiscordBot getDiscordBot() {
        return discordBot;
    }
    public BlacklistManager getBlacklistManager() {
        return this.blacklistManager;
    }
    // The DiscordBot class from DiscordCore
    public static class DiscordBot {
        private DiscordShell plugin;
        public net.dv8tion.jda.api.JDA jda;
        public DiscordBot(DiscordShell main) {
            this.plugin = main;
        }
        public void startBot(String token, ArrayList<GatewayIntent> intents) throws javax.security.auth.login.LoginException {
            jda = net.dv8tion.jda.api.JDABuilder.createDefault(token).enableIntents(intents).setMemberCachePolicy(net.dv8tion.jda.api.utils.MemberCachePolicy.ALL).build();
            jda.addEventListener(new net.dv8tion.jda.api.hooks.ListenerAdapter() {
                @Override
                public void onReady(net.dv8tion.jda.api.events.ReadyEvent event) {
                    plugin.logInfo(Level.INFO, "Discord Bot (" + event.getJDA().getSelfUser().getName() + "#" +
                            event.getJDA().getSelfUser().getDiscriminator() + ") connected to " +
                            event.getGuildTotalCount() + " guilds.");
                }
            });
        }
        public void discordBotStop() {
            plugin.logInfo(Level.INFO, "Discord Bot shutting down.");
            if (jda != null) {
                jda.shutdownNow();
            }
        }
        public void discordSendToChannel(String channel, String message) {
            if (jda.getStatus() == net.dv8tion.jda.api.JDA.Status.CONNECTED) {
                net.dv8tion.jda.api.entities.TextChannel textChannel = jda.getTextChannelById(channel);
                if (textChannel != null) {
                    textChannel.sendMessage(message).queue();
                } else {
                    plugin.logInfo(Level.WARNING, "Invalid channel ID or bot not connected.");
                }
            } else {
                plugin.logInfo(Level.WARNING, "Message unable to send; Discord bot not yet connected.");
            }
        }
    }
}
