package dev.thatsmybaby.utils;

import java.security.SecureRandom;
import java.util.List;

public class Priorities {

    public static ServerStatus getServerAvailable(List<ServerStatus> serverInfos, String priority) {
        if (priority.equals("LOWEST")) {
            return getServerAvailableLowest(serverInfos);
        }

        if (priority.equals("FILLER")) {
            return getServerAvailableFiller(serverInfos);
        }

        if (priority.equals("RANDOM")) {
            return getServerAvailableRandom(serverInfos);
        }

        return getServerAvailableNormal(serverInfos);
    }

    private static ServerStatus getServerAvailableNormal(List<ServerStatus> list) {
        for (ServerStatus serverStatus : list) {
            if (serverStatus.getOnlineCount() < serverStatus.getMaxPlayers()) {
                return serverStatus;
            }
        }

        return null;
    }

    private static ServerStatus getServerAvailableLowest(List<ServerStatus> list) {
        ServerStatus betterServer = null;

        for (ServerStatus serverStatus : list) {
            if (serverStatus.getOnlineCount() >= serverStatus.getMaxPlayers()) {
                continue;
            }

            if (betterServer == null) {
                betterServer = serverStatus;

                continue;
            }

            if (serverStatus.getOnlineCount() > betterServer.getOnlineCount()) {
                continue;
            }

            betterServer = serverStatus;
        }

        return betterServer;
    }

    private static ServerStatus getServerAvailableFiller(List<ServerStatus> list) {
        ServerStatus betterServer = null;

        for (ServerStatus serverStatus : list) {
            if (serverStatus.getOnlineCount() >= serverStatus.getMaxPlayers()) {
                continue;
            }

            if (betterServer == null) {
                betterServer = serverStatus;

                continue;
            }

            if (betterServer.getOnlineCount() > serverStatus.getOnlineCount()) {
                continue;
            }

            betterServer = serverStatus;
        }

        return betterServer;
    }

    private static ServerStatus getServerAvailableRandom(List<ServerStatus> list) {
        return list.get(new SecureRandom().nextInt(list.size()));
    }
}