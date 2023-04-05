package de.firecreeper82.lotm;

import de.firecreeper82.pathways.Ability;
import de.firecreeper82.pathways.Pathway;
import de.firecreeper82.pathways.Potion;
import de.firecreeper82.pathways.impl.fool.marionettes.Marionette;
import fr.mrmicky.fastboard.FastBoard;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

public class Beyonder implements Listener {

    private Pathway pathway;
    protected UUID uuid;

    private double spirituality;
    private double maxSpirituality;

    private double actingProgress;
    private double actingNeeded;
    private boolean digested;

    private FastBoard board;

    private boolean beyonder;
    private boolean loosingControl;
    public boolean online;

    private Team team;

    private ArrayList<Marionette> marionettes;
    private ArrayList<Mob> marionetteEntities;

    public Beyonder(UUID uuid, Pathway pathway) {
        this.pathway = pathway;
        this.uuid = uuid;

        pathway.setBeyonder(this);
        if(getPlayer() == null)
            return;

        start();

        pathway.initItems();

        marionettes = new ArrayList<>();
        marionetteEntities = new ArrayList<>();

        beyonder = true;
        loosingControl = false;

        //acting initializing
        digested = false;
        actingNeeded = Math.pow((float) (100 / pathway.getSequence().getCurrentSequence()), 2);
        actingProgress = 0;

        //Team
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        assert manager != null;
        Scoreboard board = manager.getNewScoreboard();
        team = board.registerNewTeam(getPlayer().getName() + " -- " + UUID.randomUUID());

        team.addEntry(getPlayer().getUniqueId().toString());
        team.setDisplayName("display name");
        team.setCanSeeFriendlyInvisibles(true);
        team.setAllowFriendlyFire(false);
        team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.ALWAYS);

    }

    @EventHandler
    //Restarts everything when Beyonder rejoins
    public void onJoin(PlayerJoinEvent e) {
        if(!e.getPlayer().getUniqueId().equals(uuid))
            return;
        if(!beyonder)
            return;
        start();
    }

    @EventHandler
    //Stops everything when Beyonder leaves
    public void onLeave(PlayerQuitEvent e) {
        if(!e.getPlayer().getUniqueId().equals(uuid))
            return;
        if(!beyonder)
            return;
        online = false;
    }

    @EventHandler
    //Removes Items on Death
    public void onDeath(PlayerDeathEvent e) {
        if(e.getEntity() != getPlayer())
            return;
        Player p = e.getEntity();
        for(Entity entity : p.getNearbyEntities(20, 20, 20)) {
            if(entity instanceof Item && pathway.getItems().returnItemsFromSequence(pathway.getSequence().getCurrentSequence()).contains(((Item) entity).getItemStack())) {
                entity.remove();
            }
        }
    }

    private void updateBoard() {
        board.updateTitle(pathway.getStringColor() + getPlayer().getName());
        board.updateLines("", "§5Pathway", "- " + pathway.getStringColor() + pathway.getName(), "", "§5Sequence", "- " + pathway.getStringColor() + pathway.getSequence().getCurrentSequence() + ": " + Objects.requireNonNull(Pathway.getNamesForPathway(pathway.getNameNormalized())).get(pathway.getSequence().getCurrentSequence()), "", "§5Spirituality", "- " + pathway.getStringColor() + Math.round(spirituality) + "/" + Math.round(maxSpirituality), "", "§5Acting", "- " + pathway.getStringColor() + Math.round(actingProgress) + "/" + Math.round(actingNeeded));
    }

    //Gets called everytime the Player rejoins or the Beyonder is newly initialised
    public void start() {
        updateSpirituality();
        board = new FastBoard(getPlayer());
        board.updateTitle(pathway.getStringColor() + getPlayer().getName());
        board.updateLines("", "§5Pathway", "- " + pathway.getStringColor() + pathway.getName(), "", "§5Sequence", "- " + pathway.getStringColor() + pathway.getSequence().getCurrentSequence() + ": " + Objects.requireNonNull(Pathway.getNamesForPathway(pathway.getNameNormalized())).get(pathway.getSequence().getCurrentSequence()), "", "§5Spirituality", "- " + pathway.getStringColor() + Math.round(spirituality) + "/" + Math.round(maxSpirituality), "", "§5Acting", "- " + pathway.getStringColor() + Math.round(actingProgress) + "/" + Math.round(actingNeeded));

        online = true;

        //onHold
        new BukkitRunnable() {
            @Override
            public void run() {
                //Cancel and return if player, sequence is null or player is not online
                if(!beyonder || !online || getPlayer() == null || pathway.getSequence() == null) {
                    cancel();
                    return;
                }

                if(loosingControl)
                    return;

                Player p = getPlayer();

                //Call onHold() function in Sequence
                pathway.getSequence().onHold(p.getInventory().getItemInMainHand());
            }
        }.runTaskTimer(Plugin.instance, 0, 3);

        //constant loop
        new BukkitRunnable() {
            int counter = 0;
            @Override
            public void run() {
                //Cancel and return if player, sequence is null or player is not online
                if(!beyonder || !online || getPlayer() == null || pathway.getSequence() == null) {
                    cancel();
                    return;
                }

                //scoreboard
                counter++;
                updateBoard();

                if(spirituality <= maxSpirituality / 100 && !loosingControl) {
                    looseControl(95, 10);
                }

                //spirituality handling
                if (spirituality < maxSpirituality && counter >= 8) {
                    counter = 0;
                    spirituality += (maxSpirituality / 200);
                    if(spirituality > maxSpirituality)
                        spirituality = maxSpirituality;
                }

                if(loosingControl)
                    return;

                Player p = getPlayer();

                //passive effects
                if(pathway.getSequence().getSequenceEffects().containsKey(pathway.getSequence().getCurrentSequence())) {
                    for(PotionEffect effect : pathway.getSequence().getSequenceEffects().get(pathway.getSequence().getCurrentSequence())) {
                        p.addPotionEffect(effect);
                    }
                }
                else {
                    for(int i = pathway.getSequence().getCurrentSequence(); i < 9; i++) {
                        if(pathway.getSequence().getSequenceEffects().containsKey(i)) {
                            for(PotionEffect effect : pathway.getSequence().getSequenceEffects().get(i)) {
                                p.addPotionEffect(effect);
                            }
                            break;
                        }
                    }
                }

                //passive resistances
                if(pathway.getSequence().getSequenceResistances().containsKey(pathway.getSequence().getCurrentSequence())) {
                    for(PotionEffectType effect : pathway.getSequence().getSequenceResistances().get(pathway.getSequence().getCurrentSequence())) {
                        for(PotionEffect potion : p.getActivePotionEffects()) {
                            if(potion.getType() == effect) {
                                p.removePotionEffect(effect);
                            }
                        }
                    }
                }
                else {
                    for(int i = pathway.getSequence().getCurrentSequence(); i < 9; i++) {
                        if(pathway.getSequence().getSequenceResistances().containsKey(i)) {
                            for(PotionEffectType effect : pathway.getSequence().getSequenceResistances().get(i)) {
                                if(p.getPotionEffect(effect) != null) {
                                    p.removePotionEffect(effect);
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }.runTaskTimer(Plugin.instance, 0, 10);
    }

    public void updateSpirituality() {
        if(pathway.getSequence().getCurrentSequence() > 8)
            spirituality = (int) Math.pow((float) (90 / pathway.getSequence().getCurrentSequence()), 2);
        else if(pathway.getSequence().getCurrentSequence() > 4)
            spirituality = (int) Math.pow((double) (90 / pathway.getSequence().getCurrentSequence()) * 2, 2);
        else if(pathway.getSequence().getCurrentSequence() < 5)
            spirituality = (int) Math.pow((float) (90 / pathway.getSequence().getCurrentSequence()), 3) ;
        maxSpirituality = spirituality;
    }

    public void updateActing() {
        if(actingProgress >= actingNeeded && !digested) {
            digested = true;
            getPlayer().sendMessage("§6You have digested the potion!");
            getPlayer().spawnParticle(Particle.END_ROD, pathway.getBeyonder().getPlayer().getLocation(), 50, 1, 1, 1, 0);
        }
        actingNeeded = Math.pow((100f / pathway.getSequence().getCurrentSequence()), 2);
    }

    public void acting(int sequence) {
        if (!digested) {
            actingProgress += 10f / sequence;
        }

        updateActing();
    }


    //lostControl: chance of surviving
    public void looseControl(int lostControl, int timeOfLoosingControl) {
        Random random = new Random();
        boolean survives = ((random.nextInt(100) + 1) <= lostControl);

        loosingControl = true;
        getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * timeOfLoosingControl, 3, false, false));
        getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * timeOfLoosingControl, 3, false, false));
        getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 20 * timeOfLoosingControl, 3, false, false));

        //Damaging player
        new BukkitRunnable() {
            int counter = 0;
            @Override
            public void run() {
                if(random.nextInt(25) + 1 == 5 && getPlayer().getHealth() > 2)
                    getPlayer().damage(2);

                counter++;
                if(counter == timeOfLoosingControl * 20) {
                    //When not survives, summons a Warden
                    if(!survives) {
                        Entity rampager = Objects.requireNonNull(getPlayer().getLocation().getWorld()).spawnEntity(getPlayer().getLocation(), EntityType.WARDEN);
                        rampager.setGlowing(true);
                        rampager.setCustomNameVisible(true);
                        rampager.setCustomName(pathway.getStringColor() + getPlayer().getName());
                        getPlayer().setHealth(0);
                        loosingControl = false;
                        removeBeyonder();
                        cancel();
                        return;
                    }
                    loosingControl = false;
                    cancel();
                }
            }
        }.runTaskTimer(Plugin.instance, 0, 1);
    }

    //Called from the PotionListener
    public void consumePotion(int sequence, Potion potion) {
        if(sequence >= pathway.getSequence().getCurrentSequence())
            return;

        if(!getPathway().getNameNormalized().equals(potion.getName())) {
            looseControl(0, 10);
            return;
        }
        if(pathway == null) {
            getPlayer().sendMessage("§cYour advancement has failed! You can call yourself lucky to still be alive...");
            return;
        }

        if(!digested) {
            looseControl(5, 12);
        }
        else {
            switch(getPathway().getSequence().getCurrentSequence() - 1 - sequence) {
                case 0 -> looseControl(93, 20);
                case 1 -> looseControl(50, 20);
                case 2 -> looseControl(30, 20);
                case 3, 4 -> looseControl(20, 16);
                case 5 -> looseControl(1, 16);
                default -> looseControl(0, 10);
            }
        }

        pathway.getSequence().setCurrentSequence(sequence);
        getPlayer().sendMessage(pathway.getItems().getAbilityInfo().get(sequence));
        digested = false;
        actingProgress = 0;
        updateActing();
        updateSpirituality();
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public Pathway getPathway() {
        return pathway;
    }

    public void setPathway(Pathway pathway) {
        this.pathway = pathway;
    }

    public UUID getUuid() {
        return uuid;
    }

    public double getSpirituality() {
        return spirituality;
    }

    public void setSpirituality(double spirituality) {
        this.spirituality = spirituality;
    }

    public void removeBeyonder() {
        for(Ability a : pathway.getSequence().getAbilities()) {
            a.removeAbility();
        }
        Plugin.instance.removeBeyonder(getUuid());
        if(board != null)
            board.delete();
        beyonder = false;
        pathway.setSequence(null);
        pathway = null;
    }

    public Team getTeam() {
        return team;
    }

    public boolean isBeyonder() {
        return beyonder;
    }

    public ArrayList<Marionette> getMarionettes() {
        return marionettes;
    }

    public ArrayList<Mob> getMarionetteEntities() {
        return marionetteEntities;
    }
}



