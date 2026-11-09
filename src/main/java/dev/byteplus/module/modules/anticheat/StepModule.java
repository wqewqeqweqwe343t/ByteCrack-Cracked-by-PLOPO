package dev.byteplus.module.modules.anticheat;

import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.setting.NumberSetting;
import net.minecraft.class_1324;
import net.minecraft.class_5134;

public class StepModule extends Module {
   public final NumberSetting height = this.add(new NumberSetting("Height", 1.0, 0.6, 3.0, 0.5));
   private static final double VANILLA = 0.6;

   public StepModule() {
      super("Step", "Step up blocks without jumping.", Category.ANTICHEAT);
   }

   @Override
   public void onTick() {
      class_1324 attr = mc.field_1724.method_5996(class_5134.field_47761);
      if (attr != null && attr.method_6201() != this.height.get()) {
         attr.method_6192(this.height.get());
      }
   }

   @Override
   protected void onDisable() {
      if (mc.field_1724 != null) {
         class_1324 attr = mc.field_1724.method_5996(class_5134.field_47761);
         if (attr != null) {
            attr.method_6192(0.6);
         }
      }
   }
}
