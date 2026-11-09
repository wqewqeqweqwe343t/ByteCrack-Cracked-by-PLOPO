package dev.byteplus.module.modules.byteplus;

import dev.byteplus.BytePlus;
import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.setting.BooleanSetting;
import dev.byteplus.setting.StringSetting;
import dev.byteplus.util.DiscordIpc;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DiscordPresenceModule extends Module {
   public static final String BUILT_IN_APP_ID = "";
   private final StringSetting appId = this.add(new StringSetting("Application ID", "", 24).visibleWhen(() -> "".isEmpty()));
   private final StringSetting details = this.add(new StringSetting("Details", "Using Byte+", 48));
   private final BooleanSetting showServer = this.add(new BooleanSetting("Show Server", true));
   private final StringSetting imageKey = this.add(new StringSetting("Image Key", "logo", 32));
   private final ExecutorService worker = Executors.newSingleThreadExecutor(r -> {
      Thread t = new Thread(r, "Byte+ Discord");
      t.setDaemon(true);
      return t;
   });
   private DiscordIpc ipc;
   private String connectedId = "";
   private long start;
   private int timer;
   private String lastSent = "";

   public DiscordPresenceModule() {
      super("Discord Presence", "Shows Byte+ on your Discord profile.", Category.MISC);
   }

   @Override
   protected void onEnable() {
      this.start = System.currentTimeMillis() / 1000L;
      this.timer = 0;
      this.lastSent = "";
      if (this.id().isEmpty()) {
         this.info("Set your Discord Application ID first");
      }
   }

   @Override
   protected void onDisable() {
      this.worker.execute(this::disconnect);
   }

   private String id() {
      return "".isEmpty() ? this.appId.get().trim() : "";
   }

   private void disconnect() {
      if (this.ipc != null) {
         this.ipc.close();
      }

      this.ipc = null;
      this.connectedId = "";
   }

   @Override
   public void onTickAlways() {
      if (--this.timer <= 0) {
         this.timer = 100;
         String id = this.id();
         if (!id.isEmpty()) {
            String state;
            if (mc.field_1687 == null) {
               state = "In the menus";
            } else if (mc.method_1496()) {
               state = "Singleplayer";
            } else if (this.showServer.isOn() && BytePlus.lastServer != null) {
               state = "Playing on " + BytePlus.lastServer.field_3761;
            } else {
               state = "Playing multiplayer";
            }

            String det = this.details.get();
            String key = this.imageKey.get();
            String payload = id + "|" + det + "|" + state + "|" + key;
            if (!payload.equals(this.lastSent) || this.ipc == null) {
               this.worker.execute(() -> {
                  try {
                     if (this.ipc == null || !this.connectedId.equals(id)) {
                        this.disconnect();
                        this.ipc = DiscordIpc.connect(id);
                        this.connectedId = id;
                     }

                     this.ipc.setActivity(det, state, key, "Byte+", this.start);
                     this.lastSent = payload;
                  } catch (Exception e) {
                     this.disconnect();
                     this.lastSent = "";
                  }
               });
            }
         }
      }
   }
}
