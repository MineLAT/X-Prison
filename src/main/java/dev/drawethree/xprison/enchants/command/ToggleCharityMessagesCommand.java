package dev.drawethree.xprison.enchants.command;

import dev.drawethree.xprison.enchants.XPrisonEnchants;
import me.lucko.helper.Commands;

public class ToggleCharityMessagesCommand {

	private final XPrisonEnchants plugin;

	public ToggleCharityMessagesCommand(XPrisonEnchants plugin) {
		this.plugin = plugin;
	}

	public void register() {
		Commands.create()
				.assertPlayer()
				.handler(c -> {
					plugin.getEnchantsManager().toggleCharityMessages(c.sender());
				}).registerAndBind(this.plugin.getCore(), "charitymessages");
	}

}
