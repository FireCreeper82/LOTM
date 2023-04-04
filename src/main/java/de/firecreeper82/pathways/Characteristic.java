package de.firecreeper82.pathways;

import de.firecreeper82.lotm.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;


public class Characteristic {

    private final HashMap<String, URL> blocksForPathway;
    private final UUID uuid;

    public Characteristic() throws MalformedURLException {
        blocksForPathway = new HashMap<>();
        uuid = UUID.randomUUID();
        blocksForPathway.put("fool", new URL("http://textures.minecraft.net/texture/855af6c5ff21eb55631a25221d753cdc9a0f679d5cacf555b350ba0e3521e092"));
        blocksForPathway.put("sun", new URL("http://textures.minecraft.net/texture/8a03a8a877de7a4d6b167633a96ae3983998fd9d9a4c5e3fa817d138e81e4499"));
    }


    public ItemStack getCharacteristic(int sequence, String pathway, String pathwayColor) {
        final ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta playerMeta = (SkullMeta) playerHead.getItemMeta();
        assert playerMeta != null;
        playerMeta.setDisplayName(pathwayColor + Util.capitalize(pathway) + " Beyonder Characteristic");
        String[] playerLore = {
                pathwayColor + "A Characteristic corresponding to Sequence " + sequence,
                pathwayColor  + "of the " + Util.capitalize(pathway) + " Pathway!"
        };
        playerMeta.setLore(Arrays.asList(playerLore));
        if(blocksForPathway.get(pathway) != null) {
            PlayerProfile playerProfile = Bukkit.createPlayerProfile(uuid);
            PlayerTextures playerTextures = playerProfile.getTextures();
            playerTextures.setSkin(blocksForPathway.get(pathway));
            playerMeta.setOwnerProfile(playerProfile);
        }
        playerMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        playerMeta.addEnchant(Enchantment.CHANNELING, 1, true);
        playerHead.setItemMeta(playerMeta);

        return playerHead;
    }
}
