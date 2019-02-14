package us.celestiamc.casinoproj.Main;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.plugin.RegisteredServiceProvider;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.permission.Permission;

import java.util.HashMap;

public class Casino extends JavaPlugin {
    private static final Logger log = Logger.getLogger("Minecraft");
    private static Economy econ = null;
    HashMap<String, HashMap<String, Integer>> pots = new HashMap<String, HashMap<String, Integer>>();

    public void onEnable() {
        pots.put("low", new HashMap<String, Integer>());
        pots.put("high", new HashMap<String, Integer>());
        getLogger().info("onEnable is called!");
        if (!setupEconomy()) {
            log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        long tickspersecond = 20;
        long ticksperminute = tickspersecond * 60;
        //long delay = ticksperminute * 5;
        long delay = tickspersecond * 20;
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
                    public void run() {
                        getLogger().info("Paying jackpot...");
                        payJackpot("low");
                        resetJackpot("low");
                        getLogger().info("Payed jackpot!");
                    }
                },
                delay,
                delay);
    }


    public void onDisable() {
        getLogger().info("onDisable is called!");
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("jackpot")) {
            if (sender instanceof Player) {
                getLogger().info(java.util.Arrays.toString(args));
                if (args[0].equalsIgnoreCase("joinlow")) {
                    Player player = (Player) sender;
                    String senderName = sender.getName();
                    HashMap<String, Integer> playersInLow = pots.get("low");
                    if (!playersInLow.containsKey(senderName)) {
                        int money = Integer.parseInt(args[1]);
                        int balance = (int)econ.getBalance(player);
                        if (money <= balance){
                            EconomyResponse r = econ.withdrawPlayer(senderName, money);
                            playersInLow.put(senderName, money);
                            player.sendMessage("Entered the Jackpot!");
                        }else{
                            player.sendMessage("Not enough money!");
                        }
                    } else {
                        player.sendMessage("You are already in the Jackpot!");
                    }
                } else {

                }

            } else {
                sender.sendMessage("Only players can use the jackpot!");
            }
        }
        return true;
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        getLogger().info("Plugin Loaded!");
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        getLogger().info("Plugin Registered!");
        econ = rsp.getProvider();
        return econ != null;
    }

    public static Economy getEconomy() {
        return econ;
    }

    public String drawJackpot(String potName) {
        Random generator = new Random();
        HashMap<String, Integer> pot = pots.get(potName);


        getLogger().info("pot.toString(): " + pot.toString());

        getLogger().info("pot.keySet().toString(): " + pot.keySet().toString());

        getLogger().info("pot.keySet().toArray().toString(): " + pot.keySet().toArray().toString());

        String[] users = (String[])pot.keySet().toArray(new String[0]);
        return users[new Random().nextInt(users.length)];

//        Map.Entry[] entries = (Map.Entry[]) pot.entrySet().toArray();
//        return (String) entries[generator.nextInt(entries.length)].getKey();
    }
    public void payJackpot(String potName){
        String username = drawJackpot(potName);
        Player player = getServer().getPlayer(username);
        EconomyResponse r = econ.depositPlayer(player, calculateJackpot(potName));
        player.sendMessage("Congrats! You won the jackpot.");
        Bukkit.broadcastMessage(username + " won the " + potName + " Jackpot!");
    }
    public int calculateJackpot(String potName){
        HashMap<String, Integer> pot = pots.get(potName);
        int sum = 0;
        for (int i : pot.values()){
            sum = sum+i;
        }
        return (sum);
    }
    public void resetJackpot(String potName){
        HashMap<String, Integer> pot = pots.get(potName);
        pot.clear();
    }
}


