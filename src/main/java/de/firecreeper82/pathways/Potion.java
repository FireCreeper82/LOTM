package de.firecreeper82.pathways;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;

import java.util.Arrays;
import java.util.HashMap;

public abstract class Potion {
    public String name;

    public HashMap<Integer, ItemStack[]> potionRecipes;
    public abstract ItemStack[] getSequencePotion(int sequence);
    public abstract ItemStack returnPotionForSequence(int sequence);

    public static ItemStack createPotion(String stringColor, int sequence, String name, Color color, String ritual) {
        ItemStack potion = new ItemStack(Material.POTION);
        PotionMeta potionMeta = (PotionMeta) potion.getItemMeta();
        potionMeta.setDisplayName(stringColor + "Sequence " + sequence + ": " + name);
        potionMeta.setColor(color);
        potionMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        String[] lore;
        if(ritual.equals("")) {
            lore = new String[]{
                    "§5Drink this Potion to gain the powers",
                    "§5of a Sequence " + sequence + ": " + name
            };
        }
        else {
            lore = new String[]{
                    "§5Drink this Potion to gain the powers",
                    "§5of a Sequence " + sequence + ": " + name,
                    "§5Ritual: " + ritual
            };
        }
        potionMeta.setLore(Arrays.asList(lore));
        potion.setItemMeta(potionMeta);
        return potion;
    }
}
