package dev.slava7777.ventity.entity.metadata;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataType;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.pose.EntityPose;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.util.Vector3f;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class Metadata {
    public static final int INDEX_FLAGS = 0;
    public static final int INDEX_AIR_TICKS = 1;
    public static final int INDEX_CUSTOM_NAME = 2;
    public static final int INDEX_CUSTOM_NAME_VISIBLE = 3;
    public static final int INDEX_SILENT = 4;
    public static final int INDEX_NO_GRAVITY = 5;
    public static final int INDEX_POSE = 6;
    public static final int INDEX_FROZEN_TICKS = 7;

    public static final byte FLAG_ON_FIRE = 0x01;
    public static final byte FLAG_SNEAKING = 0x02;
    public static final byte FLAG_SPRINTING = 0x08;
    public static final byte FLAG_SWIMMING = 0x10;
    public static final byte FLAG_INVISIBLE = 0x20;
    public static final byte FLAG_GLOWING = 0x40;
    public static final byte FLAG_ELYTRA = (byte) 0x80;

    public static @NotNull EntityData<Byte> flags(byte flags) {
        return new EntityData<>(INDEX_FLAGS, EntityDataTypes.BYTE, flags);
    }

    public static @NotNull EntityData<Integer> airTicks(int ticks) {
        return new EntityData<>(INDEX_AIR_TICKS, EntityDataTypes.INT, ticks);
    }

    public static @NotNull EntityData<Optional<Component>> customName(@NotNull Component name) {
        return new EntityData<>(INDEX_CUSTOM_NAME, EntityDataTypes.OPTIONAL_ADV_COMPONENT,
                Optional.of(name));
    }

    public static @NotNull EntityData<Boolean> customNameVisible(boolean visible) {
        return new EntityData<>(INDEX_CUSTOM_NAME_VISIBLE, EntityDataTypes.BOOLEAN, visible);
    }

    public static @NotNull EntityData<Boolean> silent(boolean silent) {
        return new EntityData<>(INDEX_SILENT, EntityDataTypes.BOOLEAN, silent);
    }

    public static @NotNull EntityData<Boolean> noGravity(boolean noGravity) {
        return new EntityData<>(INDEX_NO_GRAVITY, EntityDataTypes.BOOLEAN, noGravity);
    }

    public static @NotNull EntityData<EntityPose> pose(@NotNull EntityPose pose) {
        return new EntityData<>(INDEX_POSE, EntityDataTypes.ENTITY_POSE, pose);
    }

    public static @NotNull EntityData<Integer> frozenTicks(int ticks) {
        return new EntityData<>(INDEX_FROZEN_TICKS, EntityDataTypes.INT, ticks);
    }

    public static byte setFlag(byte flags, byte mask, boolean value) {
        return value ? (byte) (flags | mask) : (byte) (flags & ~mask);
    }

    public static boolean hasFlag(byte flags, byte mask) {
        return (flags & mask) != 0;
    }

    private static <T> @NotNull EntityData<T> data(int index, EntityDataType<T> type, @NotNull T value) {
        return new EntityData<>(index, type, value);
    }

    public static final class Interaction {
        public static EntityData<Float> width(float v) {
            return data(8, EntityDataTypes.FLOAT, v);
        }

        public static EntityData<Float> height(float v) {
            return data(9, EntityDataTypes.FLOAT, v);
        }

        public static EntityData<Boolean> responsive(boolean v) {
            return data(10, EntityDataTypes.BOOLEAN, v);
        }
    }

    public static class Display {
        public static EntityData<Integer> interpolationDelay(int v) {
            return data(8, EntityDataTypes.INT, v);
        }

        public static EntityData<Integer> transformationInterpolationDuration(int v) {
            return data(9, EntityDataTypes.INT, v);
        }

        public static EntityData<Integer> positionRotationInterpolationDuration(int v) {
            return data(10, EntityDataTypes.INT, v);
        }

        public static EntityData<Vector3f> translation(Vector3f v) {
            return data(11, EntityDataTypes.VECTOR3F, v);
        }

        public static EntityData<Vector3f> scale(Vector3f v) {
            return data(12, EntityDataTypes.VECTOR3F, v);
        }

        public static EntityData<Vector3f> rotationLeft(Vector3f v) {
            return data(13, EntityDataTypes.VECTOR3F, v);
        }

        public static EntityData<Vector3f> rotationRight(Vector3f v) {
            return data(14, EntityDataTypes.VECTOR3F, v);
        }

        //FIXED, 0
        //VERTICAL, 1
        //HORIZONTAL, 2
        //CENTER; 3

        public static EntityData<Byte> billboardConstraints(byte v) {
            return data(15, EntityDataTypes.BYTE, v);
        }

        public static EntityData<Integer> brightnessOverride(int v) {
            return data(16, EntityDataTypes.INT, v);
        }

        public static EntityData<Float> viewRange(float v) {
            return data(17, EntityDataTypes.FLOAT, v);
        }

        public static EntityData<Float> shadowRadius(float v) {
            return data(18, EntityDataTypes.FLOAT, v);
        }

        public static EntityData<Float> shadowStrength(float v) {
            return data(19, EntityDataTypes.FLOAT, v);
        }

        public static EntityData<Float> width(float v) {
            return data(20, EntityDataTypes.FLOAT, v);
        }

        public static EntityData<Float> height(float v) {
            return data(21, EntityDataTypes.FLOAT, v);
        }

        public static EntityData<Integer> glowColorOverride(int v) {
            return data(22, EntityDataTypes.INT, v);
        }
    }

    public static final class BlockDisplay {
        //Надо использовать тут WrappedBlockState#getGlobalId();
        //Например, грязь: StateTypes.DIRT.createBlockState().getGlobalId();
        public static EntityData<Integer> displayedBlockState(int v) {
            return data(23, EntityDataTypes.BLOCK_STATE, v);
        }
    }

    public static final class ItemDisplay {
        public static EntityData<ItemStack> displayedItem(ItemStack v) {
            return data(23, EntityDataTypes.ITEMSTACK, v);
        }

        public static EntityData<Byte> displayType(byte v) {
            return data(24, EntityDataTypes.BYTE, v);
        }
    }

    public static final class TextDisplay {
        public static EntityData<Component> text(Component v) {
            return data(23, EntityDataTypes.ADV_COMPONENT, v);
        }

        public static EntityData<Integer> lineWidth(int v) {
            return data(24, EntityDataTypes.INT, v);
        }

        public static EntityData<Integer> backgroundColor(int v) {
            return data(25, EntityDataTypes.INT, v);
        }

        public static EntityData<Byte> textOpacity(byte v) {
            return data(26, EntityDataTypes.BYTE, v);
        }

        public static EntityData<Byte> textDisplayFlags(byte v) {
            return data(27, EntityDataTypes.BYTE, v);
        }

        //это надо использовать в TextDisplay#textDisplayFlags
        // Пример:
        // byte flags = 0;
        // flags |= TextDisplay.FLAG_HAS_SHADOW;
        // flags |= TextDisplay.FLAG_SEE_THROUGH;
        // TextDisplay.textDisplayFlags(flags); // будет тень и будет видно голограмму через блоки
        public static final byte FLAG_HAS_SHADOW = 0x01;
        public static final byte FLAG_SEE_THROUGH = 0x02;
        public static final byte FLAG_USE_DEFAULT_BACKGROUND = 0x04;
        public static final byte FLAG_ALIGN_LEFT = 0x08;
        public static final byte FLAG_ALIGN_RIGHT = 0x10;
    }

    //Потом может ещё добавлю...
}
