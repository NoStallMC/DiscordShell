package main.java.org.matejko.discordshell;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import java.util.ArrayList;
import java.util.List;

public class DiscordListener extends ListenerAdapter {
    private DiscordShell plugin;
    public DiscordListener(DiscordShell plugin) {
        this.plugin = plugin;
    }
	@Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || event.isWebhookMessage()) {
            return;
        }
        if (event.getMessage().getContentRaw().isEmpty()) {
            return;
        }
        String content = event.getMessage().getContentRaw();
        if (plugin.getConfig().getConfigBoolean("server-shell.enabled") && event.getChannel().getId().equals(plugin.getConfig().getConfigString("server-shell.shell-channel-id"))){
        	List<?> rawList = (List<?>) plugin.getConfig().getConfigOption("server-shell.allowed-users");
        	List<String> allowedUsers = new ArrayList<String>();
        	for (Object obj : rawList) {
        	    allowedUsers.add(obj.toString());
        	}
            if (!allowedUsers.contains(event.getAuthor().getId())) {
                event.getChannel().sendMessage(":no_entry_sign: You are not authorized to use the server-shell.").queue();
                return;
            }
            BlacklistManager blacklist = plugin.getBlacklistManager();
            if (blacklist.isCommandBlacklisted(content)) {
            	if (plugin.getConfig().getConfigBoolean("blacklist")) {
                event.getChannel().sendMessage(":no_entry_sign: This command is blacklisted and cannot be executed.").queue();
                return;
            	}
            }
            String command = event.getMessage().getContentRaw();
            Bukkit.getLogger().info("[DiscordShell] executing command: " + command);
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                ServerShellSender sender = new ServerShellSender();
                boolean result = Bukkit.dispatchCommand(sender, content);
                List<String> output = sender.getOutput();

                if (!result || output.isEmpty()) {
                    event.getChannel().sendMessage(":x: Unknown or failed command: `" + content + "`").queue();
                    return;
                }

                StringBuilder builder = new StringBuilder(":white_check_mark: **Executed:** `" + content + "`\n```\n");
                for (int i = 0; i < Math.min(output.size(), 10); i++) {
                    builder.append(output.get(i)).append("\n");
                }
                if (output.size() > 10) {
                    builder.append("... (truncated)\n");
                }
                builder.append("```");

                event.getChannel().sendMessage(builder.toString()).queue();
            });
        }
	}
}
