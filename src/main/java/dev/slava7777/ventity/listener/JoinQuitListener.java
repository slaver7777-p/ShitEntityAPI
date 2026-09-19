package dev.slava7777.ventity.listener;

import dev.slava7777.ventity.entity.tracker.VirtualEntityTracker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinQuitListener implements Listener {

    private final VirtualEntityTracker tracker;

    public JoinQuitListener(VirtualEntityTracker tracker) {
        this.tracker = tracker;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        tracker.cacheUser(event.getPlayer());
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        tracker.uncacheUser(event.getPlayer());
    }
}
