package dev.slava7777.ventity.entity;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import dev.slava7777.ventity.entity.tracker.ViewerSet;
import dev.slava7777.ventity.utils.cached.LazyPacket;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VirtualEntity {

    private final EntityType entityType;
    private final UUID uuid;
    private final int entityId;

    private volatile Location location;
    private volatile @Nullable UUID worldId;
    private volatile float bodyYaw;
    private volatile float headYaw = 0f;
    private volatile float pitch;
    private volatile boolean onGround = true;

    private final ViewerSet viewers = new ViewerSet(this);

    private final WrapperPlayServerDestroyEntities destroyPacket;
    private final LazyPacket<WrapperPlayServerSpawnEntity> spawnPacket;

    private volatile boolean active;
    private volatile boolean removed;
    private volatile boolean positionDirty = true;

    private List<EntityData<?>> metadata = new ArrayList<>();

    public VirtualEntity(UUID uuid, EntityType entityType) {
        this.uuid = uuid;
        this.entityType = entityType;
        this.entityId = generateId();
        this.location = new Location(Vector3d.zero(), 0f, 0f);

        this.bodyYaw = 0f;
        this.headYaw = 0f;
        this.pitch = 0f;

        this.destroyPacket = new WrapperPlayServerDestroyEntities(entityId);
        this.spawnPacket = new LazyPacket<>(this::createSpawnPacket);
    }

    public VirtualEntity(EntityType entityType) {
        this(UUID.randomUUID(), entityType);
    }


    public final void setInstance(@NotNull UUID worldId, @NotNull Location location) {
        if (active && (this.worldId == null || !this.worldId.equals(worldId))) {
            sendPacketToViewers(destroyPacket);
        }

        this.worldId = worldId;
        this.location = location.clone();
        this.bodyYaw = location.getYaw();
        this.headYaw = location.getYaw();
        this.pitch = location.getPitch();
        this.positionDirty = true;

        spawnPacket.invalidate();

        if (!active) {
            active = true;
            removed = false;
        }

        spawn();

        for (User user : viewers.snapshot()) {
            sendSpawnPackets(user);
        }
    }

    public final void sendPacketToViewers(@NotNull PacketWrapper<?> packet) {
        if (viewers.isEmpty()) return;
        for (User user : viewers.snapshot()) {
            user.sendPacket(packet);
        }
    }

    public void tick() {
        // no-op
    }

    public void spawn() {

    }

    public void remove() {
        if (removed) return;
        removed = true;
        active = false;

        sendPacketToViewers(destroyPacket);
        viewers.clearSilent();
        spawnPacket.invalidate();
        worldId = null;
    }

    protected @NotNull WrapperPlayServerSpawnEntity createSpawnPacket() {
        Location loc = this.location;
        return new WrapperPlayServerSpawnEntity(
                entityId,
                uuid,
                entityType,
                loc.clone(),
                headYaw,
                0,
                Vector3d.zero()
        );
    }

    public void sendSpawnPackets(@NotNull User user) {
        user.sendPacket(getSpawnPacket());
        user.sendPacket(new WrapperPlayServerEntityHeadLook(entityId, headYaw));
        WrapperPlayServerEntityMetadata metadataPacket = getMetadataPacket();
        if (metadataPacket == null) return;
        user.sendPacket(metadataPacket);
    }

    public void setRotation(float yaw, float pitch) {
        boolean changed = false;
        if (Math.abs(this.bodyYaw - yaw) > 0.01f) {
            this.bodyYaw = yaw;
            this.headYaw = yaw;
            spawnPacket.invalidate();
            changed = true;
        }
        if (Math.abs(this.pitch - pitch) > 0.01f) {
            this.pitch = pitch;
            spawnPacket.invalidate();
            changed = true;
        }
        if (!changed || !active || viewers.isEmpty()) return;

        sendPacketToViewers(new WrapperPlayServerEntityRotation(entityId, this.bodyYaw, this.pitch, onGround));
        sendPacketToViewers(new WrapperPlayServerEntityHeadLook(entityId, this.headYaw));
    }

    public void onViewerAdded(@NotNull User user) {
        if (!isActive()) return;
        sendSpawnPackets(user);
    }

    public void onViewerRemoved(@NotNull User user) {
        if (!isActive()) return;
        user.sendPacket(getDestroyPacket());
    }

    public void setLocation(@NotNull Location newLocation) {
        this.location = newLocation.clone();
        this.positionDirty = true;
        spawnPacket.invalidate();

        if (active && !viewers.isEmpty()) {
            sendPacketToViewers(new WrapperPlayServerEntityTeleport(entityId, newLocation.clone(), onGround));
        }
    }

    public void setHeadYaw(float headYaw) {
        if (Math.abs(this.headYaw - headYaw) < 0.01f) return;
        this.headYaw = headYaw;
        if (active && !viewers.isEmpty()) {
            sendPacketToViewers(new WrapperPlayServerEntityHeadLook(getEntityId(), headYaw));
        }
    }

    public void lookAt(@NotNull Vector3d target) {
        double dx = target.getX() - location.getX();
        double dy = target.getY() - location.getY();
        double dz = target.getZ() - location.getZ();

        double horizontal = Math.sqrt(dx * dx + dz * dz);

        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = (float) Math.toDegrees(-Math.atan2(dy, horizontal));

        setRotation(yaw, pitch);
    }

    public void lookAt(@NotNull VirtualEntity entity) {
        if (this.worldId != null && entity.worldId != null
                && !this.worldId.equals(entity.worldId)) {
            throw new IllegalArgumentException("Cannot lookAt across worlds");
        }
        final Location targetLoc = entity.getRawLocation();
        final Vector3d target = targetLoc.getPosition();
        lookAt(target);
    }

    public final @Nullable WrapperPlayServerEntityMetadata getMetadataPacket() {
        if (metadata.isEmpty()) return null;
        return new WrapperPlayServerEntityMetadata(getEntityId(), metadata);
    }


    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }

    public boolean isOnGround() {
        return onGround;
    }

    public boolean consumePositionDirty() {
        boolean dirty = positionDirty;
        positionDirty = false;
        return dirty;
    }

    public void markPositionDirty() {
        this.positionDirty = true;
    }

    public boolean addViewer(@NotNull Player player) {
        User user = PacketEvents.getAPI().getPlayerManager().getUser(player);
        return user != null && addViewer(user);
    }

    public boolean addViewer(@NotNull User user) {
        return viewers.addManual(user);
    }

    public boolean removeViewer(@NotNull Player player) {
        User user = PacketEvents.getAPI().getPlayerManager().getUser(player);
        return user != null && removeViewer(user);
    }

    public boolean removeViewer(@NotNull User user) {
        return viewers.removeManual(user);
    }

    public boolean hasViewer(@NotNull User user) {
        return viewers.isViewer(user);
    }

    public void addAutoViewer(@NotNull User user) {
        viewers.addAuto(user);
    }

    public void removeAutoViewer(@NotNull User user) {
        viewers.removeAuto(user);
    }

    public final @NotNull WrapperPlayServerSpawnEntity getSpawnPacket() {
        return spawnPacket.get();
    }

    public final @NotNull WrapperPlayServerDestroyEntities getDestroyPacket() {
        return destroyPacket;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isRemoved() {
        return removed;
    }

    public float getHeadYaw() {
        return headYaw;
    }

    public Location getRawLocation() {
        return location;
    }

    public Location getLocation() {
        return location.clone();
    }

    public @Nullable UUID getWorldId() {
        return worldId;
    }

    public ViewerSet getViewers() {
        return viewers;
    }

    public int generateId() {
        return SpigotReflectionUtil.generateEntityId();
    }

    public int getEntityId() {
        return entityId;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setMetadata(List<EntityData<?>> metadata) {
        this.metadata = metadata;
    }

    public void addMetadata(EntityData<?> metadata) {
        this.metadata.add(metadata);
    }

    public @Nullable EntityData<?> getMetadata(int index) {
        return metadata.get(index);
    }

    public List<EntityData<?>> getMetadata() {
        return metadata;
    }

    public UUID getUuid() {
        return uuid;
    }
}
