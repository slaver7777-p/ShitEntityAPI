package dev.slava7777.ventity;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.component.ComponentTypes;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.util.Vector3d;
import dev.by1337.cmd.Command;
import dev.by1337.core.command.bcmd.CommandWrapper;
import dev.by1337.core.util.text.minimessage.MiniMessage;
import dev.slava7777.ventity.entity.VirtualEntity;
import dev.slava7777.ventity.entity.living.VirtualLivingEntity;
import dev.slava7777.ventity.entity.metadata.Metadata;
import dev.slava7777.ventity.entity.tracker.VirtualEntityTracker;
import dev.slava7777.ventity.listener.JoinQuitListener;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Optional;
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

                                    VirtualLivingEntity entity = new VirtualLivingEntity(EntityTypes.ZOMBIE);
                                    entity.setEquipment(EquipmentSlot.CHEST_PLATE, ItemStack.builder().type(ItemTypes.NETHERITE_CHESTPLATE).component(ComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true).build());
                                    entity.setEquipment(EquipmentSlot.HELMET, ItemStack.builder().type(ItemTypes.DIAMOND_HELMET).component(ComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true).build());
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

                                    //invisible
                                    // new EntityData<>(0, EntityDataTypes.BYTE, (byte) 0x20);

                                    //тут кароче кастомное имя
                                    //final List<EntityData<?>> metadata = List.of(
                                            //CUSTOM_NAME
                                    //        new EntityData<>(2, EntityDataTypes.OPTIONAL_ADV_COMPONENT, Optional.ofNullable(MiniMessage.deserialize("<gradient:#EC550B:#F53100><bold>nnikitagay</bold></gradient>"))),
                                            //CUSTOM_NAME_VISIBLE
                                    //        new EntityData<>(3, EntityDataTypes.BOOLEAN, true)

                                    //);

                                    final List<EntityData<?>> metadata = List.of(
                                            Metadata.Display.billboardConstraints((byte) 1),
                                            Metadata.TextDisplay.textDisplayFlags(Metadata.TextDisplay.FLAG_SEE_THROUGH),
                                            Metadata.TextDisplay.text(MiniMessage.deserialize("<gradient:#EC550B:#F53100><bold>nn srudio</bold></gradient> <gradient:#EC550B:#F53100>nnikito4ka and slava7777 sdelali opyat govno</gradient>")),
                                            Metadata.TextDisplay.lineWidth(250),
                                            //background-color задаётся в aarrggbb формате
                                            Metadata.TextDisplay.backgroundColor(0xFF16ECF1),
                                            //прозрачность текста, -1 = 255 (текст непрозрачный)
                                            Metadata.TextDisplay.textOpacity((byte) 175)
                                            );

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

                                            //VirtualLivingEntity entity = new VirtualLivingEntity(EntityTypes.ZOMBIE);
                                            //entity.setEquipment(EquipmentSlot.CHEST_PLATE, ItemStack.builder().type(ItemTypes.NETHERITE_CHESTPLATE).component(ComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true).build());
                                            //entity.setEquipment(EquipmentSlot.HELMET, ItemStack.builder().type(ItemTypes.DIAMOND_HELMET).component(ComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true).build());
                                            //entity.setMetadata(metadata);
                                            //entity.setInstance(worldId, entityLoc);

                                            VirtualEntity entity = new VirtualEntity(EntityTypes.TEXT_DISPLAY);
                                            entity.setMetadata(metadata);
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
