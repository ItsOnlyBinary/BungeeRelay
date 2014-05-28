package io.github.dead_i.bungeerelay.listeners;

import io.github.dead_i.bungeerelay.IRC;
import io.github.dead_i.bungeerelay.Util;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public class ServerConnectListener implements Listener {
    Plugin plugin;
    public ServerConnectListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onServerConnect(ServerConnectEvent event) {
        if (!IRC.sock.isConnected()) return;
        ProxiedPlayer p = event.getPlayer();
        String confchan = IRC.config.getString("server.channel");
        String c = confchan;
        if (c.isEmpty()) c = IRC.config.getString("server.chanprefix") + event.getTarget().getName();

        if (IRC.chans.get(c).isBanned(p) && IRC.config.getBoolean("server.ban")) {
            p.disconnect(new TextComponent(ChatColor.translateAlternateColorCodes('&', IRC.config.getString("formats.disconnectban"))));
            return;
        }

        if (confchan.isEmpty()) Util.sendChannelJoin(event.getPlayer(), c);
    }
}
