package dev.byteplus.module.modules.anticheat;

import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.setting.NumberSetting;
import net.minecraft.class_746;
import net.minecraft.class_2828.class_5911;

public class NoFallModule extends Module {
   public final NumberSetting trigger = this.add(new NumberSetting("Trigger Distance", 2.0, 1.0, 2.9, 0.1));

   public NoFallModule() {
      super("NoFall", "Takes no fall damage.", Category.ANTICHEAT);
   }

   @Override
   public void onTick() {
      class_746 p = mc.field_1724;
      if (!(p.field_6017 < this.trigger.get()) && !p.method_6128() && !p.method_31549().field_7479 && mc.method_1562() != null) {
         mc.method_1562().method_52787(new class_5911(true, p.field_5976));
         p.method_38785();
      }
   }
}
