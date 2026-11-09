package dev.byteplus.module.modules.anticheat;

import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.setting.NumberSetting;
import net.minecraft.class_746;

public class AirJumpModule extends Module {
   public final NumberSetting maxJumps = this.add(new NumberSetting("Max Air Jumps", 0.0, 0.0, 10.0, 1.0));
   private boolean wasDown;
   private int used;

   public AirJumpModule() {
      super("AirJump", "Jump again mid-air (Max Air Jumps 0 = unlimited).", Category.ANTICHEAT);
   }

   @Override
   public void onTick() {
      class_746 p = mc.field_1724;
      boolean down = mc.field_1690.field_1903.method_1434() && mc.field_1755 == null && !Move.freecam();
      if (p.method_24828()) {
         this.used = 0;
      }

      if (down
         && !this.wasDown
         && !p.method_24828()
         && !p.method_5799()
         && !p.method_5771()
         && !p.method_31549().field_7479
         && !p.method_5765()
         && (this.maxJumps.getInt() == 0 || this.used < this.maxJumps.getInt())) {
         p.method_6043();
         p.method_38785();
         this.used++;
      }

      this.wasDown = down;
   }
}
