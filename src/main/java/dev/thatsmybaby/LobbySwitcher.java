package dev.thatsmybaby;

import com.google.common.io.ByteStreams;
import dev.thatsmybaby.command.LobbyCommand;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class LobbySwitcher extends Plugin {

    @Getter
    private static LobbySwitcher instance;

    private final Map<String, String> messages = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;

        this.saveDefaultConfig();

        try {
            ServerFactory.getInstance().init();

            Configuration configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(this.getDataFolder(), "messages.yml"));

            for (String s : configuration.getKeys()) {
                this.messages.put(s, configuration.getString(s));
            }

            getProxy().getPluginManager().registerCommand(this, new LobbyCommand("lobby", null, "hub", "leave"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String translateString(String key, String... values) {
        String message = this.messages.getOrDefault(key, key);

        if (values.length >= 2) {
            for (int i = 0; i < values.length; i += 2) {
                try {
                    String k = values[i];
                    String v = values[i + 1];

                    message = message.replaceAll(k, v);
                } catch (Exception e) {
                    return "";
                }
            }
        }

        return ChatColor.translateAlternateColorCodes('&', message);
    }

    @SuppressWarnings({"UnstableApiUsage", "ResultOfMethodCallIgnored"})
    private void saveDefaultConfig() {
        if (!this.getDataFolder().exists()) {
            this.getDataFolder().mkdir();
        }

        for (String fileName : new String[]{"messages", "servers"}) {

            File file = new File(this.getDataFolder(), fileName + ".yml");

            if (file.exists()) {
                return;
            }

            try {
                file.createNewFile();

                try (InputStream is = this.getResourceAsStream( fileName + ".yml");
                     OutputStream os = new FileOutputStream(file)) {
                    ByteStreams.copy(is, os);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}