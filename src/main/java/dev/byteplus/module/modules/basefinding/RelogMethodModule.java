package dev.byteplus.module.modules.basefinding;

import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.module.modules.misc.AutoReconnectModule;
import dev.byteplus.setting.NumberSetting;
import net.minecraft.class_2561;

public class RelogMethodModule extends Module {
   private final NumberSetting yLevel = this.add(new NumberSetting("Y Level", -4.0, -64.0, 320.0, 1.0));
   private final NumberSetting rejoinDelay = this.add(new NumberSetting("Rejoin Delay", 3.0, 1.0, 30.0, 1.0));
   private final NumberSetting scanWait = this.add(new NumberSetting("Scan Wait", 10.0, 3.0, 60.0, 1.0));
   private int graceTicks;
   private boolean armed;
   private double lastY = Double.NaN;

   public RelogMethodModule() {
      super("Relog Method", "Relogs once when you pass a Y level so chunks reload.", Category.BASEFINDING);
   }

   @Override
   protected void onEnable() {
      this.graceTicks = 20;
      this.armed = mc.field_1724 == null || mc.field_1724.method_23318() > this.yLevel.get();
      this.lastY = Double.NaN;
   }

   @Override
   public void onJoin() {
      this.graceTicks = this.scanWait.getInt() * 20;
      this.armed = false;
      this.lastY = Double.NaN;
   }

   @Override
   public void onTick() {
      double y = mc.field_1724.method_23318();
      double threshold = this.yLevel.get();
      if (this.graceTicks > 0) {
         this.graceTicks--;
         this.lastY = y;
         if (y > threshold) {
            this.armed = true;
         }
      } else {
         if (y > threshold) {
            this.armed = true;
         }

         boolean crossed = !Double.isNaN(this.lastY) && this.lastY > threshold && y <= threshold;
         this.lastY = y;
         if (this.armed && crossed && mc.method_1562() != null) {
            this.armed = false;
            AutoReconnectModule.requestReconnect(this.rejoinDelay.getFloat());
            mc.method_1562().method_48296().method_10747(class_2561.method_43470("Relog"));
         }
      }
   }
}
