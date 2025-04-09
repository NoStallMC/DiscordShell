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
        //Don't respond to bots
        if (event.getAuthor().isBot() || event.isWebhookMessage()) {
            return;
        }
        //Don't respond to funky messages
        if (event.getMessage().getContentRaw().isEmpty()) {
            return;
        }
        String content = event.getMessage().getContentRaw();
        //Server Shell Command Execution- ~jkoo
        if (plugin.getConfig().getConfigBoolean("server-shell.enabled") && event.getChannel().getId().equals(plugin.getConfig().getConfigString("server-shell.shell-channel-id"))){
        	List<?> rawList = (List<?>) plugin.getConfig().getConfigOption("server-shell.allowed-users");
        	List<String> allowedUsers = new ArrayList<String>();
        	for (Object obj : rawList) {
        	    allowedUsers.add(obj.toString());
        	}
            //Check if the sender is authorized
            if (!allowedUsers.contains(event.getAuthor().getId())) {
                event.getChannel().sendMessage(":no_entry_sign: You are not authorized to use the server-shell.").queue();
                return;
            }
            //Blacklist check
            BlacklistManager blacklist = plugin.getBlacklistManager();
            if (blacklist.isCommandBlacklisted(content)) {
            	if (plugin.getConfig().getConfigBoolean("blacklist")) {
                event.getChannel().sendMessage(":no_entry_sign: This command is blacklisted and cannot be executed.").queue();
                return;
            	}
            }
            //Command to execute
            String command = event.getMessage().getContentRaw();
            //Log the command for debugging
            Bukkit.getLogger().info("[DiscordShell] executing command: " + command);
            //Execute the command
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                boolean result = Bukkit.dispatchCommand(new ServerShellSender(), command);
                if (result) {
                    event.getChannel().sendMessage(":white_check_mark: Executed: `" + command + "`").queue();
                } else {
                    event.getChannel().sendMessage(":x: Unknown or failed command: `" + command + "`").queue();
                }
            });
        }
	}
}