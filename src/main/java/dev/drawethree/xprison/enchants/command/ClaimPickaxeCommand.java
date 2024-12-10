package dev.drawethree.xprison.enchants.command;

import dev.drawethree.xprison.enchants.XPrisonEnchants;
import dev.drawethree.xprison.enchants.managers.CooldownManager;
import dev.drawethree.xprison.utils.item.PrisonItem;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import me.lucko.helper.Commands;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ClaimPickaxeCommand {

	private static final String COMMAND_NAME = "claimpickaxe";

	private final XPrisonEnchants plugin;

	public ClaimPickaxeCommand(XPrisonEnchants plugin) {
		this.plugin = plugin;
	}


	public void register() {
		Commands.create()
				.assertPlayer()
				.assertPermission("xprison.claim_pickaxe", this.plugin.getEnchantsConfig().getMessage("claim_pickaxe_no_permission"))
				.handler(c -> {

					if (!checkCooldown(c.sender())) {
						PlayerUtils.sendMessage(c.sender(), this.plugin.getEnchantsConfig().getMessage("claim_pickaxe_cooldown").replace("%time%", String.valueOf(this.getCooldownManager().getRemainingTime(c.sender()))));
						return;
					}

					ItemStack pickAxe = c.sender().getItemInHand();

					if (!validatePickaxe(pickAxe)) {
						PlayerUtils.sendMessage(c.sender(), this.plugin.getEnchantsConfig().getMessage("claim_pickaxe_no_pickaxe"));
						return;
					}

					PrisonItem item = new PrisonItem(pickAxe);
					if (item.getOwnerName() != null) {
						PlayerUtils.sendMessage(c.sender(), this.plugin.getEnchantsConfig().getMessage("claim_pickaxe_already_claimed").replace("%owner%", item.getOwnerName()));
						return;
					}

					item.setOwnerName(c.sender().getName());
					pickAxe = item.load();

					plugin.getEnchantsManager().updatePickaxe(c.sender().getPlayer(), pickAxe);
					PlayerUtils.sendMessage(c.sender(), this.plugin.getEnchantsConfig().getMessage("claim_pickaxe_success"));
				}).registerAndBind(plugin.getCore(), COMMAND_NAME);
	}

	private boolean validatePickaxe(ItemStack pickAxe) {
		return pickAxe != null && this.plugin.getCore().isPickaxeSupported(pickAxe.getType());
	}

	private boolean checkCooldown(Player sender) {
		return (sender.isOp() || !getCooldownManager().hasValueCooldown(sender));
	}

	private CooldownManager getCooldownManager() {
		return this.plugin.getCooldownManager();
	}
}
