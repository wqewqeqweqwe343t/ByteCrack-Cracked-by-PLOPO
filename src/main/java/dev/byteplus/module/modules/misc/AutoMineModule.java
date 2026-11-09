package dev.byteplus.module.modules.misc;

import dev.byteplus.module.Category;
import dev.byteplus.module.Module;

public class AutoMineModule extends Module {
   public AutoMineModule() {
      super("Auto Mine", "Holds attack so you keep mining without stopping.", Category.MISC);
   }

   @Override
   public void onTick() {
      if (mc.field_1755 == null) {
         mc.field_1690.field_1886.method_23481(true);
      }
   }

   @Override
   protected void onDisable() {
      mc.field_1690.field_1886.method_23481(false);
   }

   @Override
   public boolean persistEnabled() {
      return false;
   }
}
