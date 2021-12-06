package dev.thatsmybaby.command;

import dev.thatsmybaby.LobbySwitcher;
import dev.thatsmybaby.ServerFactory;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class LobbyCommand extends Command {

    public LobbyCommand(String name, String permission, String... aliases) {
        super(name, permission, aliases);
    }

    @Override
    public void execute(CommandSender commandSender, String[] args) {
        if (!(commandSender instanceof ProxiedPlayer)) {
            commandSender.sendMessage(new ComponentBuilder("Run this command in-game").color(ChatColor.RED).create());

            return;
        }

        if (args.length > 0 && commandSender.hasPermission("lobbyswitcher.admin")) {
            if (args[0].equalsIgnoreCase("send")) {
                if (args.length < 2) {
                    commandSender.sendMessage(new ComponentBuilder("Usage: /hub send <player>").color(ChatColor.RED).create());

                    return;
                }

                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(args[1]);

                if (player == null) {
                    commandSender.sendMessage(new ComponentBuilder(args[1] + " not found").color(ChatColor.RED).create());

                    return;
                }

                ServerFactory.getInstance().sendToServerAvailable(player);

                commandSender.sendMessage(new TextComponent(LobbySwitcher.getInstance().translateString("SENDING_PLAYER_TO_FALLBACK", "<player>", player.getName())));

                return;
            }

            if (args[0].equalsIgnoreCase("sendall")) {
                if (args.length < 2) {
                    commandSender.sendMessage(new ComponentBuilder("Usage: /hub sendall <server>").color(ChatColor.RED).create());

                    return;
                }

                ServerInfo serverInfo = ProxyServer.getInstance().getServerInfo(args[1]);

                if (serverInfo == null) {
                    commandSender.sendMessage(new ComponentBuilder("Server " + args[1] + " not found").color(ChatColor.RED).create());

                    return;
                }

                commandSender.sendMessage(new TextComponent(LobbySwitcher.getInstance().translateString("SENDALL_COMMAND", "<players_count>", String.valueOf(serverInfo.getPlayers().size()), "<server>", serverInfo.getName())));

                for (ProxiedPlayer player : serverInfo.getPlayers()) {
                    ServerFactory.getInstance().sendToServerAvailable(player);
                }

                return;
            }
        }

        ServerFactory.getInstance().sendToServerAvailable((ProxiedPlayer) commandSender);
    }
}