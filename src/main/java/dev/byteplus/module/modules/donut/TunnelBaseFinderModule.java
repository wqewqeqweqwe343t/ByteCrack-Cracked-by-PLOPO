package dev.byteplus.module.modules.donut;

import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.setting.BooleanSetting;
import dev.byteplus.setting.ModeSetting;
import dev.byteplus.setting.NumberSetting;
import dev.byteplus.util.WorldUtil;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_3965;
import net.minecraft.class_239.class_240;

public class TunnelBaseFinderModule extends Module {
   private final ModeSetting direction = this.add(new ModeSetting("Direction", "North", "North", "East", "South", "West"));
   private final BooleanSetting lavaStop = this.add(new BooleanSetting("Stop At Lava", true));
   private final NumberSetting smoothness = this.add(new NumberSetting("Smoothness", 0.8, 0.1, 1.0, 0.05));
   private boolean hasTarget;
   private class_2338 target;
   private float targetYaw;
   private float targetPitch;

   public TunnelBaseFinderModule() {
      super("Tunnel Base Finder", "Tunnels straight in one direction for you.", Category.DONUT);
   }

   private class_2350 dir() {
      return switch ((String)this.direction.get()) {
         case "East" -> class_2350.field_11034;
         case "South" -> class_2350.field_11035;
         case "West" -> class_2350.field_11039;
         default -> class_2350.field_11043;
      };
   }

   @Override
   protected void onEnable() {
      this.hasTarget = false;
      this.target = null;
   }

   @Override
   protected void onDisable() {
      mc.field_1690.field_1886.method_23481(false);
      mc.field_1690.field_1894.method_23481(false);
      this.hasTarget = false;
   }

   @Override
   public void onTick() {
      if (mc.field_1755 != null) {
         this.releaseKeys();
         this.hasTarget = false;
      } else {
         class_2350 d = this.dir();
         class_2338 feet = mc.field_1724.method_24515().method_10093(d);
         class_2338 head = feet.method_10084();
         if (this.lavaStop.isOn()) {
            for (class_2338 p : new class_2338[]{feet, head, feet.method_10093(d), head.method_10093(d), head.method_10084(), feet.method_10074()}) {
               if (mc.field_1687.method_8320(p).method_27852(class_2246.field_10164)) {
                  this.info("Lava ahead - stopped");
                  this.setEnabled(false);
                  return;
               }
            }
         }

         this.target = !mc.field_1687.method_8320(head).method_26215() ? head : (!mc.field_1687.method_8320(feet).method_26215() ? feet : null);
         class_243 eye = mc.field_1724.method_33571();
         this.targetYaw = d.method_10144();
         if (this.target != null) {
            this.targetPitch = WorldUtil.rotationTo(eye, this.target.method_10263() + 0.5, this.target.method_10264() + 0.5, this.target.method_10260() + 0.5)[1];
         } else {
            this.targetPitch = 0.0F;
         }

         this.hasTarget = true;
         float yawErr = Math.abs(class_3532.method_15393(this.targetYaw - mc.field_1724.method_36454()));
         if (this.target != null) {
            class_239 hit = mc.field_1765;
            boolean onTarget = hit instanceof class_3965 bh && hit.method_17783() == class_240.field_1332 && bh.method_17777().equals(this.target);
            mc.field_1690.field_1886.method_23481(onTarget);
            mc.field_1690.field_1894.method_23481(false);
         } else {
            mc.field_1690.field_1886.method_23481(false);
            mc.field_1690.field_1894.method_23481(yawErr < 4.0F);
         }
      }
   }

   @Override
   public void onFrame(float dt) {
      if (this.hasTarget && mc.field_1724 != null && mc.field_1755 == null) {
         float dYaw = class_3532.method_15393(this.targetYaw - mc.field_1724.method_36454());
         float dPitch = this.targetPitch - mc.field_1724.method_36455();
         if (!(Math.abs(dYaw) < 0.01F) || !(Math.abs(dPitch) < 0.01F)) {
            float rate = 2.0F + (1.0F - this.smoothness.getFloat()) * 16.0F;
            float k = 1.0F - (float)Math.exp(-dt * rate);
            float stepYaw = dYaw * k;
            float stepPitch = dPitch * k;
            float max = 240.0F * dt;
            float len = (float)Math.sqrt(stepYaw * stepYaw + stepPitch * stepPitch);
            if (len > max) {
               stepYaw *= max / len;
               stepPitch *= max / len;
            }

            mc.field_1724.method_5872(stepYaw / 0.15, stepPitch / 0.15);
         }
      }
   }

   private void releaseKeys() {
      mc.field_1690.field_1886.method_23481(false);
      mc.field_1690.field_1894.method_23481(false);
   }

   @Override
   public boolean persistEnabled() {
      return false;
   }
}
