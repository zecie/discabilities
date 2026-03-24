package com.discabilities;

import org.bukkit.plugin.java.JavaPlugin;

public class DiscAbilitiesPlugin extends JavaPlugin {

    private AbilityManager abilityManager;
    private CooldownManager cooldownManager;
    private CombatTracker combatTracker;
    private DiscInventory discInventory;
    private HudManager hudManager;
    private HitTracker hitTracker;
    private PassiveManager passiveManager;
    private TrustManager trustManager;

    @Override
    public void onEnable() {
        cooldownManager = new CooldownManager();
        combatTracker = new CombatTracker();
        hitTracker = new HitTracker();
        trustManager = new TrustManager();
        abilityManager = new AbilityManager(this, cooldownManager, combatTracker);
        abilityManager.setTrustManager(trustManager);
        discInventory = new DiscInventory(this);
        hudManager = new HudManager(this, discInventory, cooldownManager, abilityManager, hitTracker);
        passiveManager = new PassiveManager(this, discInventory, hitTracker, cooldownManager, combatTracker, abilityManager, trustManager);

        hudManager.start();
        passiveManager.start();

        getServer().getPluginManager().registerEvents(
            new DiscUseListener(abilityManager, cooldownManager, combatTracker, discInventory, hudManager, passiveManager), this);
        getServer().getPluginManager().registerEvents(
            new CombatListener(combatTracker, passiveManager, trustManager), this);
        getServer().getPluginManager().registerEvents(
            new DeathListener(discInventory, abilityManager, hudManager, hitTracker), this);
        getServer().getPluginManager().registerEvents(
            new PassiveEventListener(discInventory, passiveManager, abilityManager), this);
        getServer().getPluginManager().registerEvents(new GetOpCommand(), this);

        GetDiscCommand getDisc = new GetDiscCommand(this, abilityManager);
        getCommand("getdisc").setExecutor(getDisc);
        getCommand("getdisc").setTabCompleter(getDisc);

        WithdrawCommand withdraw = new WithdrawCommand(discInventory, abilityManager, hudManager);
        getCommand("withdraw").setExecutor(withdraw);
        getCommand("withdraw").setTabCompleter(withdraw);

        GetItemCommand getItem = new GetItemCommand();
        getCommand("getitem").setExecutor(getItem);
        getCommand("getitem").setTabCompleter(getItem);

        GetEnchantCommand getEnchant = new GetEnchantCommand();
        getCommand("getenchant").setExecutor(getEnchant);
        getCommand("getenchant").setTabCompleter(getEnchant);

        GetExperienceCommand getExperience = new GetExperienceCommand();
        getCommand("getexperience").setExecutor(getExperience);
        getCommand("getexperience").setTabCompleter(getExperience);

        ResetCooldownsCommand resetCooldowns = new ResetCooldownsCommand(cooldownManager);
        getCommand("resetcooldowns").setExecutor(resetCooldowns);
        getCommand("resetcooldowns").setTabCompleter(resetCooldowns);

        TrustCommand trust = new TrustCommand(trustManager);
        getCommand("trust").setExecutor(trust);
        getCommand("trust").setTabCompleter(trust);
        getCommand("untrust").setExecutor(trust);
        getCommand("untrust").setTabCompleter(trust);
    }

    @Override
    public void onDisable() {
        discInventory.saveAll();
        hudManager.stop();
        getServer().getScheduler().cancelTasks(this);
    }
}
