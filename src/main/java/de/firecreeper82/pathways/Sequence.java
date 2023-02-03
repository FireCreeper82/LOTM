package de.firecreeper82.pathways;

import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class Sequence {

    public int currentSequence;
    public Pathway pathway;

    public boolean[] usesAbilities;
    public ArrayList<Ability> abilities;

    public HashMap<Integer, PotionEffect[]> sequenceEffects;

    public Sequence(Pathway pathway) {
        this.pathway = pathway;
    }
    public Sequence(Pathway pathway, int optionalSequence) {
        this.pathway = pathway;
        this.currentSequence = optionalSequence;
    }

    public ArrayList<ItemStack> returnItems() {
        return null;
    }

    public void useAbility(ItemStack item, PlayerInteractEvent e) {

    }

    public void destroyItem(ItemStack item, PlayerDropItemEvent e) {

    }

    public void useAbility(int ability, ItemStack item) {

    }

    public boolean checkValid(ItemStack item) {
        return true;
    }

    public void removeSpirituality(double remove) {

    }

    public int getCurrentSequence() {
        return currentSequence;
    }

    public void setCurrentSequence(int currentSequence) {
        this.currentSequence = currentSequence;
    }

    public Pathway getPathway() {
        return pathway;
    }

    public void setPathway(Pathway pathway) {
        this.pathway = pathway;
    }

    public HashMap<Integer, PotionEffect[]> getSequenceEffects() {
        return sequenceEffects;
    }

    public void setSequenceEffects(HashMap<Integer, PotionEffect[]> sequenceEffects) {
        this.sequenceEffects = sequenceEffects;
    }

    public ArrayList<Ability> getAbilities() {
        return abilities;
    }

    public void setAbilities(ArrayList<Ability> abilities) {
        this.abilities = abilities;
    }

    public boolean[] getUsesAbilities() {
        return usesAbilities;
    }

    public void setUsesAbilities(boolean[] usesAbilities) {
        this.usesAbilities = usesAbilities;
    }
}