package me.drawethree.wildprisonenchants.enchants.implementations;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.drawethree.wildprisonautosell.WildPrisonAutoSell;
import me.drawethree.wildprisonenchants.WildPrisonEnchants;
import me.drawethree.wildprisonenchants.enchants.WildPrisonEnchantment;
import me.drawethree.wildprisonmultipliers.WildPrisonMultipliers;
import me.drawethree.wildprisontokens.WildPrisonTokens;
import me.lucko.helper.Schedulers;
import me.lucko.helper.cooldown.Cooldown;
import me.lucko.helper.cooldown.CooldownMap;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class JackHammerEnchant extends WildPrisonEnchantment {

    private final double chance;
    private final int cooldown;
    private final CooldownMap<Player> cooldownMap;

    public JackHammerEnchant(WildPrisonEnchants instance) {
        super(instance, 10);
        this.chance = plugin.getConfig().getDouble("enchants." + id + ".Chance");
        this.cooldown = plugin.getConfig().getInt("enchants." + id + ".Cooldown");
        this.cooldownMap = CooldownMap.create(Cooldown.of(cooldown, TimeUnit.SECONDS));
    }

    @Override
    public void onEquip(Player p, ItemStack pickAxe, int level) {

    }

    @Override
    public void onUnequip(Player p, ItemStack pickAxe, int level) {

    }

    @Override
    public void onBlockBreak(BlockBreakEvent e, int enchantLevel) {
        if (!cooldownMap.test(e.getPlayer())) {
            return;
        }

        if (chance * enchantLevel >= ThreadLocalRandom.current().nextDouble()) {
            Block b = e.getBlock();
            List<ProtectedRegion> regions = plugin.getWorldGuard().getRegionContainer().get(b.getWorld()).getApplicableRegions(b.getLocation()).getRegions().stream().filter(reg -> reg.getId().startsWith("mine")).collect(Collectors.toList());
            if (regions.size() > 0) {
                Player p = e.getPlayer();
                ProtectedRegion region = regions.get(0);
                //List<BlockState> blocksAffected = new ArrayList<>();

                long totalDeposit = 0;
                int blockCount = 0;
                int fortuneLevel = WildPrisonEnchants.getApi().getEnchantLevel(p, 3);
                int amplifier = fortuneLevel == 0 ? 1 : fortuneLevel + 1;
                for (int x = region.getMinimumPoint().getBlockX(); x <= region.getMaximumPoint().getBlockX(); x++) {
                    for (int z = region.getMinimumPoint().getBlockZ(); z <= region.getMaximumPoint().getBlockZ(); z++) {
                        Block b1 = b.getWorld().getBlockAt(x, b.getY(), z);
                        if (b1 != null && b1.getType() != Material.AIR) {
                            blockCount++;
                            //blocksAffected.add(b1.getState());
                            if (WildPrisonAutoSell.getApi().hasAutoSellEnabled(p)) {
                                totalDeposit += (WildPrisonAutoSell.getApi().getPriceForBrokenBlock(region, b1) * amplifier);
                            } else {
                                p.getInventory().addItem(new ItemStack(b1.getType(), fortuneLevel + 1));
                            }
                            b1.setType(Material.AIR);
                        }
                    }
                }

                //Bukkit.getPluginManager().callEvent(new JackHammerTriggerEvent(e.getPlayer(), region, blocksAffected));

                WildPrisonEnchants.getEconomy().depositPlayer(p, WildPrisonMultipliers.getApi().getTotalToDeposit(p, totalDeposit));
                WildPrisonEnchants.getEnchantsManager().addBlocksBrokenToItem(p, blockCount);
                WildPrisonTokens.getInstance().getTokensManager().addBlocksBroken(p, blockCount);
            }
        }
    }
}
