package com.snowgears.duomfa;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


public class PlayerListener implements Listener {

    private DuoMFA plugin = DuoMFA.getPlugin();
    //player name, task id of auth status task
    private HashMap<String, Integer> playersNotAuthenticated = new HashMap<String, Integer>();

    public PlayerListener(DuoMFA instance) {
        plugin = instance;
    }

    public void cancelTaskAndAllowPlayer(Player player){
        //present login mfa title to the player
        String title = ChatColor.GREEN+"Success.";
        player.sendTitle(title, "", 10, 40, 10); //no title fade in or out. 5 minutes on screen

        if(playersNotAuthenticated.get(player.getName()) != null){
            DuoMFA.getPlugin().getServer().getScheduler().cancelTask(playersNotAuthenticated.get(player.getName()));
            playersNotAuthenticated. remove(player.getName());
        }
    }

    public void cancelTaskAndDenyPlayer(Player player){
        //deny player access with red title to contact admin
        String title = ChatColor.RED+"Denied.";
        String subtitle = ChatColor.RED+"Please log out.";
        player.sendTitle(title, subtitle, 0, 6000, 0); //no title fade in or out. 5 minutes on screen

        if(playersNotAuthenticated.get(player.getName()) != null){
            DuoMFA.getPlugin().getServer().getScheduler().cancelTask(playersNotAuthenticated.get(player.getName()));
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        final Player player = event.getPlayer();
        playersNotAuthenticated.put(event.getPlayer().getName(), 0);

        try {
            JSONObject preauth = DuoMFA.getPlugin().getDuoAuthClient().preauth(player.getName());
            //System.out.println(preauth.toString());
            String result = preauth.getString("result");
            if (result.equals("auth")){

                //get first device of user
                //(in future we would iterate and decide factor based on device capabilities associated with the user)
                JSONObject device1 = (JSONObject) preauth.getJSONArray("devices").get(0);
                String device1ID = device1.getString("device");

                //push an async auth to the player (async so it doesn't sleep the whole thread)
                JSONObject auth = DuoMFA.getPlugin().getDuoAuthClient().auth(player.getName(), "push", device1ID, 1);
                //System.out.println(device1ID);
                //System.out.println(auth);
                final String transactionID = auth.getString("txid");

                //present login mfa title to the player
                String title = ChatColor.GREEN+"MFA Required.";
                String subtitle = ChatColor.GREEN+"Login request pushed to your device.";
                player.sendTitle(title, subtitle, 0, 6000, 0); //no title fade in or out. 5 minutes on screen

                //kick off server repeating task to check status of auth for player
                final int taskID = DuoMFA.getPlugin().getServer().getScheduler().scheduleAsyncRepeatingTask(DuoMFA.getPlugin(), new Runnable() {
                    public void run() {

                        JSONObject authStatus = DuoMFA.getPlugin().getDuoAuthClient().auth_status(transactionID);
                        //System.out.println(authStatus);

                        try {
                            String result = authStatus.getString("result");
                            //if auth status is success, cancel repeating task
                            if (result.equals("allow"))
                                cancelTaskAndAllowPlayer(player);
                            else if (result.equals("deny")){
                                cancelTaskAndDenyPlayer(player);
                            }


                        } catch (JSONException je){
                            je.printStackTrace();
                        }
                    }
                }, 20L, 20L); //query after a second

                playersNotAuthenticated.put(event.getPlayer().getName(), taskID);

            } else if (result.equals("allow")){
                //allow player to passthrough and dont show them title
                if(playersNotAuthenticated.get(player.getName()) != null)
                    playersNotAuthenticated.remove(player.getName());
            } else {
                //deny player access with red title to contact admin
                String title = ChatColor.RED+"Denied.";
                String subtitle = ChatColor.RED+"Please contact your admin to enroll you in Duo MFA.";
                player.sendTitle(title, subtitle, 0, 6000, 0); //no title fade in or out. 5 minutes on screen
            }
        } catch (Exception e){

        }
    }

    public boolean isPlayerAuthenticated(Player player){
        if(playersNotAuthenticated.get(player.getName()) != null)
            return false;
        return true;
    }

    @EventHandler
    public void onDisplayChange(PlayerInteractEvent event) {
        if(!isPlayerAuthenticated(event.getPlayer()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if(!isPlayerAuthenticated(event.getPlayer()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerIne(InventoryInteractEvent event) {
        if(event.getWhoClicked() instanceof Player)
            if(!isPlayerAuthenticated((Player)event.getWhoClicked()))
                event.setCancelled(true);
    }
}