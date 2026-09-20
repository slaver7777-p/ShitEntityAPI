package dev.slava7777.ventity.utils.cached;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public final class LazyPacket<T> {

    private final Supplier<@NotNull T> supplier;
    private volatile T cached;

    public LazyPacket(@NotNull Supplier<@NotNull T> supplier) {
        this.supplier = supplier;
    }

    public @NotNull T get() {
        T value = cached;
        if (value == null) {
            synchronized (this) {
                value = cached;
                if (value == null) {
                    value = supplier.get();
                    cached = value;
                }
            }
        }
        return value;
    }

    public void invalidate() {
        synchronized (this) {
            cached = null;
        }
    }
}