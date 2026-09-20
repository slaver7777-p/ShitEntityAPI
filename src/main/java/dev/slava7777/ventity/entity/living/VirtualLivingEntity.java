package dev.slava7777.ventity.entity.living;

import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import dev.slava7777.ventity.entity.VirtualEntity;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class VirtualLivingEntity extends VirtualEntity implements EquipmentHandler {

    private ItemStack mainHandItem = ItemStack.EMPTY;
    private ItemStack offHandItem = ItemStack.EMPTY;
    private ItemStack helmet = ItemStack.EMPTY;
    private ItemStack chestplate = ItemStack.EMPTY;
    private ItemStack leggings = ItemStack.EMPTY;
    private ItemStack boots = ItemStack.EMPTY;
    private ItemStack bodyItem = ItemStack.EMPTY;

    public VirtualLivingEntity(EntityType entityType) {
        super(entityType);
    }

    @Override
    public @NotNull ItemStack getEquipment(@NotNull EquipmentSlot slot) {
        return switch (slot) {
            case MAIN_HAND -> mainHandItem;
            case OFF_HAND -> offHandItem;
            case HELMET -> helmet;
            case CHEST_PLATE -> chestplate;
            case LEGGINGS -> leggings;
            case BOOTS -> boots;
            case BODY, SADDLE -> bodyItem;
        };
    }

    @Override
    public void setEquipment(@NotNull EquipmentSlot slot, @NotNull ItemStack itemStack) {
        ItemStack current = getEquipment(slot);
        if (current.equals(itemStack)) return;

        switch (slot) {
            case MAIN_HAND -> mainHandItem = itemStack;
            case OFF_HAND -> offHandItem = itemStack;
            case HELMET -> helmet = itemStack;
            case CHEST_PLATE -> chestplate = itemStack;
            case LEGGINGS -> leggings = itemStack;
            case BOOTS -> boots = itemStack;
            case BODY, SADDLE -> bodyItem = itemStack;
        }

        if (isActive() && !getViewers().isEmpty()) {
            syncEquipment(slot);
        }
    }

    @Override
    public void syncEquipment(@NotNull List<Equipment> equipment) {
        if (equipment.isEmpty()) return;
        if (!isActive() || getViewers().isEmpty()) return;
        WrapperPlayServerEntityEquipment equipmentPacket = getEquipmentsPacket();
        if (equipmentPacket == null) return;
        sendPacketToViewers(equipmentPacket);
    }

    @Override
    public void sendSpawnPackets(@NotNull User user) {
        super.sendSpawnPackets(user);
        WrapperPlayServerEntityEquipment equipmentPacket = getEquipmentsPacket();
        if (equipmentPacket == null) return;
        user.sendPacket(equipmentPacket);
    }
}
