package dev.byteplus.module.modules.misc;

import dev.byteplus.mixin.MinecraftAccessor;
import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.setting.NumberSetting;

public class FastPlaceModule extends Module {
   private final NumberSetting delay = this.add(new NumberSetting("Delay", 0.0, 0.0, 3.0, 1.0));

   public FastPlaceModule() {
      super("Fast Place", "Removes the delay between placing blocks and using items.", Category.MISC);
   }

   @Override
   public void onTick() {
      MinecraftAccessor acc = (MinecraftAccessor)mc;
      if (acc.byteplus$getRightClickDelay() > this.delay.getInt()) {
         acc.byteplus$setRightClickDelay(this.delay.getInt());
      }
   }
}
