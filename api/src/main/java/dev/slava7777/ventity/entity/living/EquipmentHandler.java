package dev.slava7777.ventity.entity.living;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import dev.slava7777.ventity.entity.VirtualEntity;
import dev.slava7777.ventity.utils.Check;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public interface EquipmentHandler {

    /**
     * Gets the equipment in a specific slot.
     *
     * @param slot the equipment to get the item from
     * @return the equipment {@link ItemStack}
     */
    ItemStack getEquipment(EquipmentSlot slot);

    void setEquipment(EquipmentSlot slot, ItemStack itemStack);

    /**
     * Gets the {@link ItemStack} in main hand.
     *
     * @return the {@link ItemStack} in main hand
     */
    default ItemStack getItemInMainHand() {
        return getEquipment(EquipmentSlot.MAIN_HAND);
    }

    /**
     * Changes the main hand {@link ItemStack}.
     *
     * @param itemStack the main hand {@link ItemStack}
     */
    default void setItemInMainHand(ItemStack itemStack) {
        setEquipment(EquipmentSlot.MAIN_HAND, itemStack);
    }

    /**
     * Gets the {@link ItemStack} in off hand.
     *
     * @return the item in off hand
     */
    default ItemStack getItemInOffHand() {
        return getEquipment(EquipmentSlot.OFF_HAND);
    }

    /**
     * Changes the off hand {@link ItemStack}.
     *
     * @param itemStack the off hand {@link ItemStack}
     */
    default void setItemInOffHand(ItemStack itemStack) {
        setEquipment(EquipmentSlot.OFF_HAND, itemStack);
    }

    /**
     * Gets the {@link ItemStack} in the specific hand.
     *
     * @param hand the Hand to get the {@link ItemStack} from
     * @return the {@link ItemStack} in {@code hand}
     */
    default ItemStack getItemInHand(InteractionHand hand) {
        return switch (hand) {
            case MAIN_HAND -> getItemInMainHand();
            case OFF_HAND -> getItemInOffHand();
        };
    }

    /**
     * Changes the {@link ItemStack} in the specific hand.
     *
     * @param hand  the hand to set the item to
     * @param stack the {@link ItemStack} to set
     */
    default void setItemInHand(InteractionHand hand, ItemStack stack) {
        switch (hand) {
            case MAIN_HAND -> setItemInMainHand(stack);
            case OFF_HAND -> setItemInOffHand(stack);
        }
    }

    /**
     * Gets the helmet.
     *
     * @return the helmet
     */
    default ItemStack getHelmet() {
        return getEquipment(EquipmentSlot.HELMET);
    }

    /**
     * Changes the helmet.
     *
     * @param itemStack the helmet
     */
    default void setHelmet(ItemStack itemStack) {
        setEquipment(EquipmentSlot.HELMET, itemStack);
    }

    /**
     * Gets the chestplate.
     *
     * @return the chestplate
     */
    default ItemStack getChestplate() {
        return getEquipment(EquipmentSlot.CHEST_PLATE);
    }

    /**
     * Changes the chestplate.
     *
     * @param itemStack the chestplate
     */
    default void setChestplate(ItemStack itemStack) {
        setEquipment(EquipmentSlot.CHEST_PLATE, itemStack);
    }

    /**
     * Gets the leggings.
     *
     * @return the leggings
     */
    default ItemStack getLeggings() {
        return getEquipment(EquipmentSlot.LEGGINGS);
    }

    /**
     * Changes the leggings.
     *
     * @param itemStack the leggings
     */
    default void setLeggings(ItemStack itemStack) {
        setEquipment(EquipmentSlot.LEGGINGS, itemStack);
    }

    /**
     * Gets the boots.
     *
     * @return the boots
     */
    default ItemStack getBoots() {
        return getEquipment(EquipmentSlot.BOOTS);
    }

    /**
     * Changes the boots.
     *
     * @param itemStack the boots
     */
    default void setBoots(ItemStack itemStack) {
        setEquipment(EquipmentSlot.BOOTS, itemStack);
    }

    /**
     * Gets the body equipment. Used by horses, wolves, and llama's.
     *
     * @return the body equipment
     */
    default ItemStack getBodyEquipment() {
        return getEquipment(EquipmentSlot.BODY);
    }

    /**
     * Changes the body equipment. Used by horses, wolves, and llama's.
     *
     * @param itemStack the body equipment
     */
    default void setBodyEquipment(ItemStack itemStack) {
        setEquipment(EquipmentSlot.BODY, itemStack);
    }

    default boolean hasEquipment(EquipmentSlot slot) {
        return !getEquipment(slot).isEmpty();
    }

    /**
     * Sends a specific equipment to viewers.
     *
     * @param slot the slot of the equipment
     */
    default void syncEquipment(EquipmentSlot slot) {
        syncEquipment(slot, getEquipment(slot));
    }

    default void syncEquipment(EquipmentSlot slot, ItemStack stack) {
        syncEquipment(new Equipment(slot, stack));
    }

    default void syncEquipment(Equipment equipment) {
        syncEquipment(List.of(equipment));
    }

    default void syncEquipment(List<Equipment> equipment) {
        Check.stateCondition(!(this instanceof VirtualEntity), "Only accessible for Entity");

        VirtualEntity entity = (VirtualEntity) this;
        entity.sendPacketToViewers(new WrapperPlayServerEntityEquipment(entity.getEntityId(), equipment));
    }

    /**
     * Gets the packet with the non-empty equipments.
     *
     * @return the equipments packet, or {@code null} if every slot is empty
     * @throws IllegalStateException if 'this' is not an {@link VirtualEntity}
     */
    default @Nullable WrapperPlayServerEntityEquipment getEquipmentsPacket() {
        Check.stateCondition(!(this instanceof VirtualEntity), "Only accessible for Entity");

        final VirtualEntity entity = (VirtualEntity) this;

        final List<Equipment> equipment = new ArrayList<>(EquipmentSlot.values().length);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            final ItemStack stack = getEquipment(slot);
            if (stack == null || stack.isEmpty()) continue;
            equipment.add(new Equipment(slot, stack));
        }

        if (equipment.isEmpty()) return null;
        return new WrapperPlayServerEntityEquipment((entity).getEntityId(), equipment);
    }

}
