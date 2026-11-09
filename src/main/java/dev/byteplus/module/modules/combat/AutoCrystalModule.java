package dev.byteplus.module.modules.combat;

import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.setting.BooleanSetting;
import dev.byteplus.setting.NumberSetting;
import dev.byteplus.util.InvUtil;
import net.minecraft.class_1268;
import net.minecraft.class_1511;
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_239;
import net.minecraft.class_2680;
import net.minecraft.class_3965;
import net.minecraft.class_3966;
import net.minecraft.class_239.class_240;

public class AutoCrystalModule extends Module {
   private final NumberSetting placeCps = this.add(new NumberSetting("Place Speed", 20.0, 1.0, 40.0, 1.0));
   private final NumberSetting breakCps = this.add(new NumberSetting("Break Speed", 20.0, 1.0, 40.0, 1.0));
   private final BooleanSetting requireUse = this.add(new BooleanSetting("Hold Use Key", true));
   private float placeTimer;
   private float breakTimer;

   public AutoCrystalModule() {
      super("Auto Crystal", "Places and breaks end crystals very fast.", Category.COMBAT);
   }

   @Override
   public void onFrame(float dt) {
      if (mc.field_1724 != null && mc.field_1687 != null && mc.field_1755 == null) {
         this.placeTimer -= dt;
         this.breakTimer -= dt;
         if (!this.requireUse.isOn() || mc.field_1690.field_1904.method_1434()) {
            if (mc.field_1724.method_6047().method_31574(class_1802.field_8301)) {
               class_239 hr = mc.field_1765;
               if (hr instanceof class_3966 ehr && ehr.method_17782() instanceof class_1511 crystal) {
                  if (this.breakTimer <= 0.0F) {
                     mc.field_1761.method_2918(mc.field_1724, crystal);
                     mc.field_1724.method_6104(class_1268.field_5808);
                     this.breakTimer = 1.0F / this.breakCps.getFloat();
                  }
               } else if (hr instanceof class_3965 bhr && bhr.method_17783() == class_240.field_1332) {
                  class_2680 state = mc.field_1687.method_8320(bhr.method_17777());
                  boolean base = state.method_27852(class_2246.field_10540) || state.method_27852(class_2246.field_9987);
                  if (base && mc.field_1687.method_22347(bhr.method_17777().method_10084()) && this.placeTimer <= 0.0F) {
                     InvUtil.interact(bhr);
                     this.placeTimer = 1.0F / this.placeCps.getFloat();
                  }
               }
            }
         }
      }
   }
}
