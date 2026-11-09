package dev.byteplus.util;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1923;
import net.minecraft.class_243;
import net.minecraft.class_2806;
import net.minecraft.class_2818;
import net.minecraft.class_310;

public final class WorldUtil {
   private WorldUtil() {
   }

   public static List<class_2818> loadedChunks() {
      class_310 mc = class_310.method_1551();
      List<class_2818> out = new ArrayList<>();
      if (mc.field_1687 != null && mc.field_1724 != null) {
         int radius = mc.field_1690.method_38521() + 1;
         int pcx = mc.field_1724.method_24515().method_10263() >> 4;
         int pcz = mc.field_1724.method_24515().method_10260() >> 4;

         for (int r = 0; r <= radius; r++) {
            for (int cx = pcx - r; cx <= pcx + r; cx++) {
               for (int cz = pcz - r; cz <= pcz + r; cz++) {
                  if (Math.max(Math.abs(cx - pcx), Math.abs(cz - pcz)) == r) {
                     class_2818 c = mc.field_1687.method_2935().method_2857(cx, cz, class_2806.field_12803, false);
                     if (c != null) {
                        out.add(c);
                     }
                  }
               }
            }
         }

         return out;
      } else {
         return out;
      }
   }

   public static class_2818 chunk(int cx, int cz) {
      class_310 mc = class_310.method_1551();
      return mc.field_1687 == null ? null : mc.field_1687.method_2935().method_2857(cx, cz, class_2806.field_12803, false);
   }

   public static long key(int cx, int cz) {
      return class_1923.method_8331(cx, cz);
   }

   public static int keyX(long key) {
      return class_1923.method_8325(key);
   }

   public static int keyZ(long key) {
      return class_1923.method_8332(key);
   }

   public static double distSqToPlayer(double x, double y, double z) {
      class_243 p = class_310.method_1551().field_1724.method_73189();
      double dx = x - p.field_1352;
      double dy = y - p.field_1351;
      double dz = z - p.field_1350;
      return dx * dx + dy * dy + dz * dz;
   }

   public static float wrapDegrees(float deg) {
      deg %= 360.0F;
      if (deg >= 180.0F) {
         deg -= 360.0F;
      }

      if (deg < -180.0F) {
         deg += 360.0F;
      }

      return deg;
   }

   public static float[] rotationTo(class_243 eye, double x, double y, double z) {
      double dx = x - eye.field_1352;
      double dy = y - eye.field_1351;
      double dz = z - eye.field_1350;
      double h = Math.sqrt(dx * dx + dz * dz);
      float yaw = (float)Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
      float pitch = (float)(-Math.toDegrees(Math.atan2(dy, h)));
      return new float[]{yaw, pitch};
   }
}
