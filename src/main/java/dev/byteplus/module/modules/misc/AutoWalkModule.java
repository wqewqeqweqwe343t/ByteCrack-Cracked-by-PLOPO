package dev.byteplus.module.modules.misc;

import dev.byteplus.module.Category;
import dev.byteplus.module.Module;

public class AutoWalkModule extends Module {
   public AutoWalkModule() {
      super("Auto Walk", "Walks forward without stopping.", Category.MISC);
   }

   @Override
   public void onTick() {
      if (mc.field_1755 == null) {
         mc.field_1690.field_1894.method_23481(true);
      }
   }

   @Override
   protected void onDisable() {
      mc.field_1690.field_1894.method_23481(false);
   }

   @Override
   public boolean persistEnabled() {
      return false;
   }
}
