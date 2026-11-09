package dev.byteplus.module.modules.anticheat;

import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.setting.NumberSetting;
import net.minecraft.class_243;
import net.minecraft.class_2664;
import net.minecraft.class_2743;
import net.minecraft.class_310;
import net.minecraft.class_746;

public class VelocityModule extends Module {
   public final NumberSetting horizontal = this.add(new NumberSetting("Horizontal %", 0.0, 0.0, 100.0, 5.0));
   public final NumberSetting vertical = this.add(new NumberSetting("Vertical %", 0.0, 0.0, 100.0, 5.0));
   private static VelocityModule instance;

   public VelocityModule() {
      super("Velocity", "Reduce or cancel knockback.", Category.ANTICHEAT);
      instance = this;
   }

   private static boolean active() {
      return instance != null && instance.isEnabled() && class_310.method_1551().field_1724 != null;
   }

   public static boolean onMotion(class_2743 packet) {
      class_310 mc = class_310.method_1551();
      if (active() && mc.method_18854() && packet.method_11818() == mc.field_1724.method_5628()) {
         double h = instance.horizontal.get() / 100.0;
         double v = instance.vertical.get() / 100.0;
         class_243 m = packet.method_73085();
         if (h > 0.0 || v > 0.0) {
            mc.field_1724.method_18800(m.field_1352 * h, m.field_1351 * v, m.field_1350 * h);
         }

         return true;
      } else {
         return false;
      }
   }

   public static void afterExplosion(class_2664 packet) {
      if (active()) {
         packet.comp_2884().ifPresent(kb -> {
            class_746 p = class_310.method_1551().field_1724;
            double h = 1.0 - instance.horizontal.get() / 100.0;
            double v = 1.0 - instance.vertical.get() / 100.0;
            p.method_18799(p.method_18798().method_1023(kb.field_1352 * h, kb.field_1351 * v, kb.field_1350 * h));
         });
      }
   }
}
