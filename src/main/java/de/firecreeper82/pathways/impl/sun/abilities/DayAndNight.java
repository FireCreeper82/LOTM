package de.firecreeper82.pathways.impl.sun.abilities;

import de.firecreeper82.lotm.Plugin;
import de.firecreeper82.pathways.Ability;
import de.firecreeper82.pathways.Items;
import de.firecreeper82.pathways.Pathway;
import de.firecreeper82.pathways.impl.sun.SunItems;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class DayAndNight extends Ability {
    public DayAndNight(int identifier, Pathway pathway, int sequence, Items items) {
        super(identifier, pathway, sequence, items);
        items.addToSequenceItems(identifier - 1, sequence);
    }

    @Override
    public void useAbility() {
        p = pathway.getBeyonder().getPlayer();
        pathway.getSequence().getUsesAbilities()[identifier - 1] = true;
        new BukkitRunnable() {
            int counter = 0;

            @Override
            public void run() {
                p.getWorld().setTime(p.getWorld().getTime() + 150);
                if (counter >= 20) {
                    pathway.getSequence().removeSpirituality(1000);
                    counter = 0;
                }
                counter++;
                if (!pathway.getSequence().getUsesAbilities()[identifier - 1]) {
                    cancel();
                }
            }
        }.runTaskTimer(Plugin.instance, 0, 1);
    }

    @Override
    public ItemStack getItem() {
        return SunItems.createItem(Material.CLOCK, "Day and Night", "1000/s", identifier, 1, Bukkit.getPlayer(pathway.getUuid()).getName());
    }
}
