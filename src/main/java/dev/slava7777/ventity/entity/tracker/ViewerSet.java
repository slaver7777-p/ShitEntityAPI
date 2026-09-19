package dev.slava7777.ventity.entity.tracker;

import com.github.retrooper.packetevents.protocol.player.User;
import dev.slava7777.ventity.entity.VirtualEntity;
import dev.slava7777.ventity.utils.UUIDUtil;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class ViewerSet extends AbstractSet<User> {

    private static final byte AUTO = 1;
    private static final byte MANUAL = 2;

    private final VirtualEntity entity;

    private final Long2ObjectMap<User> users = new Long2ObjectOpenHashMap<>();
    private final Long2ByteMap states = new Long2ByteOpenHashMap();

    private volatile @Nullable Set<User> combinedCache;

    public ViewerSet(@NotNull VirtualEntity entity) {
        this.entity = entity;
    }

    public static long key(@NotNull User user) {
        return UUIDUtil.key(user.getUUID());
    }

    public boolean addAuto(@NotNull User user) {
        long id = key(user);
        byte s = states.get(id);
        if ((s & MANUAL) != 0) return false;

        User existing = users.get(id);
        boolean newSession = existing != null && existing != user;

        if ((s & AUTO) != 0 && !newSession) return false;

        users.put(id, user);
        states.put(id, (byte) (s | AUTO));
        combinedCache = null;
        entity.onViewerAdded(user);
        return true;
    }

    public boolean removeAuto(@NotNull User user) {
        long id = key(user);
        byte s = states.get(id);
        if ((s & AUTO) == 0) return false;

        byte newState = (byte) (s & ~AUTO);
        if (newState == 0) {
            states.remove(id);
            users.remove(id);
            combinedCache = null;
            entity.onViewerRemoved(user);
        } else {
            states.put(id, newState);
        }
        return true;
    }

    public boolean addManual(@NotNull User user) {
        long id = key(user);
        byte s = states.get(id);
        if ((s & MANUAL) != 0) return false;

        boolean wasAuto = (s & AUTO) != 0;
        users.put(id, user);
        states.put(id, (byte) (s | MANUAL));
        combinedCache = null;
        if (!wasAuto) entity.onViewerAdded(user);
        return true;
    }

    public boolean removeManual(@NotNull User user) {
        long id = key(user);
        byte s = states.get(id);
        if ((s & MANUAL) == 0) return false;

        byte newState = (byte) (s & ~MANUAL);
        if (newState == 0) {
            states.remove(id);
            users.remove(id);
            combinedCache = null;
            entity.onViewerRemoved(user);
        } else {
            states.put(id, newState);
        }
        return true;
    }

    public void clearViewer(@NotNull User user) {
        long id = key(user);
        if (users.remove(id) == null) return;
        states.remove(id);
        combinedCache = null;
    }

    public boolean isViewer(@NotNull User user) {
        return states.containsKey(key(user));
    }

    public boolean isManual(@NotNull User user) {
        return (states.get(key(user)) & MANUAL) != 0;
    }

    @Override
    public @NotNull Iterator<User> iterator() {
        return snapshot().iterator();
    }

    @Override
    public int size() {
        return users.size();
    }

    @Override
    public boolean isEmpty() {
        return users.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        if (!(o instanceof User u)) return false;
        return isViewer(u);
    }

    @Override
    public void clear() {
        if (users.isEmpty()) return;
        List<User> toRemove = new ArrayList<>(users.values());
        users.clear();
        states.clear();
        combinedCache = null;
        for (User u : toRemove) entity.onViewerRemoved(u);
    }

    public void clearSilent() {
        users.clear();
        states.clear();
        combinedCache = null;
    }

    @NotNull
    public Set<User> snapshot() {
        Set<User> cache = combinedCache;
        if (cache != null) return cache;
        Set<User> result = Collections.unmodifiableSet(new HashSet<>(users.values()));
        combinedCache = result;
        return result;
    }

    public @NotNull List<User> asList() {
        return new ArrayList<>(users.values());
    }
}