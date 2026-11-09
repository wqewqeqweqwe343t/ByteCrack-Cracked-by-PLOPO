package dev.byteplus.module.modules.combat;

import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.setting.BooleanSetting;
import dev.byteplus.setting.NumberSetting;
import dev.byteplus.util.Friends;
import net.minecraft.class_1268;
import net.minecraft.class_1309;
import net.minecraft.class_1657;

public class TriggerbotModule extends Module {
   private final NumberSetting range = this.add(new NumberSetting("Range", 3.0, 1.0, 6.0, 0.1));
   private final NumberSetting cooldown = this.add(new NumberSetting("Cooldown", 1.0, 0.5, 1.0, 0.05));
   private final BooleanSetting smartCrits = this.add(new BooleanSetting("Smart Crits", true));
   private final BooleanSetting playersOnly = this.add(new BooleanSetting("Players Only", true));

   public TriggerbotModule() {
      super("Triggerbot", "Hits players in your crosshair with full cooldown.", Category.COMBAT);
   }

   @Override
   public void onTick() {
      if (mc.field_1755 == null) {
         if (mc.field_1692 instanceof class_1309 target && target.method_5805()) {
            if (!this.playersOnly.isOn() || target instanceof class_1657) {
               if (!(target instanceof class_1657 fp && Friends.isFriend(fp))) {
                  if (!(mc.field_1724.method_5739(target) > this.range.getFloat())) {
                     if (!(mc.field_1724.method_7261(0.5F) < this.cooldown.getFloat())) {
                        if (!this.smartCrits.isOn()
                           || mc.field_1724.method_24828()
                           || mc.field_1724.method_5799()
                           || !(mc.field_1724.method_18798().field_1351 >= 0.0) && !(mc.field_1724.field_6017 <= 0.0)) {
                           mc.field_1761.method_2918(mc.field_1724, target);
                           mc.field_1724.method_6104(class_1268.field_5808);
                        }
                     }
                  }
               }
            }
         }
      }
   }
}
