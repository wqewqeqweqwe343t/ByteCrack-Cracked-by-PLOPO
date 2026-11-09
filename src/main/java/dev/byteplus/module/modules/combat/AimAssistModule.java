package dev.byteplus.module.modules.combat;

import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.setting.BooleanSetting;
import dev.byteplus.setting.NumberSetting;
import dev.byteplus.util.Friends;
import dev.byteplus.util.WorldUtil;
import net.minecraft.class_1657;
import net.minecraft.class_238;
import net.minecraft.class_243;

public class AimAssistModule extends Module {
   private final NumberSetting range = this.add(new NumberSetting("Range", 4.5, 1.0, 8.0, 0.1));
   private final NumberSetting fov = this.add(new NumberSetting("FOV", 70.0, 10.0, 180.0, 1.0));
   private final NumberSetting speed = this.add(new NumberSetting("Speed", 6.0, 1.0, 20.0, 0.5));
   private final BooleanSetting vertical = this.add(new BooleanSetting("Vertical", true));
   private final BooleanSetting onlyAttacking = this.add(new BooleanSetting("Only When Attacking", false));

   public AimAssistModule() {
      super("Aim Assist", "Smoothly guides your aim onto nearby players.", Category.COMBAT);
   }

   @Override
   public void onFrame(float dt) {
      if (mc.field_1724 != null && mc.field_1687 != null && mc.field_1755 == null) {
         if (!this.onlyAttacking.isOn() || mc.field_1690.field_1886.method_1434()) {
            class_243 eye = mc.field_1724.method_33571();
            class_1657 best = null;
            float bestAngle = Float.MAX_VALUE;
            float[] bestRot = null;

            for (class_1657 p : mc.field_1687.method_18456()) {
               if (p != mc.field_1724 && p.method_5805() && !p.method_7325() && !Friends.isFriend(p) && !(mc.field_1724.method_5739(p) > this.range.getFloat())
                  )
                {
                  class_238 bb = p.method_5829();
                  double x = clamp(eye.field_1352, bb.field_1323 + 0.1, bb.field_1320 - 0.1);
                  double y = clamp(eye.field_1351, bb.field_1322 + 0.2, bb.field_1325 - 0.2);
                  double z = clamp(eye.field_1350, bb.field_1321 + 0.1, bb.field_1324 - 0.1);
                  float[] rot = WorldUtil.rotationTo(eye, x, y, z);
                  float dYaw = Math.abs(WorldUtil.wrapDegrees(rot[0] - mc.field_1724.method_36454()));
                  float dPitch = Math.abs(rot[1] - mc.field_1724.method_36455());
                  float angle = (float)Math.sqrt(dYaw * dYaw + dPitch * dPitch);
                  if (!(angle > this.fov.getFloat() / 2.0F) && angle < bestAngle) {
                     bestAngle = angle;
                     best = p;
                     bestRot = rot;
                  }
               }
            }

            if (best != null) {
               float k = 1.0F - (float)Math.exp(-this.speed.getFloat() * dt);
               float dYaw = WorldUtil.wrapDegrees(bestRot[0] - mc.field_1724.method_36454()) * k;
               float dPitch = this.vertical.isOn() ? (bestRot[1] - mc.field_1724.method_36455()) * k : 0.0F;
               mc.field_1724.method_5872(dYaw / 0.15, dPitch / 0.15);
            }
         }
      }
   }

   private static double clamp(double v, double min, double max) {
      return min > max ? (min + max) / 2.0 : Math.max(min, Math.min(max, v));
   }
}
