package dev.slava7777.ventity.entity.tracker;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.world.Location;
import dev.slava7777.ventity.entity.VirtualEntity;
import dev.slava7777.ventity.utils.UUIDUtil;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

public final class VirtualEntityTracker {

    private static final double DEFAULT_VIEW_DISTANCE = 32.0;
    private static final int DEFAULT_TICK_PERIOD = 1;

    private final Plugin plugin;
    private final double viewDistanceSq;
    private final int tickPeriod;

    private final Long2ObjectMap<VirtualEntity> entities = new Long2ObjectOpenHashMap<>();
    private final CopyOnWriteArrayList<VirtualEntity> entityList = new CopyOnWriteArrayList<>();
    private final Long2ObjectMap<List<VirtualEntity>> entitiesByWorld = new Long2ObjectOpenHashMap<>();

    private final Long2ObjectMap<PlayerState> playerStates = new Long2ObjectOpenHashMap<>();
    private final Long2ObjectMap<User> userCache = new Long2ObjectOpenHashMap<>();

    private volatile boolean forceRecompute = true;

    private ScheduledTask task;

    public VirtualEntityTracker(@NotNull Plugin plugin) {
        this(plugin, DEFAULT_VIEW_DISTANCE, DEFAULT_TICK_PERIOD);
    }

    public VirtualEntityTracker(@NotNull Plugin plugin, double viewDistance, int tickPeriod) {
        this.plugin = plugin;
        this.viewDistanceSq = viewDistance * viewDistance;
        this.tickPeriod = tickPeriod;

        for (Player p : Bukkit.getOnlinePlayers()) {
            cacheUser(p);
        }
    }

    public void cacheUser(@NotNull Player player) {
        User user = PacketEvents.getAPI().getPlayerManager().getUser(player);
        if (user != null) userCache.put(UUIDUtil.key(player.getUniqueId()), user);
        forceRecompute = true;
    }

    public void uncacheUser(@NotNull Player player) {
        long id = UUIDUtil.key(player.getUniqueId());
        User removed = userCache.remove(id);
        playerStates.remove(id);
        if (removed == null) return;

        for (VirtualEntity entity : entityList) {
            if (entity.isRemoved()) continue;
            entity.getViewers().clearViewer(removed);
        }
        forceRecompute = true;
    }

    public void start() {
        if (task != null) return;
        task = Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, _ -> tick(), tickPeriod, tickPeriod);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public void register(@NotNull VirtualEntity entity) {
        if (entities.putIfAbsent(entity.getEntityId(), entity) != null) return;
        entityList.add(entity);
        entity.markPositionDirty();
    }

    public void unregister(@NotNull VirtualEntity entity) {
        if (entities.remove(entity.getEntityId()) == null) return;
        entityList.remove(entity);
        entity.remove();
    }

    public @NotNull Collection<VirtualEntity> getEntities() {
        return Collections.unmodifiableList(entityList);
    }

    private void tick() {
        if (entityList.isEmpty()) return;

        groupEntitiesByWorld();
        if (entitiesByWorld.isEmpty()) return;

        boolean anyPlayerMoved = false;

        for (World world : Bukkit.getWorlds()) {
            final long worldKey = UUIDUtil.key(world.getUID());
            final List<VirtualEntity> worldEntities = entitiesByWorld.get(worldKey);

            if (worldEntities == null || worldEntities.isEmpty()) continue;

            final List<Player> players = world.getPlayers();

            if (players.isEmpty()) {
                tickEntitiesOnly(worldEntities);
                continue;
            }

            if (tickWorldWithPlayers(worldEntities, players)) {
                anyPlayerMoved = true;
            }
        }

        if (anyPlayerMoved) {
            forceRecompute = false;
        } else if (forceRecompute) {
            forceRecompute = false;
        }
    }

    private void tickEntitiesOnly(@NotNull List<VirtualEntity> worldEntities) {
        for (VirtualEntity entity : worldEntities) {
            if (entity.isRemoved() || !entity.isActive()) continue;
            try {
                entity.consumePositionDirty();
                entity.tick();
            } catch (Throwable t) {
                plugin.getLogger().log(Level.SEVERE,
                        "Error ticking virtual entity " + entity.getEntityId(), t);
            }
        }
    }

    private boolean tickWorldWithPlayers(@NotNull List<VirtualEntity> worldEntities,
                                         @NotNull List<Player> players) {
        final int count = players.size();
        final double[] px = new double[count];
        final double[] pz = new double[count];
        final User[] users = new User[count];

        boolean anyMoved = false;

        for (int i = 0; i < count; i++) {
            Player player = players.get(i);
            org.bukkit.Location location = player.getLocation();
            px[i] = location.getX();
            pz[i] = location.getZ();

            long key = UUIDUtil.key(player.getUniqueId());

            PlayerState state = playerStates.get(key);
            if (state == null) {
                state = new PlayerState(px[i], pz[i]);
                playerStates.put(key, state);
                anyMoved = true;
            } else if (state.lastX != px[i] || state.lastZ != pz[i]) {
                state.lastX = px[i];
                state.lastZ = pz[i];
                anyMoved = true;
            }

            User user = userCache.get(key);
            if (user == null) {
                user = PacketEvents.getAPI().getPlayerManager().getUser(player);
                if (user != null) userCache.put(key, user);
            }
            users[i] = user;
        }

        final boolean globalDirty = anyMoved || forceRecompute;

        for (VirtualEntity entity : worldEntities) {
            if (entity.isRemoved() || !entity.isActive()) continue;

            try {
                boolean entityMoved = entity.consumePositionDirty();
                if (globalDirty || entityMoved) {
                    updateAutoViewers(entity, px, pz, users, count);
                }
                entity.tick();
            } catch (Throwable t) {
                plugin.getLogger().log(Level.SEVERE,
                        "Error ticking virtual entity " + entity.getEntityId(), t);
            }
        }

        return anyMoved;
    }

    private void groupEntitiesByWorld() {
        for (var it = entitiesByWorld.values().iterator(); it.hasNext(); ) {
            it.next().clear();
        }

        for (VirtualEntity entity : entityList) {
            if (entity.isRemoved() || !entity.isActive()) continue;

            UUID worldId = entity.getWorldId();
            if (worldId == null) continue;

            long key = UUIDUtil.key(worldId);
            List<VirtualEntity> list = entitiesByWorld.get(key);
            if (list == null) {
                list = new ArrayList<>();
                entitiesByWorld.put(key, list);
            }
            list.add(entity);
        }
    }

    private void updateAutoViewers(@NotNull VirtualEntity entity,
                                   double[] px, double[] pz,
                                   User[] users, int count) {
        final Location loc = entity.getRawLocation();
        final double ex = loc.getX(), ez = loc.getZ();

        for (int i = 0; i < count; i++) {
            final User user = users[i];
            if (user == null) continue;

            final double dx = px[i] - ex;
            final double dz = pz[i] - ez;

            if (dx * dx + dz * dz <= viewDistanceSq) {
                entity.addAutoViewer(user);
            } else {
                entity.removeAutoViewer(user);
            }
        }
    }

    public int size() {
        return entities.size();
    }

    private static final class PlayerState {
        double lastX;
        double lastZ;

        PlayerState(double x, double z) {
            this.lastX = x;
            this.lastZ = z;
        }
    }
}