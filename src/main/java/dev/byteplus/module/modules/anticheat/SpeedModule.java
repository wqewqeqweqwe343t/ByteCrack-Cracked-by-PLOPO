package dev.byteplus.module.modules.anticheat;

import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.setting.ModeSetting;
import dev.byteplus.setting.NumberSetting;
import net.minecraft.class_746;

public class SpeedModule extends Module {
   public final ModeSetting mode = this.add(new ModeSetting("Mode", "Strafe", "Strafe", "Ground"));
   public final NumberSetting speed = this.add(new NumberSetting("Speed", 1.3, 1.0, 4.0, 0.05));

   public SpeedModule() {
      super("Speed", "Move faster (auto-hop strafe or ground speed).", Category.ANTICHEAT);
   }

   @Override
   public void onTick() {
      class_746 p = mc.field_1724;
      if (!Move.freecam() && Move.moving() && !p.method_5765() && !p.method_31549().field_7479 && !p.method_6128() && !p.method_5799() && !p.method_5771()) {
         boolean strafe = this.mode.is("Strafe");
         if (strafe && p.method_24828()) {
            p.method_6043();
         }

         if (strafe || p.method_24828()) {
            double base = !p.method_5624() && !strafe ? 0.2158 : 0.2873;
            double[] d = Move.dir(base * this.speed.get());
            p.method_18800(d[0], p.method_18798().field_1351, d[1]);
         }
      }
   }
}
