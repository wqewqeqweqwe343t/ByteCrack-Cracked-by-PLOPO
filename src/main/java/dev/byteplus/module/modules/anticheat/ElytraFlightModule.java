package dev.byteplus.module.modules.anticheat;

import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.setting.NumberSetting;
import net.minecraft.class_746;

public class ElytraFlightModule extends Module {
   public final NumberSetting speed = this.add(new NumberSetting("Speed", 1.5, 0.1, 5.0, 0.1));
   public final NumberSetting verticalSpeed = this.add(new NumberSetting("Vertical Speed", 0.8, 0.1, 3.0, 0.1));
   public final NumberSetting fallSpeed = this.add(new NumberSetting("Fall Speed", 0.0, 0.0, 0.5, 0.01));

   public ElytraFlightModule() {
      super("ElytraFlight", "Control your elytra freely: hover, go up and down, fly at any speed.", Category.ANTICHEAT);
   }

   @Override
   public void onTick() {
      class_746 p = mc.field_1724;
      if (p.method_6128()) {
         if (Move.freecam()) {
            p.method_18800(0.0, 0.0, 0.0);
         } else {
            double[] d = Move.dir(this.speed.get());
            double y = -this.fallSpeed.get();
            if (mc.field_1690.field_1903.method_1434()) {
               y = this.verticalSpeed.get();
            } else if (mc.field_1690.field_1832.method_1434()) {
               y = -this.verticalSpeed.get();
            }

            p.method_18800(d[0], y, d[1]);
         }
      }
   }
}
