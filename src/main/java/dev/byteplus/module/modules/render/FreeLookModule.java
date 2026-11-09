package dev.byteplus.module.modules.render;

import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.setting.BooleanSetting;
import dev.byteplus.setting.NumberSetting;
import net.minecraft.class_1297;
import net.minecraft.class_243;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_5498;
import net.minecraft.class_239.class_240;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;

public class FreeLookModule extends Module {
   public final BooleanSetting throughWalls = this.add(new BooleanSetting("Through Walls", true));
   public final NumberSetting distance = this.add(new NumberSetting("Distance", 4.0, 1.0, 30.0, 0.5));
   public float yaw;
   public float pitch;
   private class_5498 previousType;
   private boolean previousCull;
   private boolean cullChanged;

   public FreeLookModule() {
      super("FreeLook", "Look around your player freely. Scroll to zoom in and out.", Category.RENDER);
   }

   @Override
   protected void onEnable() {
      if (mc.field_1724 == null) {
         this.setEnabled(false);
      } else {
         this.yaw = mc.field_1724.method_36454();
         this.pitch = mc.field_1724.method_36455();
         this.previousType = mc.field_1690.method_31044();
         mc.field_1690.method_31043(class_5498.field_26665);
         this.previousCull = mc.field_1730;
         mc.field_1730 = false;
         this.cullChanged = true;
         if (mc.field_1769 != null) {
            mc.field_1769.method_3292();
         }
      }
   }

   @Override
   protected void onDisable() {
      if (this.previousType != null) {
         mc.field_1690.method_31043(this.previousType);
      }

      this.previousType = null;
      if (this.cullChanged) {
         mc.field_1730 = this.previousCull;
         this.cullChanged = false;
         if (mc.field_1769 != null) {
            mc.field_1769.method_3292();
         }
      }
   }

   @Override
   public boolean persistEnabled() {
      return false;
   }

   @Override
   public boolean onTurn(double dx, double dy) {
      this.yaw += (float)dx * 0.15F;
      this.pitch = Math.max(-90.0F, Math.min(90.0F, this.pitch + (float)dy * 0.15F));
      return true;
   }

   @Override
   public boolean onScroll(double amount) {
      this.distance.set(this.distance.get() - amount * 0.5);
      return true;
   }

   public class_243 cameraPosition(class_1297 entity, float partial) {
      class_243 eye = entity.method_5836(partial);
      double yawR = Math.toRadians(this.yaw);
      double pitchR = Math.toRadians(this.pitch);
      class_243 forward = new class_243(-Math.sin(yawR) * Math.cos(pitchR), -Math.sin(pitchR), Math.cos(yawR) * Math.cos(pitchR));
      class_243 target = eye.method_1020(forward.method_1021(this.distance.get()));
      if (!this.throughWalls.isOn() && mc.field_1687 != null) {
         class_3965 hit = mc.field_1687.method_17742(new class_3959(eye, target, class_3960.field_23142, class_242.field_1348, entity));
         if (hit.method_17783() == class_240.field_1332) {
            return hit.method_17784().method_1019(forward.method_1021(0.2));
         }
      }

      return target;
   }
}
