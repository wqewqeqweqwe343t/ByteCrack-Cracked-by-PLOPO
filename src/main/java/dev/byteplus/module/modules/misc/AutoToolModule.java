package dev.byteplus.module.modules.misc;

import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.setting.BooleanSetting;
import dev.byteplus.util.InvUtil;
import net.minecraft.class_1661;
import net.minecraft.class_1799;
import net.minecraft.class_2680;
import net.minecraft.class_3965;
import net.minecraft.class_239.class_240;

public class AutoToolModule extends Module {
   private final BooleanSetting switchBack = this.add(new BooleanSetting("Switch Back", true));
   private final BooleanSetting saveTools = this.add(new BooleanSetting("Save Low Durability", true));
   private int previous = -1;

   public AutoToolModule() {
      super("Auto Tool", "Switches to the best tool in your hotbar for the block you mine.", Category.MISC);
   }

   @Override
   public void onTick() {
      boolean mining = mc.field_1690.field_1886.method_1434() && mc.field_1765 instanceof class_3965 b && b.method_17783() == class_240.field_1332;
      if (!mining) {
         if (this.previous != -1 && this.switchBack.isOn()) {
            InvUtil.select(this.previous);
         }

         this.previous = -1;
      } else {
         class_2680 state = mc.field_1687.method_8320(((class_3965)mc.field_1765).method_17777());
         if (!state.method_26215()) {
            class_1661 inv = mc.field_1724.method_31548();
            int best = InvUtil.selected();
            float bestSpeed = speed(inv.method_5438(best), state);

            for (int i = 0; i < 9; i++) {
               class_1799 s = inv.method_5438(i);
               if (!this.saveTools.isOn() || !s.method_7963() || s.method_7936() - s.method_7919() >= 10) {
                  float sp = speed(s, state);
                  if (sp > bestSpeed + 0.01F) {
                     bestSpeed = sp;
                     best = i;
                  }
               }
            }

            if (best != InvUtil.selected()) {
               if (this.previous == -1) {
                  this.previous = InvUtil.selected();
               }

               InvUtil.select(best);
            }
         }
      }
   }

   private static float speed(class_1799 s, class_2680 state) {
      return s.method_7924(state);
   }
}
