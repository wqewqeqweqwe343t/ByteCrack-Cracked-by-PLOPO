package dev.byteplus.module.modules.anticheat;

import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.setting.NumberSetting;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_3965;
import net.minecraft.class_746;
import net.minecraft.class_239.class_240;
import net.minecraft.class_2828.class_2829;
import org.lwjgl.glfw.GLFW;

public class ClickTPModule extends Module {
   public final NumberSetting range = this.add(new NumberSetting("Range", 40.0, 5.0, 100.0, 5.0));
   public final NumberSetting stepSize = this.add(new NumberSetting("Step Size", 8.0, 2.0, 10.0, 1.0));
   private boolean wasDown;

   public ClickTPModule() {
      super("ClickTP", "Middle-click a block to teleport to it.", Category.ANTICHEAT);
   }

   @Override
   public void onTick() {
      boolean down = mc.field_1755 == null && !Move.freecam() && GLFW.glfwGetMouseButton(mc.method_22683().method_4490(), 2) == 1;
      if (down && !this.wasDown) {
         this.teleport();
      }

      this.wasDown = down;
   }

   private void teleport() {
      class_746 p = mc.field_1724;
      class_239 hit = p.method_5745(this.range.get(), 1.0F, false);
      if (hit instanceof class_3965 bh && hit.method_17783() == class_240.field_1332 && mc.method_1562() != null) {
         class_243 target = class_243.method_24955(bh.method_17777().method_10093(bh.method_17780()));
         class_243 from = p.method_73189();
         int steps = (int)Math.ceil(from.method_1022(target) / this.stepSize.get());

         for (int i = 1; i <= steps; i++) {
            class_243 at = from.method_35590(target, (double)i / steps);
            mc.method_1562().method_52787(new class_2829(at, i == steps, p.field_5976));
         }

         p.method_33574(target);
         p.method_18799(class_243.field_1353);
         p.method_38785();
      }
   }
}
