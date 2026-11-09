package dev.byteplus.module.modules.misc;

import dev.byteplus.BytePlus;
import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.render.Render2D;
import dev.byteplus.setting.NumberSetting;
import dev.byteplus.util.Theme;
import net.minecraft.class_412;
import net.minecraft.class_419;
import net.minecraft.class_442;
import net.minecraft.class_639;
import net.minecraft.class_642;

public class AutoReconnectModule extends Module {
   private final NumberSetting delay = this.add(new NumberSetting("Delay", 5.0, 1.0, 60.0, 1.0));
   private float countdown = -1.0F;
   private static float forcedDelay = -1.0F;

   public AutoReconnectModule() {
      super("Auto Reconnect", "Rejoins the server after you get disconnected.", Category.MISC);
   }

   public static void requestReconnect(float seconds) {
      forcedDelay = seconds;
   }

   public void globalTick() {
      boolean onDisconnect = mc.field_1755 instanceof class_419;
      boolean active = this.isEnabled() || forcedDelay >= 0.0F;
      if (onDisconnect && active && BytePlus.lastServer != null) {
         if (this.countdown < 0.0F) {
            this.countdown = forcedDelay >= 0.0F ? forcedDelay : this.delay.getFloat();
         }

         this.countdown -= 0.05F;
         if (this.countdown <= 0.0F) {
            this.countdown = -1.0F;
            forcedDelay = -1.0F;
            class_642 data = BytePlus.lastServer;
            class_412.method_36877(new class_442(), mc, class_639.method_2950(data.field_3761), data, false, null);
         }
      } else {
         this.countdown = -1.0F;
      }
   }

   public void drawCountdown(float sw, float sh) {
      if (!(this.countdown < 0.0F) && mc.field_1755 instanceof class_419) {
         String text = "RECONNECTING IN " + String.format("%.1f", Math.max(0.0F, this.countdown)) + "S";
         float ts = 8.0F;
         float w = Render2D.textWidth(text, ts, 1, 0.6F) + 20.0F;
         float x = (sw - w) / 2.0F;
         float y = 10.0F;
         Render2D.shadow(x, y, w, 18.0F, 5.0F, 10.0F, 1610612736);
         Render2D.roundRect(x, y, w, 18.0F, 5.0F, Theme.HUD_BG);
         Render2D.roundRectOutline(x, y, w, 18.0F, 5.0F, 0.6F, Theme.HUD_OUTLINE);
         Render2D.text(text, x + 10.0F, y + (18.0F - ts) / 2.0F, ts, Theme.HUD_ACCENT, 1, 0.6F);
      }
   }
}
