package chat.utils;

import chat.loggers.Loggers;
import chat.tcp.Group;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Usefullstuff {
    private static Usefullstuff INSTANCE;
    private final Map<String, Group> connectedGroups = new ConcurrentHashMap<>();
    private final Gson gson = new Gson();
    private String nickname = "X";
    private Group activeGroup = null;
    private ServerSocket serverSocket;
    private final int PORT = 7450;

    private Usefullstuff() {
        try {
            this.serverSocket = new ServerSocket(7401);
        } catch (IOException ex) {
            Loggers.errorLogger.error(ex.getMessage());
        }
    }

    public static Usefullstuff getINSTANCE() {
        if (INSTANCE == null) {
            INSTANCE = new Usefullstuff();
        }
        return INSTANCE;
    }

    public static StringBuilder data(final byte[] a) {
        if (a == null)
            return null;
        StringBuilder ret = new StringBuilder();
        int i = 0;
        while (a[i] != 0) {
            ret.append((char) a[i]);
            i++;
        }
        return ret;
    }

    public Gson getGson() {
        return gson;
    }

    public Map<String, Group> getConnectedGroups() {
        return connectedGroups;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Group getActiveGroup() {
        return activeGroup;
    }

    public void setActiveGroup(Group activeGroup) {
        if (this.activeGroup != null) {
            this.activeGroup.stopListeners();
        }

        this.activeGroup = activeGroup;

        if (activeGroup != null) {
            this.activeGroup.startListeners();
        }

        Loggers.infoLogger.info("Changed active group: " + activeGroup);
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public int getPORT() {
        return PORT;
    }
}
