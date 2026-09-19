package dev.slava7777.ventity.entity.living;

import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import dev.slava7777.ventity.entity.VirtualEntity;

import java.util.EnumMap;
import java.util.Map;

public class VirtualLivingEntity extends VirtualEntity implements EquipmentHandler {

    private final Map<EquipmentSlot, ItemStack> equipment = new EnumMap<>(EquipmentSlot.class);

    public VirtualLivingEntity(EntityType entityType) {

        super(entityType);
    }

    @Override
    public ItemStack getEquipment(EquipmentSlot slot) {
        return null;
    }

    @Override
    public void setEquipment(EquipmentSlot slot, ItemStack itemStack) {

    }
}
