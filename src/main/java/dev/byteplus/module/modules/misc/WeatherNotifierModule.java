package dev.byteplus.module.modules.misc;

import dev.byteplus.hud.Notifications;
import dev.byteplus.module.Category;
import dev.byteplus.module.Module;

public class WeatherNotifierModule extends Module {
   private int last = -1;

   public WeatherNotifierModule() {
      super("Weather Notifier", "Shows a banner when the weather changes.", Category.MISC);
   }

   @Override
   protected void onEnable() {
      this.last = -1;
   }

   @Override
   public void onJoin() {
      this.last = -1;
   }

   @Override
   public void onTick() {
      int now = mc.field_1687.method_8546() ? 2 : (mc.field_1687.method_8419() ? 1 : 0);
      if (this.last != -1 && now != this.last) {
         switch (now) {
            case 1:
               Notifications.banner("Weather", "It started raining", "textures/item/water_bucket.png");
               break;
            case 2:
               Notifications.banner("Weather", "A thunderstorm rolled in", "textures/item/trident.png");
               break;
            default:
               Notifications.banner("Weather", "The skies cleared up", "textures/block/sunflower_front.png");
         }
      }

      this.last = now;
   }
}
