package dev.slava7777.ventity;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.util.Vector3d;
import dev.by1337.cmd.Command;
import dev.by1337.core.command.bcmd.CommandWrapper;
import dev.slava7777.ventity.entity.VirtualEntity;
import dev.slava7777.ventity.entity.tracker.VirtualEntityTracker;
import dev.slava7777.ventity.listener.JoinQuitListener;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public final class VirtualEntityCore extends JavaPlugin {

    private CommandWrapper cmdWrapper;
    private VirtualEntityTracker tracker;

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        PacketEvents.getAPI().init();

        this.tracker = new VirtualEntityTracker(this);
        this.tracker.start();

        Bukkit.getPluginManager().registerEvents(new JoinQuitListener(tracker), this);

        cmdWrapper = new CommandWrapper(createCommand(), this);
        cmdWrapper.setPermission("vea.use");
        cmdWrapper.register();
    }

    @Override
    public void onDisable() {
        tracker.stop();
        tracker.getEntities().forEach(entity -> tracker.unregister(entity));
        PacketEvents.getAPI().terminate();
        cmdWrapper.close();
    }

    private Command<CommandSender> createCommand() {
        return new Command<CommandSender>("vec")
                .aliases("virtualentitycore", "virtualentity")
                .sub(
                        new Command<CommandSender>("test")
                                .executor((s, args) -> {
                                    if (!(s instanceof Player player)) return;

                                    VirtualEntity entity = new VirtualEntity(EntityTypes.WARDEN);
                                    entity.setInstance(player.getWorld().getUID(), SpigotConversionUtil.fromBukkitLocation(player.getLocation()));
                                    tracker.register(entity);

                                    s.sendMessage("End crystal (entity id = " + entity.getEntityId() + ") spawned, total: " + tracker.size());
                                })
                )
                .sub(
                        new Command<CommandSender>("stress")
                                .executor((s, args) -> {
                                    if (!(s instanceof Player player)) return;

                                    final int total = 350;
                                    final double ringSpacing = 2.0;

                                    org.bukkit.Location origin = player.getLocation();
                                    final UUID worldId = player.getWorld().getUID();

                                    final double ox = origin.getX();
                                    final double oy = origin.getY();
                                    final double oz = origin.getZ();

                                    int spawned = 0;
                                    int ringIndex = 0;

                                    while (spawned < total) {
                                        ringIndex++;
                                        double radius = ringIndex * ringSpacing;
                                        double circumference = 2 * Math.PI * radius;
                                        int onRing = Math.max(1, (int) Math.round(circumference / ringSpacing));
                                        onRing = Math.min(onRing, total - spawned);

                                        for (int j = 0; j < onRing; j++) {
                                            double angle = 2 * Math.PI * j / onRing;
                                            double x = ox + Math.cos(angle) * radius;
                                            double z = oz + Math.sin(angle) * radius;

                                            Location entityLoc = new Location(
                                                    new Vector3d(x, oy, z), 0f, 0f);

                                            VirtualEntity entity = new VirtualEntity(EntityTypes.WARDEN);
                                            entity.setInstance(worldId, entityLoc);
                                            tracker.register(entity);
                                        }

                                        spawned += onRing;
                                    }

                                    s.sendMessage("Spawned " + total + " entities (rings), total: " + tracker.size());
                                })
                )
                .sub(
                        new Command<CommandSender>("clear")
                                .executor((s, args) -> {
                                    int count = tracker.size();
                                    for (VirtualEntity e : tracker.getEntities()) {
                                        tracker.unregister(e);
                                    }
                                    s.sendMessage("Removed " + count + " entities");
                                })
                );
    }
}
