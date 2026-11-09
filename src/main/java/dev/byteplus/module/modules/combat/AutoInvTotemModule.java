package dev.byteplus.module.modules.combat;

import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.setting.BooleanSetting;
import dev.byteplus.setting.NumberSetting;
import dev.byteplus.util.InvUtil;
import net.minecraft.class_1661;
import net.minecraft.class_1802;
import net.minecraft.class_490;

public class AutoInvTotemModule extends Module {
   private final NumberSetting delay = this.add(new NumberSetting("Delay", 2.0, 0.0, 10.0, 1.0));
   private final BooleanSetting offhand = this.add(new BooleanSetting("Offhand", true));
   private final BooleanSetting hotbar = this.add(new BooleanSetting("Hotbar Totem", true));
   private final NumberSetting hotbarSlot = this.add(new NumberSetting("Hotbar Slot", 9.0, 1.0, 9.0, 1.0));
   private int timer;

   public AutoInvTotemModule() {
      super("Auto Inv Totem", "Equips totems when you open your inventory.", Category.COMBAT);
   }

   @Override
   public void onTick() {
      if (!(mc.field_1755 instanceof class_490)) {
         this.timer = this.delay.getInt();
      } else if (this.timer-- <= 0) {
         if (this.offhand.isOn() && !mc.field_1724.method_6079().method_31574(class_1802.field_8288)) {
            int slot = InvUtil.findInventory(s -> s.method_31574(class_1802.field_8288));
            if (slot != -1) {
               InvUtil.swapToOffhand(slot);
               this.timer = this.delay.getInt();
               return;
            }
         }

         int hb = this.hotbarSlot.getInt() - 1;
         if (this.hotbar.isOn() && !mc.field_1724.method_31548().method_5438(hb).method_31574(class_1802.field_8288)) {
            int slot = -1;
            class_1661 inv = mc.field_1724.method_31548();

            for (int i = 9; i < 36; i++) {
               if (inv.method_5438(i).method_31574(class_1802.field_8288)) {
                  slot = i;
                  break;
               }
            }

            if (slot != -1) {
               InvUtil.swapToHotbar(slot, hb);
               this.timer = this.delay.getInt();
            }
         }
      }
   }
}
