package dev.byteplus.module.modules.anticheat;

import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.setting.BooleanSetting;
import dev.byteplus.setting.ModeSetting;
import dev.byteplus.setting.NumberSetting;
import net.minecraft.class_746;

public class FlightModule extends Module {
   public final ModeSetting mode = this.add(new ModeSetting("Mode", "Velocity", "Velocity", "Vanilla"));
   public final NumberSetting speed = this.add(new NumberSetting("Speed", 1.0, 0.1, 5.0, 0.1));
   public final NumberSetting verticalSpeed = this.add(new NumberSetting("Vertical Speed", 0.8, 0.1, 5.0, 0.1));
   public final BooleanSetting antiKick = this.add(new BooleanSetting("Anti Kick", true));
   private int ticks;

   public FlightModule() {
      super("Flight", "Fly in survival. Space to go up, sneak to go down.", Category.ANTICHEAT);
   }

   @Override
   public boolean persistEnabled() {
      return false;
   }

   @Override
   public void onTick() {
      class_746 p = mc.field_1724;
      this.ticks++;
      p.method_38785();
      if (!Move.freecam()) {
         if (this.mode.is("Vanilla")) {
            p.method_31549().field_7478 = true;
            p.method_31549().field_7479 = true;
            p.method_31549().method_7248(0.05F * this.speed.getFloat());
         } else {
            double[] d = Move.dir(this.speed.get() * 0.5);
            double y = 0.0;
            if (mc.field_1690.field_1903.method_1434()) {
               y += this.verticalSpeed.get() * 0.5;
            }

            if (mc.field_1690.field_1832.method_1434()) {
               y -= this.verticalSpeed.get() * 0.5;
            }

            if (this.antiKick.isOn() && y == 0.0 && this.ticks % 40 == 0) {
               y = -0.04;
            }

            p.method_18800(d[0], y, d[1]);
         }
      } else {
         p.method_18800(0.0, this.antiKick.isOn() && this.ticks % 40 == 0 ? -0.04 : 0.0, 0.0);
      }
   }

   @Override
   protected void onDisable() {
      class_746 p = mc.field_1724;
      if (p != null) {
         boolean canFly = p.method_68878() || p.method_7325();
         p.method_31549().field_7478 = canFly;
         p.method_31549().field_7479 = canFly && p.method_31549().field_7479;
         p.method_31549().method_7248(0.05F);
         p.method_7355();
         p.method_18800(0.0, Math.min(0.0, p.method_18798().field_1351), 0.0);
      }
   }
}
