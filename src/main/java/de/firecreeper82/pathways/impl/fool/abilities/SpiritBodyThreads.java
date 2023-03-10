package de.firecreeper82.pathways.impl.fool.abilities;

import de.firecreeper82.lotm.Plugin;
import de.firecreeper82.pathways.Ability;
import de.firecreeper82.pathways.Pathway;
import de.firecreeper82.pathways.impl.fool.FoolItems;
import de.firecreeper82.pathways.impl.fool.marionettes.Marionette;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.util.*;

public class SpiritBodyThreads extends Ability {

    private final HashMap<Integer, int[]> mobColors;
    private final HashMap<EntityCategory, Integer> categoryToInt;
    private final HashMap<String, EntityCategory> stringToCategory;

    private final ArrayList<String> disabledCategories;

    private final ArrayList<Entity> marionettes;

    private Entity selectedEntity;

    private int maxDistance;
    private int maxDistanceControl;
    private int preferredDistance;

    private int convertTimeSeconds;

    private boolean turning;

    private Team team;

    public SpiritBodyThreads(int identifier, Pathway pathway) {
        super(identifier, pathway);

        disabledCategories = new ArrayList<>();

        marionettes = new ArrayList<>();

        mobColors = new HashMap<>();
        categoryToInt = new HashMap<>();
        stringToCategory = new HashMap<>();

        categoryToInt.put(EntityCategory.UNDEAD, 1);
        categoryToInt.put(EntityCategory.ARTHROPOD, 2);
        categoryToInt.put(EntityCategory.ILLAGER, 3);
        categoryToInt.put(EntityCategory.NONE, 4);
        categoryToInt.put(EntityCategory.WATER, 4);

        mobColors.put(0, new int[]{78, 78, 78});
        mobColors.put(1, new int[]{75, 133, 0});
        mobColors.put(2, new int[]{57, 0, 133});
        mobColors.put(3, new int[]{87, 43, 0});
        mobColors.put(4, new int[]{0, 38, 69});

        stringToCategory.put("undead", EntityCategory.UNDEAD);
        stringToCategory.put("normal", EntityCategory.NONE);
        stringToCategory.put("illager", EntityCategory.ILLAGER);
        stringToCategory.put("arthropod", EntityCategory.ARTHROPOD);

        maxDistance = 50;
        maxDistanceControl = 10;
        preferredDistance = maxDistance;

        convertTimeSeconds = 45;

        turning = false;

        p = pathway.getBeyonder().getPlayer();

        ScoreboardManager manager = Bukkit.getScoreboardManager();
        assert manager != null;
        Scoreboard board = manager.getNewScoreboard();
        team = board.registerNewTeam(p.getUniqueId().toString());
        team.setPrefix(ChatColor.DARK_PURPLE + p.getName());
        team.addEntry(p.getUniqueId().toString());

        team.setCanSeeFriendlyInvisibles(true);
        team.setAllowFriendlyFire(false);
    }

    @Override
    //Check if Player is already turning something into Marionette
    // if not -> calls the turningIntoMarionette function
    // else -> stops the turning process
    public void useAbility() {
        if(selectedEntity == null)
            return;

        if(!turning) {
            turnIntoMarionette(selectedEntity);
            return;
        }

        turning = false;
    }

    public void turnIntoMarionette(Entity e) {
        if(!(e instanceof LivingEntity)) {
            turning = false;
            return;
        }
        Player p = pathway.getBeyonder().getPlayer();
        turning = true;

        //Make hostile entities aware of Player
        ((Damageable) e).damage(0, p);

        //Runs every 1/2 seconds and gives Entity effects
        //At the end of the time if entity is still being turned, removes entity
        new BukkitRunnable() {
            long counter = 2L * convertTimeSeconds;
            @Override
            public void run() {
                if(!turning) {
                    cancel();
                    return;
                }

                //Check if entity is too far away
                Location entityLoc = e.getLocation().clone();
                entityLoc.add(0, 0.75, 0);
                if(entityLoc.distance(p.getEyeLocation()) > maxDistanceControl) {
                    turning = false;
                    cancel();
                    return;
                }

                counter--;


                ((LivingEntity) e).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 5));
                ((LivingEntity) e).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 4));
                ((LivingEntity) e).addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 1));

                if(!turning)
                    cancel();
                if(counter <= 0) {
                    turning = false;

                    //Marionette is a Player
                    if(e instanceof Player) {
                        ((Player) e).setHealth(0);
                        return;
                    }
                    else {
                        new Marionette(selectedEntity.getLocation());
                        selectedEntity.remove();
                    }

                    cancel();
                }
            }
        }.runTaskTimer(Plugin.instance, 0, 10);

        //Particle effects for Entities
        new BukkitRunnable() {
            long counter = 10L * convertTimeSeconds;
            double spiralRadius = 2;

            double spiral = 0;
            double height = 0;
            double spiralX;
            double spiralZ;
            @Override
            public void run() {
                Location entityLoc = e.getLocation().clone();
                entityLoc.add(0, 0.75, 0);

                spiralX = spiralRadius * Math.cos(spiral);
                spiralZ = spiralRadius * Math.sin(spiral);
                spiral += 0.25;
                height += .05;
                if(height >= 2.5)
                    height = 0;
                Particle.DustOptions dust = new Particle.DustOptions(Color.fromBGR(154, 0, 194), 1.25f);
                if(entityLoc.getWorld() != null)
                    entityLoc.getWorld().spawnParticle(Particle.REDSTONE, spiralX + entityLoc.getX(), height + entityLoc.getY(), spiralZ + entityLoc.getZ(), 5, dust);

                counter--;
                spiralRadius -= (1.5 / (10L * convertTimeSeconds));

                if(!turning)
                    cancel();
                if(counter <= 0) {
                    cancel();
                }

            }
        }.runTaskTimer(Plugin.instance, 0, 2);
    }


    @Override
    public ItemStack getItem() {
        return FoolItems.createItem(Material.LEAD, "Spirit Body Threads", "100", identifier, 5, pathway.getBeyonder().getPlayer().getName());
    }

    @Override
    public void onHold() {
        Player p = pathway.getBeyonder().getPlayer();

        //Loop through hall entities and check their respective color and "draw" the Thread
        //Indicate selected entity on the actionbar
        outerloop: for(Entity e : p.getNearbyEntities(preferredDistance, preferredDistance, preferredDistance)) {
            if(e == p || !(e instanceof LivingEntity))
                continue;

            //Check if Thread is disabled via disable-thread command
            if(e instanceof Player && disabledCategories.contains("player"))
                continue ;
            EntityCategory entityCategory = normalizeCategory(((LivingEntity) e).getCategory());
            for(String s : disabledCategories) {
                if(s.equals("player"))
                    continue;
                if(entityCategory == stringToCategory.get(s) && !(e instanceof Player))
                    continue outerloop;
            }

            //Randomly sets the selected entity to an entity in the control range
            if(selectedEntity == null && e.getLocation().clone().add(0, 0.75, 0).distance(p.getEyeLocation()) <= maxDistanceControl) {
                selectedEntity = e;
            }
            if(selectedEntity != null && selectedEntity.getLocation().distance(p.getLocation()) > maxDistanceControl) {
                selectedEntity = null;
            }

            //Getting the colors
            int[] colors;
            if(e == selectedEntity)
                colors = new int[]{255, 255, 255};
            else if(e instanceof Player)
                colors = mobColors.get(0);
            else
                colors = mobColors.get(categoryToInt.get(((LivingEntity) e).getCategory()));

            //Check if currently turning Entity into Marionette
            if(turning) {
                if(e != selectedEntity)
                    continue;
                colors = new int[]{145, 0, 194};
            }


            //Drawing the threads
            Location entityLoc = e.getLocation().clone().add(0, 0.75, 0);
            Location playerLoc = p.getEyeLocation().clone().subtract(0, 0.5, 0);
            Vector dir = entityLoc.toVector().subtract(playerLoc.toVector()).normalize().multiply(.65);

            int counter = 0;
            while(playerLoc.distance(entityLoc) > .5 && counter < 150) {
                Particle.DustOptions dust = new Particle.DustOptions(Color.fromBGR(colors[0], colors[1], colors[2]), .75f);
                p.spawnParticle(Particle.REDSTONE, playerLoc, 1, .05, 0, .05, dust);
                playerLoc.add(dir);
                counter++;
            }

            //Displaying the actionbar
            String entityName;
            if(selectedEntity == null)
                entityName = "None";
            else
                entityName = selectedEntity.getType().name().substring(0, 1).toUpperCase() + selectedEntity.getType().name().substring(1).toLowerCase();
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("??5Selected: ??8" + entityName));
        }
    }

    @Override
    public void leftClick() {
        Player p = pathway.getBeyonder().getPlayer();

        //Loop through hall entities and check their respective color and "draw" the Thread
        //Indicate selected entity on the actionbar
        List<Entity> entities = p.getNearbyEntities(preferredDistance, preferredDistance, preferredDistance);
        Collections.shuffle(entities);
        outerloop: for(Entity e : entities) {
            if (e == p || !(e instanceof LivingEntity) || e == selectedEntity)
                continue;

            //Check if Thread is disabled via disable-thread command
            if (e instanceof Player && disabledCategories.contains("player"))
                return;
            EntityCategory entityCategory = normalizeCategory(((LivingEntity) e).getCategory());
            for (String s : disabledCategories) {
                if (s.equals("player"))
                    continue;
                if (entityCategory == stringToCategory.get(s))
                    continue outerloop;
            }

            //Randomly sets the selected entity to an entity in the control range
            if (e.getLocation().distance(p.getLocation()) <= maxDistanceControl) {
                selectedEntity = e;
                break;
            }
        }
    }

    //Disable / Enable a specific EntityCategory for the Threads
    public boolean disableCategory(String category) {
        if(!disabledCategories.contains(category.toLowerCase())) {
            disabledCategories.add(category.toLowerCase());
            return true;
        }
        else {
            disabledCategories.remove(category.toLowerCase());
            return false;
        }
    }

    public void setPreferredDistance(int distance) {
        preferredDistance = Math.min(distance, maxDistance);
    }

    //If given EntityCategory.WATER returns EntityCategory.NONE
    public EntityCategory normalizeCategory(EntityCategory entityCategory) {
        if(entityCategory == EntityCategory.WATER)
            return EntityCategory.NONE;

        return entityCategory;
    }
}
