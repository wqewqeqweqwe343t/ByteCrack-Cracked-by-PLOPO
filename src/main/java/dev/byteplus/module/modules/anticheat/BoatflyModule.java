package dev.byteplus.module.modules.anticheat;

import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.setting.BooleanSetting;
import dev.byteplus.setting.NumberSetting;
import net.minecraft.class_10255;
import net.minecraft.class_1297;

public class BoatflyModule extends Module {
   public final NumberSetting speed = this.add(new NumberSetting("Speed", 1.0, 0.1, 5.0, 0.1));
   public final NumberSetting verticalSpeed = this.add(new NumberSetting("Vertical Speed", 0.5, 0.1, 3.0, 0.1));
   public final NumberSetting glide = this.add(new NumberSetting("Glide", 0.02, 0.0, 0.3, 0.01));
   public final BooleanSetting throughWalls = this.add(new BooleanSetting("Through Walls", false));
   public final BooleanSetting faceCamera = this.add(new BooleanSetting("Face Camera", true));
   private class_1297 phased;

   public BoatflyModule() {
      super("Boatfly", "Fly with your boat (space up, Ctrl down). Can also pass through walls.", Category.ANTICHEAT);
   }

   @Override
   public void onTick() {
      if (mc.field_1724.method_5854() instanceof class_10255 boat) {
         if (Move.freecam()) {
            boat.method_18800(0.0, 0.0, 0.0);
         } else {
            if (this.faceCamera.isOn()) {
               boat.method_36456(mc.field_1724.method_36454());
            }

            double[] d = Move.dir(this.speed.get() * 0.5);
            double y = -this.glide.get();
            if (mc.field_1690.field_1903.method_1434()) {
               y = this.verticalSpeed.get() * 0.5;
            } else if (mc.field_1690.field_1867.method_1434()) {
               y = -this.verticalSpeed.get() * 0.5;
            }

            boat.method_18800(d[0], y, d[1]);
            boat.method_38785();
            boat.field_5960 = this.throughWalls.isOn();
            if (boat.field_5960) {
               this.phased = boat;
            } else {
               this.unphase();
            }
         }
      } else {
         this.unphase();
      }
   }

   private void unphase() {
      if (this.phased != null) {
         this.phased.field_5960 = false;
         this.phased = null;
      }
   }

   @Override
   protected void onDisable() {
      this.unphase();
   }
}
