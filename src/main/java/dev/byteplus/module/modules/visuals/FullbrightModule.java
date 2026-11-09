package dev.byteplus.module.modules.visuals;

import dev.byteplus.mixin.OptionInstanceAccessor;
import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.setting.NumberSetting;

public class FullbrightModule extends Module {
   public final NumberSetting brightness = this.add(new NumberSetting("Brightness", 12.0, 1.0, 16.0, 0.5));
   private Double previous;

   public FullbrightModule() {
      super("Fullbright", "See clearly in caves and at night.", Category.VISUALS);
   }

   @Override
   protected void onEnable() {
      if (mc.field_1690 != null) {
         this.previous = (Double)mc.field_1690.method_42473().method_41753();
         this.apply();
      }
   }

   @Override
   public void onTickAlways() {
      this.apply();
   }

   private void apply() {
      if (mc.field_1690 != null) {
         if (this.previous == null) {
            this.previous = Math.min(1.0, (Double)mc.field_1690.method_42473().method_41753());
         }

         double v = this.brightness.get();
         if ((Double)mc.field_1690.method_42473().method_41753() != v) {
            ((OptionInstanceAccessor)(Object)mc.field_1690.method_42473()).byteplus$setValue(v);
         }
      }
   }

   @Override
   protected void onDisable() {
      if (mc.field_1690 != null) {
         ((OptionInstanceAccessor)(Object)mc.field_1690.method_42473()).byteplus$setValue(this.previous != null ? Math.min(1.0, this.previous) : 0.5);
      }
   }
}
