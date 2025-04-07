package main.java.org.matejko.plugin;

import org.bukkit.plugin.Plugin;
import org.bukkit.util.config.Configuration;
import java.io.File;
import java.util.Arrays;

public class Config extends Configuration {
    // Configuration options for the server shell
    public Config(Plugin plugin) {
        super(new File(plugin.getDataFolder(), "config.yml"));
        this.load();
        this.write();
        this.save();
    }
    private void write() {
        // Server shell configuration options
        generateConfigOption("server-shell.enabled", false);
        generateConfigOption("server-shell.shell-channel-id", "id");
        generateConfigOption("server-shell.allowed-users", Arrays.asList("id1", "id2"));
        generateConfigOption("server-shell.info", "If enabled, allows execution of server commands from Discord. Be VERY careful with this.");
        // Discord bot configuration options (added from ReduxConfig)
        generateConfigOption("token", "INSERT_TOKEN_HERE");
        generateConfigOption("intents", Arrays.asList("GUILD_MEMBERS", "DIRECT_MESSAGES", "MESSAGE_CONTENT"));
    }
    private void generateConfigOption(String key, Object defaultValue) {
        if (this.getProperty(key) == null) {
            this.setProperty(key, defaultValue);
        }
        final Object value = this.getProperty(key);
        this.removeProperty(key);
        this.setProperty(key, value);
    }
    // Getters for the configuration options
    public Object getConfigOption(String key) {
        return this.getProperty(key);
    }
    public String getConfigString(String key) {
        return String.valueOf(getConfigOption(key));
    }
    public Boolean getConfigBoolean(String key) {
        return Boolean.valueOf(getConfigString(key));
    }
}
