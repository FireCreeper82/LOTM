package de.firecreeper82.pathways.impl.fool.abilities;

import de.firecreeper82.lotm.Plugin;
import de.firecreeper82.pathways.Ability;
import de.firecreeper82.pathways.Pathway;
import de.firecreeper82.pathways.impl.fool.FoolItems;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;

public class FogOfHistory extends Ability implements Listener {

    ArrayList<ItemStack> items;
    ArrayList<ItemStack> summonedItems;
    ArrayList<Inventory> pages;

    ItemStack arrow;
    ItemStack barrier;

    int currentPage;

    public FogOfHistory(int identifier, Pathway pathway) {
        super(identifier, pathway);
        Plugin.instance.getServer().getPluginManager().registerEvents(this, Plugin.instance);
        items = new ArrayList<>();

        for(ItemStack item : pathway.getBeyonder().getPlayer().getInventory().getContents()) {
            if(item == null)
                continue;
            ItemStack addItem = item.clone();
            addItem.setAmount(1);
            addItem = normalizeItem(addItem);
            items.add(addItem);
        }
        summonedItems = new ArrayList<>();

        barrier = new ItemStack(Material.BARRIER);
        ItemMeta tempMeta = barrier.getItemMeta();
        assert tempMeta != null;
        tempMeta.setDisplayName("§aPrevious Page");
        tempMeta.addEnchant(Enchantment.LUCK, 1, true);
        tempMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        barrier.setItemMeta(tempMeta);

        arrow = new ItemStack(Material.ARROW);
        tempMeta.setDisplayName("§aNext Page");
        arrow.setItemMeta(tempMeta);

        currentPage = 0;
    }

    @EventHandler
    public void onPlayerPickUpItem(EntityPickupItemEvent e) {
        if(e.getEntity() != pathway.getBeyonder().getPlayer())
            return;
        ItemStack itemNormalized = normalizeItem(e.getItem().getItemStack().clone());


        boolean isContained = false;

        items.removeAll(Collections.singleton(null));

        items.removeIf(item -> pathway.getSequence().checkValid(item));


        for(ItemStack item : items) {
            if(item == null)
                continue;
            if(normalizeItem(item.clone()).isSimilar(itemNormalized))
                isContained = true;
        }

        if(!isContained) {
            ItemStack addItem = e.getItem().getItemStack().clone();
            addItem.setAmount(1);
            addItem = normalizeItem(addItem);
            items.add(addItem);
        }

    }

    private ItemStack normalizeItem(ItemStack itemNormalized) {
        if(itemNormalized instanceof Damageable) {
            ((Damageable) itemNormalized).setDamage(0);
        }
        return itemNormalized;
    }

    @Override
    public void useAbility() {
        Player p = pathway.getBeyonder().getPlayer();

        currentPage = 0;

        items.removeAll(Collections.singleton(null));

        items.removeIf(item -> pathway.getSequence().checkValid(item));

        double pageCount = Math.ceil((float) items.size() / 52);


        if(pageCount == 0)
            return;

        pages = new ArrayList<>();

        for(int i = 0; i < pageCount; i++) {
            pages.add(Bukkit.createInventory(p, 54, "§5Fog of History"));
        }

        int counter = 0;
        for(int i = 0; i < pageCount; i++) {
            for(int j = 0; j < 52; j++) {
                if(counter >= items.size())
                    break;
                pages.get(i).setItem(j, items.get(counter));
                counter++;
            }
        }
        for(Inventory inv : pages) {
            inv.setItem(52, barrier);
            inv.setItem(53, arrow);
        }

        p.openInventory(pages.get(0));

    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        ItemStack checkItem = e.getItemDrop().getItemStack().clone();

        boolean contains = false;
        for(ItemStack item : summonedItems) {
            if(item.isSimilar(checkItem))
                contains = true;
        }

        if(!contains)
            return;

        summonedItems.remove(checkItem);
        e.getItemDrop().remove();
    }

    @EventHandler
    public void onInventoryInteract(InventoryClickEvent e) {
        if(pages == null)
            return;
        if(!pages.contains(e.getInventory()))
            return;

        e.setCancelled(true);
        if(summonedItems.size() >= 3)
            return;
        if(e.getCurrentItem() == null)
            return;

        if(e.getCurrentItem().isSimilar(barrier)) {
            if(currentPage <= 0)
                return;
            currentPage -= 1;
            e.getWhoClicked().openInventory(pages.get(currentPage));
            return;
        }

        if(e.getCurrentItem().isSimilar(arrow)) {
            currentPage += 1;
            if(currentPage >= pages.size()) {
                currentPage -= 1;
                return;
            }
            e.getWhoClicked().openInventory(pages.get(currentPage));
            return;
        }


        ItemStack summonedItem = e.getCurrentItem().clone();
        summonedItem.setAmount(1);
        e.getWhoClicked().getInventory().addItem(summonedItem);
        summonedItems.add(summonedItem);
        new BukkitRunnable() {
            @Override
            public void run() {
                summonedItems.remove(summonedItem);
                if(e.getWhoClicked().getInventory().contains(summonedItem))
                    e.getWhoClicked().getInventory().remove(summonedItem);
            }
        }.runTaskLater(Plugin.instance, 20 * 60 * 3);
    }

    @Override
    public ItemStack getItem() {
        return FoolItems.createItem(Material.QUARTZ, "Fog of History", "100", identifier, 3, pathway.getBeyonder().getPlayer().getName());
    }
}
