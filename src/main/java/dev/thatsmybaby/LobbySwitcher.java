package dev.thatsmybaby;

import dev.thatsmybaby.command.LobbyCommand;
import lombok.Getter;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.IOException;

public class LobbySwitcher extends Plugin {

    @Getter
    private static LobbySwitcher instance;

    @Override
    public void onEnable() {
        instance = this;

        try {
            ServerFactory.getInstance().init();

            getProxy().getPluginManager().registerCommand(this, new LobbyCommand("lobby", null, "hub", "leave"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}