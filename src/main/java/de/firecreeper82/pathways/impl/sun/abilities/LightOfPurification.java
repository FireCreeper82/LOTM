package de.firecreeper82.pathways.impl.sun.abilities;

import de.firecreeper82.lotm.Plugin;
import de.firecreeper82.pathways.Ability;
import de.firecreeper82.pathways.Pathway;
import de.firecreeper82.pathways.impl.sun.SunItems;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Random;

public class LightOfPurification extends Ability {
    public LightOfPurification(int identifier, Pathway pathway) {
        super(identifier, pathway);
    }

    @Override
    public void useAbility() {
        double multiplier = getMultiplier();

        p = pathway.getBeyonder().getPlayer();
        pathway.getSequence().getUsesAbilities()[identifier - 1] = true;

        Location loc = p.getLocation();

        //Spawning Particles
        loc.add(0, 1, 0);
        new BukkitRunnable() {
            double radius = 1.8;
            @Override
            public void run() {
                Particle.DustOptions dustRipple = new Particle.DustOptions(Color.fromBGR(0, 215, 255), 1f);
                radius = radius + 0.75;
                for(int j = 0; j < 30 * radius; j++) {
                    double x = radius * Math.cos(j);
                    double z = radius * Math.sin(j);
                    loc.getWorld().spawnParticle(Particle.REDSTONE, loc.getX() + x, loc.getY(), loc.getZ() + z, 5, 0.2, 1, 0.2, 0, dustRipple);
                    Random rand = new Random();
                    if(j % (rand.nextInt(8) + 1) == 0)
                        loc.getWorld().spawnParticle(Particle.END_ROD, loc.getX() + x, loc.getY(), loc.getZ() + z, 1, 0.2, 1, 0.2, 0);

                    //checking for entities
                    for(Entity entity : loc.getWorld().getNearbyEntities(new Location(loc.getWorld(), loc.getX() + x, loc.getY(), loc.getZ() + z), 1, 3, 1)) {
                        if(entity instanceof LivingEntity) {
                            if(((LivingEntity) entity).getCategory() == EntityCategory.UNDEAD)
                                ((Damageable) entity).damage(25 * multiplier, p);
                        }
                    }
                }

                if(radius >= 20) {
                    cancel();
                    pathway.getSequence().getUsesAbilities()[identifier - 1] = false;
                }
            }
        }.runTaskTimer(Plugin.instance, 0, 1);
    }

    @Override
    public ItemStack getItem() {
        return SunItems.createItem(Material.GLOWSTONE, "Light of Purification", "50", identifier, 5, Bukkit.getPlayer(pathway.getUuid()).getName());
    }
}
