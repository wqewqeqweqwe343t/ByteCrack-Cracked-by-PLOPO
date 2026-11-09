package dev.byteplus.module.modules.anticheat;

import dev.byteplus.BytePlus;
import dev.byteplus.module.ModuleManager;
import net.minecraft.class_310;
import net.minecraft.class_315;

final class Move {
   private Move() {
   }

   private static class_310 mc() {
      return class_310.method_1551();
   }

   static boolean freecam() {
      ModuleManager m = BytePlus.modules();
      return m != null && m.freecam.isEnabled();
   }

   static boolean moving() {
      class_315 o = mc().field_1690;
      return o.field_1894.method_1434() || o.field_1881.method_1434() || o.field_1913.method_1434() || o.field_1849.method_1434();
   }

   static double[] dir(double speed) {
      class_315 o = mc().field_1690;
      float f = 0.0F;
      float s = 0.0F;
      if (o.field_1894.method_1434()) {
         f++;
      }

      if (o.field_1881.method_1434()) {
         f--;
      }

      if (o.field_1913.method_1434()) {
         s++;
      }

      if (o.field_1849.method_1434()) {
         s--;
      }

      if (f == 0.0F && s == 0.0F) {
         return new double[]{0.0, 0.0};
      }

      double len = Math.sqrt(f * f + s * s);
      f = (float)(f / len);
      s = (float)(s / len);
      double yaw = Math.toRadians(mc().field_1724.method_36454());
      double sin = Math.sin(yaw);
      double cos = Math.cos(yaw);
      return new double[]{(s * cos - f * sin) * speed, (f * cos + s * sin) * speed};
   }
}
