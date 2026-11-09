package dev.byteplus.module.modules.combat;

import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.setting.NumberSetting;
import dev.byteplus.util.InvUtil;
import net.minecraft.class_1802;
import net.minecraft.class_490;

public class AutoTotemModule extends Module {
   private final NumberSetting delay = this.add(new NumberSetting("Delay", 1.0, 0.0, 10.0, 1.0));
   private int timer;

   public AutoTotemModule() {
      super("Auto Totem", "Keeps a totem in your offhand.", Category.COMBAT);
   }

   @Override
   public void onTick() {
      if (mc.field_1724.method_6079().method_31574(class_1802.field_8288)) {
         this.timer = this.delay.getInt();
      } else if (mc.field_1755 == null || mc.field_1755 instanceof class_490) {
         if (this.timer-- <= 0) {
            int slot = InvUtil.findInventory(s -> s.method_31574(class_1802.field_8288));
            if (slot != -1) {
               InvUtil.swapToOffhand(slot);
               this.timer = this.delay.getInt();
            }
         }
      }
   }
}
