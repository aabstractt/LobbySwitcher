package dev.thatsmybaby.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ServerStatus {

    private final int onlineCount;
    private final int maxPlayers;
    private boolean online;
    private String serverName;
}