package dev.byteplus.module.modules.render;

import dev.byteplus.BytePlus;
import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.setting.NumberSetting;

public class SwingSpeedModule extends Module {
   private final NumberSetting speed = this.add(new NumberSetting("Speed", 0.6, 0.1, 3.0, 0.05));

   public SwingSpeedModule() {
      super("SwingSpeed", "Changes your swing animation speed (visual only). Lower = slower, 1.0 = vanilla.", Category.RENDER);
   }

   public static int override() {
      if (BytePlus.modules() == null) {
         return -1;
      }

      SwingSpeedModule m = BytePlus.modules().swingSpeed;
      return m != null && m.isEnabled() ? Math.max(1, Math.min(120, Math.round(6.0F / m.speed.getFloat()))) : -1;
   }
}
