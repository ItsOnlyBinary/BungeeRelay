package io.github.dead_i.bungeerelay;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.*;

public class Util {
    private static ProxyServer proxy = ProxyServer.getInstance();

    public static void incrementUid(int pos) {
        StringBuilder sb = new StringBuilder(IRC.currentUid);
        if (IRC.currentUid.charAt(pos) == 'Z') {
            sb.setCharAt(pos, '0');
            IRC.currentUid = sb.toString();
        } else if (IRC.currentUid.charAt(pos) == '9') {
            sb.setCharAt(pos, 'A');
            IRC.currentUid = sb.toString();
            if (pos == 3) return;
            incrementUid(pos - 1);
        } else {
            sb.setCharAt(pos, (char) (IRC.currentUid.charAt(pos) + 1));
            IRC.currentUid = sb.toString();
        }
    }

    public static void incrementUid() {
        do {
            incrementUid(8);
        } while (IRC.uids.containsValue(IRC.currentUid));
    }

    public static void sendUserConnect(ProxiedPlayer p) {
        String name = IRC.config.getString("server.userprefix") + p.getName() + IRC.config.getString("server.usersuffix");
        IRC.times.put(p, System.currentTimeMillis() / 1000);
        IRC.uids.put(p, IRC.currentUid);
        IRC.users.put(IRC.currentUid, name);
        IRC.out.println("UID " + IRC.currentUid + " " + System.currentTimeMillis() / 1000 + " " + name + " " + p.getAddress().getHostName() + " " + p.getAddress().getHostName() + " " + p.getName() + " " + p.getAddress().getHostString() + " " + IRC.times.get(p) + " +r :Minecraft Player");
    }

    public static void sendChannelJoin(ProxiedPlayer p, String c) {
        String uid = IRC.uids.get(p);
        IRC.out.println("FJOIN " + c + " " + System.currentTimeMillis() / 1000 + " +nt :," + uid);
        giveChannelModes(p, c);
    }

    public static void giveChannelModes(ProxiedPlayer p, String c) {
        String modes = "+";
        if (p.hasPermission("irc.owner")) modes += "";
        if (p.hasPermission("irc.protect")) modes += "a";
        if (p.hasPermission("irc.op")) modes += "o";
        if (p.hasPermission("irc.halfop")) modes += "h";
        if (p.hasPermission("irc.voice")) modes += "v";
        giveChannelModes(c, modes, IRC.uids.get(p));
    }

    public static void giveChannelModes(String c, String m, String s) {
        if (!m.isEmpty()) {
            String target = "";
            for (int i=0; i<m.length(); i++) {
                target += s + " ";
            }
            giveChannelModes(c, "+" + m + " " + target.trim());
        }
    }

    public static boolean giveChannelModes(String channel, String m) {
        String modes = m.split(" ")[0];
        for (int i=0; i<modes.length(); i++) {
            String mode = Character.toString(modes.charAt(i));
            if (!IRC.prefixModes.contains(mode) && !IRC.chanModes.contains(mode) && !mode.equals("+") && !mode.equals("-")) {
                proxy.getLogger().warning("Tried to set the +" + mode + " mode, but the IRC server stated earlier that it wasn't compatible with this mode.");
                proxy.getLogger().warning("If you want to use " + mode + ", enable appropriate module in your IRC server's configuration files.");
                proxy.getLogger().warning("Skipping...");
                return false;
            }
        }
        IRC.out.println(":" + IRC.SID + " FMODE " + channel + " " + getChanTS(channel) + " " + m);
        return true;
    }

    public static void sendMainJoin(String c, String m, String t) {
        long chanTS = getChanTS(c);
        IRC.out.println("FJOIN " + c + " " + chanTS + " +nt :," + IRC.botUID);

        giveChannelModes(c, m, IRC.botUID);

        if (!t.isEmpty()) IRC.out.println(":" + IRC.SID + " TOPIC " + c + " :" + t);
    }

    public static Collection<ProxiedPlayer> getPlayersByChannel(String c) {
        return proxy.getPlayers();
    }

    public static String getUidByNick(String nick) {
        for (Map.Entry<String, String> entry : IRC.users.entrySet()) {
            if (nick.equalsIgnoreCase(entry.getValue())) return entry.getKey();
        }
        return null;
    }

    public static Long getChanTS(String c) {
        if (!IRC.chans.containsKey(c)) IRC.chans.put(c, new Channel(System.currentTimeMillis() / 1000));
        return IRC.chans.get(c).ts;
    }
}
