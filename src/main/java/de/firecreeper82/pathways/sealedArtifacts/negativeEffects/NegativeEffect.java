package de.firecreeper82.pathways.sealedArtifacts.negativeEffects;

import de.firecreeper82.lotm.Plugin;
import de.firecreeper82.pathways.sealedArtifacts.SealedArtifact;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class NegativeEffect {

    protected int sequence;
    protected int delay;
    protected boolean constant;

    public NegativeEffect(int sequence, int delay, boolean constant) {
        this.sequence = sequence;
        this.delay = delay;
        this.constant = constant;
    }

    public void start(SealedArtifact artifact) {
        NegativeEffect instance = this;

        new BukkitRunnable() {
            int counter = 20;
            int delayCounter = delay;
            Player p;

            @Override
            public void run() {
                if (counter <= 0) {
                    if (p == null || (p.isValid() && !p.getInventory().contains(artifact.getItem())) || !p.isValid()) {
                        p = null;
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            if (player.getInventory().contains(artifact.getItem()))
                                p = player;
                        }
                    }
                    counter = 20;
                }
                counter--;

                delayCounter--;
                if (delayCounter != 0)
                    return;

                delayCounter = delay;

                if (p == null || !p.isValid()) {
                    p = null;
                    return;
                }

                if (instance.constant)
                    instance.doEffect(artifact, p);
                else if (p.getInventory().getItemInMainHand().isSimilar(artifact.getItem()))
                    doEffect(artifact, p);
            }
        }.runTaskTimer(Plugin.instance, 0, 0);
    }

    public int getDelay() {
        return delay;
    }

    public abstract void doEffect(SealedArtifact artifact, Player p);
}
