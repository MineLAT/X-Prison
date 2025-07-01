package dev.drawethree.xprison.autosell.listener;

import dev.drawethree.xprison.autosell.XPrisonAutoSell;
import dev.drawethree.xprison.autosell.model.SellRegion;
import dev.drawethree.xprison.utils.compat.MinecraftVersion;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import org.bukkit.Location;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.WorldLoadEvent;

import java.util.HashSet;
import java.util.Set;

public class AutoSellListener {

    private final XPrisonAutoSell plugin;
    private final Set<Location> clearLocations = new HashSet<>();

    public AutoSellListener(XPrisonAutoSell plugin) {
        this.plugin = plugin;
    }

    public void subscribeToEvents() {
        this.subscribeToPlayerJoinEvent();
        this.subscribeToBlockBreakEvent();
        this.subscribeToWorldLoadEvent();
    }

    private void subscribeToWorldLoadEvent() {
        Events.subscribe(WorldLoadEvent.class)
                .handler(e -> this.plugin.getManager().loadPostponedAutoSellRegions(e.getWorld())).bindWith(this.plugin.getCore());
    }

    private void subscribeToPlayerJoinEvent() {
        Events.subscribe(PlayerJoinEvent.class)
				.handler(e -> Schedulers.sync().runLater(() -> {

					if (this.plugin.getManager().hasAutoSellEnabled(e.getPlayer())) {
						PlayerUtils.sendMessage(e.getPlayer(), this.plugin.getAutoSellConfig().getMessage("autosell_enable"));
						return;
					}

					if (this.plugin.getManager().canPlayerEnableAutosellOnJoin(e.getPlayer())) {
						this.plugin.getManager().toggleAutoSell(e.getPlayer());
					}
				}, 20)).bindWith(this.plugin.getCore());
    }

    private void subscribeToBlockBreakEvent() {

        Events.subscribe(BlockBreakEvent.class, EventPriority.HIGHEST)
                .filter(e -> !e.isCancelled() && e.getPlayer().getItemInHand() != null && this.plugin.getCore().isPickaxeSupported(e.getPlayer().getItemInHand().getType()))
                .handler(e -> {

                    SellRegion sellRegion = this.plugin.getManager().getAutoSellRegion(e.getBlock().getLocation());

                    if (sellRegion == null) {
                        return;
                    }

                    boolean success = false;

                    if (this.plugin.getManager().hasAutoSellEnabled(e.getPlayer())) {
                        success = this.plugin.getManager().autoSellBlock(e.getPlayer(), e.getBlock());
                    }

                    if (!success) {
                        success = this.plugin.getManager().givePlayerItem(e.getPlayer(), e.getBlock());
                    }

                    if (success) {
                        // Do not set block to air due compatibility issues
                        if (MinecraftVersion.atLeast(MinecraftVersion.V.v1_12)) {
                            e.setDropItems(false);
                        } else {
                            final Location location = e.getBlock().getLocation();
                            clearLocations.add(location);
                            Schedulers.bukkit().runTaskLater(plugin.getCore(), () -> clearLocations.remove(location), 10L);
                        }
                    } else {
                        e.setCancelled(true);
                    }
                }).bindWith(this.plugin.getCore());

        Events.subscribe(ItemSpawnEvent.class, EventPriority.HIGHEST)
                .filter(e -> !e.isCancelled() && clearLocations.remove(e.getLocation()))
                .handler(e -> {
                    e.setCancelled(true);
                }).bindWith(this.plugin.getCore());
    }
}

