package dev.byteplus;

import dev.byteplus.config.Config;
import dev.byteplus.dev.DevCapture;
import dev.byteplus.module.ModuleManager;
import dev.byteplus.util.TickRate;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.ClientStopping;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.Join;
import net.minecraft.class_642;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BytePlus implements ClientModInitializer {
   public static final String NAME = "Cracked By PLOPO";
   public static final Logger LOGGER = LoggerFactory.getLogger("Cracked By PLOPO");
   private static ModuleManager modules;
   public static class_642 lastServer;

   public void onInitializeClient() {
      modules = new ModuleManager();
      modules.hud.setEnabled(true);
      Config.load();
      DevCapture.init();
      ClientTickEvents.END_CLIENT_TICK.register((EndTick)client -> {
         if (client.field_1724 != null && client.field_1687 != null) {
            modules.onTick();
         }

         modules.onTickAlways();
         DevCapture.tick();
      });
      ClientPlayConnectionEvents.JOIN.register((Join)(handler, sender, client) -> {
         TickRate.reset();
         if (client.method_1558() != null) {
            lastServer = client.method_1558();
            modules.clickGui.lastServer.set(lastServer.field_3761);
            client.execute(Config::save);
         }

         client.execute(modules::onJoin);
      });
      ClientLifecycleEvents.CLIENT_STOPPING.register((ClientStopping)client -> Config.save());
      LOGGER.info("{} loaded with {} modules", "Cracked By PLOPO", modules.all().size());
   }

   public static ModuleManager modules() {
      return modules;
   }
}
