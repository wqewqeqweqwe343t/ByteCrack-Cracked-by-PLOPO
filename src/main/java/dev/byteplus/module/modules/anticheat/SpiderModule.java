package dev.byteplus.module.modules.anticheat;

import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.setting.NumberSetting;
import net.minecraft.class_243;
import net.minecraft.class_746;

public class SpiderModule extends Module {
   public final NumberSetting speed = this.add(new NumberSetting("Speed", 0.2, 0.05, 1.0, 0.05));

   public SpiderModule() {
      super("Spider", "Climb walls like a spider by walking into them.", Category.ANTICHEAT);
   }

   @Override
   public void onTick() {
      class_746 p = mc.field_1724;
      if (!Move.freecam() && p.field_5976 && !p.method_31549().field_7479) {
         class_243 v = p.method_18798();
         p.method_18800(v.field_1352, this.speed.get(), v.field_1350);
         p.method_38785();
      }
   }
}
