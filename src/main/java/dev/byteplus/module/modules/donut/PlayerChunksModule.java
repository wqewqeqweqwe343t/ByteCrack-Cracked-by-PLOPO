package dev.byteplus.module.modules.donut;

import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.module.modules.misc.NameProtectModule;
import dev.byteplus.render.Esp;
import dev.byteplus.render.Render2D;
import dev.byteplus.setting.BooleanSetting;
import dev.byteplus.setting.NumberSetting;
import dev.byteplus.util.Theme;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_1657;

public class PlayerChunksModule extends Module {
   private final BooleanSetting remember = this.add(new BooleanSetting("Remember", true));
   private final NumberSetting memory = this.add(new NumberSetting("Memory (min)", 5.0, 1.0, 60.0, 1.0));
   private final BooleanSetting tracers = this.add(new BooleanSetting("Tracers", true));
   private final Map<String, PlayerChunksModule.Seen> seen = new HashMap<>();

   public PlayerChunksModule() {
      super("Player Chunks", "Shows chunks where players are or were.", Category.DONUT);
   }

   @Override
   protected void onDisable() {
      this.seen.clear();
   }

   @Override
   public void onJoin() {
      this.seen.clear();
   }

   @Override
   public void onTick() {
      long now = System.currentTimeMillis();

      for (class_1657 p : mc.field_1687.method_18456()) {
         if (p != mc.field_1724) {
            int cx = p.method_24515().method_10263() >> 4;
            int cz = p.method_24515().method_10260() >> 4;
            String name = p.method_5477().getString();
            this.seen.put(name, new PlayerChunksModule.Seen(cx, cz, p.method_23318(), name, now));
         }
      }

      long keep = this.remember.isOn() ? this.memory.getInt() * 60000L : 1500L;
      this.seen.values().removeIf(s -> now - s.time > keep);
   }

   @Override
   public void onRenderWorld() {
      long now = System.currentTimeMillis();

      for (PlayerChunksModule.Seen s : this.seen.values()) {
         boolean live = now - s.time < 1500L;
         int rgb = live ? 16734797 : 16763176;
         double x = s.cx * 16;
         double z = s.cz * 16;
         Esp.plate(x, z, x + 16.0, z + 16.0, s.y, rgb, live ? 50 : 25, live ? 230 : 140);
         if (this.tracers.isOn() && live) {
            Esp.tracer(x + 8.0, s.y, z + 8.0, rgb, 200);
         }
      }
   }

   @Override
   public void onRenderScreen(int fbW, int fbH, float guiScale) {
      for (PlayerChunksModule.Seen s : this.seen.values()) {
         float[] p = Esp.project(s.cx * 16 + 8, s.y + 2.5, s.cz * 16 + 8);
         if (p != null) {
            String name = NameProtectModule.replace(s.name);
            float ts = 6.5F * guiScale;
            float w = Render2D.textWidth(name, ts, 1);
            Render2D.text(name, p[0] - w / 2.0F, p[1] - ts / 2.0F, ts, Theme.HUD_ACCENT, 1);
         }
      }
   }

   private record Seen(int cx, int cz, double y, String name, long time) {
   }
}
