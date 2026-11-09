package dev.byteplus.module.modules.anticheat;

import dev.byteplus.mixin.MultiPlayerGameModeAccessor;
import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.setting.NumberSetting;

public class SpeedMineModule extends Module {
   public final NumberSetting breakAt = this.add(new NumberSetting("Break At %", 70.0, 10.0, 100.0, 5.0));

   public SpeedMineModule() {
      super("SpeedMine", "Break blocks faster, with no delay between them.", Category.ANTICHEAT);
   }

   @Override
   public void onTick() {
      if (mc.field_1761 != null) {
         MultiPlayerGameModeAccessor acc = (MultiPlayerGameModeAccessor)mc.field_1761;
         acc.byteplus$setDestroyDelay(0);
         if (mc.field_1761.method_2923() && acc.byteplus$destroyProgress() >= this.breakAt.get() / 100.0) {
            acc.byteplus$setDestroyProgress(1.0F);
         }
      }
   }
}
