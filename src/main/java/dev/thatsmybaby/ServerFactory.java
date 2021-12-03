package dev.thatsmybaby;

import dev.thatsmybaby.utils.Priorities;
import dev.thatsmybaby.utils.ServerStatus;
import lombok.Getter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ServerFactory {

    @Getter
    private final static ServerFactory instance = new ServerFactory();

    private final Map<String, Map<String, Object>> serversMap = new HashMap<>();
    @Getter
    private final List<String> servers = new ArrayList<>();

    private final Map<String, ServerStatus> serverStatusMap = new ConcurrentHashMap<>();

    public void init() throws IOException {
        File file = new File(LobbySwitcher.getInstance().getDataFolder(), "servers.yml");

        Configuration configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);

        for (String category : configuration.getKeys()) {
            Configuration section = configuration.getSection(category);

            Map<String, Object> sections = new HashMap<>();

            for (String s : section.getKeys()) {
                sections.put(s, section.getStringList(s));
            }

            this.servers.addAll(section.getStringList("fallbacks"));

            this.serversMap.put(category, sections);
        }

        ProxyServer.getInstance().getLogger().info("Starting the ping task, the interval is 5 seconds");

        ProxyServer.getInstance().getScheduler().schedule(LobbySwitcher.getInstance(), () -> {
            for (String s : ServerFactory.getInstance().getServers()) {
                update(ProxyServer.getInstance().getServerInfo(s));
            }
        }, 0L, 10000, TimeUnit.MILLISECONDS);
    }

    @SuppressWarnings("unchecked")
    public void sendToServerAvailable(ProxiedPlayer player) {
        Server server = player.getServer();

        if (server == null) {
            player.sendMessage(new TextComponent(LobbySwitcher.getInstance().translateString("SERVERS_NOT_FOUND")));

            return;
        }

        Map<String, Object> map = this.serversMap.values().stream().filter(storage -> ((List<String>) storage.get("servers")).contains(server.getInfo().getName())).findFirst().orElse(new HashMap<>());

        if (map.isEmpty()) {
            player.sendMessage(new TextComponent(LobbySwitcher.getInstance().translateString("SERVERS_NOT_FOUND")));

            return;
        }

        List<String> fallbacks = (List<String>) map.get("fallbacks");

        if (fallbacks.isEmpty()) {
            player.sendMessage(new TextComponent(LobbySwitcher.getInstance().translateString("SERVERS_NOT_FOUND")));

            return;
        }

        sendToFallback(player, fallbacks, (String) map.getOrDefault("priority", "NORMAL"));
    }

    protected void sendToFallback(ProxiedPlayer player, List<String> fallbacks, String priority) {
        if (fallbacks.isEmpty()) {
            player.sendMessage(new TextComponent(LobbySwitcher.getInstance().translateString("SERVERS_NOT_FOUND")));

            return;
        }

        List<ServerStatus> list = new ArrayList<>();

        for (String s : fallbacks) {
            ServerStatus serverStatus = this.serverStatusMap.get(s);

            if (serverStatus == null) {
                serverStatus = new ServerStatus(0, 0, false, "");
            }

            update(ProxyServer.getInstance().getServerInfo(s));

            list.add(serverStatus);
        }

        ServerStatus serverStatus = Priorities.getServerAvailable(list.stream().filter(ServerStatus::isOnline).collect(Collectors.toList()), priority);

        if (serverStatus == null) {
            player.sendMessage(new TextComponent(LobbySwitcher.getInstance().translateString("SERVERS_NOT_FOUND")));

            return;
        }

        ServerInfo serverInfo = ProxyServer.getInstance().getServerInfo(serverStatus.getServerName());

        player.connect(serverInfo);

        player.sendMessage(new TextComponent(LobbySwitcher.getInstance().translateString("CONNECTING_TO", "<server>", serverInfo.getName())));
    }

    private void update(ServerInfo serverInfo) {
        serverInfo.ping((serverPing, throwable) -> {
            int online = 0;
            int max = 0;

            if (serverPing != null) {
                online = serverPing.getPlayers().getOnline();

                max = serverPing.getPlayers().getMax();
            }

            ServerStatus serverStatus = new ServerStatus(online, max, throwable == null, serverInfo.getName());

            this.serverStatusMap.put(serverInfo.getName(), serverStatus);
        });
    }
}