package dev.ua.ikeepcalm.mystical.parents;

import dev.ua.ikeepcalm.entities.beyonders.Beyonder;
import dev.ua.ikeepcalm.LordOfTheMinecraft;
import dev.ua.ikeepcalm.mystical.pathways.demoness.DemonessPathway;
import dev.ua.ikeepcalm.mystical.pathways.door.DoorPathway;
import dev.ua.ikeepcalm.mystical.pathways.fool.FoolPathway;
import dev.ua.ikeepcalm.mystical.pathways.sun.SunPathway;
import dev.ua.ikeepcalm.mystical.pathways.tyrant.TyrantPathway;

import java.util.HashMap;
import java.util.UUID;

public abstract class Pathway {

    protected final UUID uuid;
    protected Sequence sequence;
    protected String name;
    protected Beyonder beyonder;
    protected final int optionalSequence;
    protected String stringColor;
    protected String nameNormalized;
    protected final int pathwayInt;

    public Items items;

    public Pathway(UUID uuid, int optionalSequence, int pathwayInt) {
        this.uuid = uuid;
        this.optionalSequence = optionalSequence;
        this.pathwayInt = pathwayInt;
    }


    public void init() {

    }

    public UUID getUuid() {
        return uuid;
    }

    public Sequence getSequence() {
        return sequence;
    }

    public void setSequence(Sequence sequence) {
        this.sequence = sequence;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Beyonder getBeyonder() {
        return beyonder;
    }

    public void setBeyonder(Beyonder beyonder) {
        this.beyonder = beyonder;
    }

    public Items getItems() {
        return items;
    }

    public void setItems(Items items) {
        this.items = items;
    }

    public String getStringColor() {
        return stringColor;
    }

    public Pathway getPathway() {
        return this;
    }

    public String getNameNormalized() {
        return nameNormalized;
    }


    //Initializes a new Pathway
    //Called from BeyonderCmd, Plugin and PotionListener
    public static Pathway initializeNew(String pathway, UUID uuid, int sequence) {
        Pathway pathwayObject;
        if (LordOfTheMinecraft.beyonders.containsKey(uuid))
            return null;
        switch (pathway) {
            case "sun" -> pathwayObject = new SunPathway(uuid, sequence, 0);
            case "fool" -> pathwayObject = new FoolPathway(uuid, sequence, 1);
            case "door" -> pathwayObject = new DoorPathway(uuid, sequence, 2);
            case "demoness" -> pathwayObject = new DemonessPathway(uuid, sequence, 3);
            case "tyrant" -> pathwayObject = new TyrantPathway(uuid, sequence, 4);
            default -> {
                return null;
            }
        }

        Beyonder beyonder = new Beyonder(uuid, pathwayObject);
        LordOfTheMinecraft.beyonders.put(uuid, beyonder);
        LordOfTheMinecraft.instance.getServer().getPluginManager().registerEvents(beyonder, LordOfTheMinecraft.instance);
        return pathwayObject;

    }

    public abstract void initItems();


    public static HashMap<Integer, String> getNamesForPathway(String pathway) {
        switch (pathway.toLowerCase()) {
            case "sun" -> {
                return SunPathway.getNames();
            }
            case "fool" -> {
                return FoolPathway.getNames();
            }
            case "door" -> {
                return DoorPathway.getNames();
            }
            case "demoness" -> {
                return DemonessPathway.getNames();
            }
            case "tyrant" -> {
                return TyrantPathway.getNames();
            }
            default -> {
                return null;
            }
        }
    }

    public int getPathwayInt() {
        return pathwayInt;
    }
}

