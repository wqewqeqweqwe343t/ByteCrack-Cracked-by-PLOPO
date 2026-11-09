package dev.byteplus.module.modules.misc;

import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.setting.NumberSetting;
import net.minecraft.class_10185;
import net.minecraft.class_241;
import net.minecraft.class_243;
import net.minecraft.class_315;

public class FreecamModule extends Module {
   public final NumberSetting speed = this.add(new NumberSetting("Speed", 8.0, 0.1, 60.0, 0.1));
   public final NumberSetting smoothness = this.add(new NumberSetting("Smoothness", 0.5, 0.0, 1.0, 0.05));
   public double x;
   public double y;
   public double z;
   public float yaw;
   public float pitch;
   private double vx;
   private double vy;
   private double vz;
   private boolean previousCull;
   private boolean active;
   private long lastNanos;
   public class_10185 heldInput;
   public class_241 heldMove;

   public FreecamModule() {
      super("Freecam", "Fly your camera around freely. Scroll to change speed.", Category.MISC);
   }

   @Override
   protected void onEnable() {
      if (mc.field_1724 == null) {
         this.setEnabled(false);
      } else {
         class_243 cam = mc.field_1773.method_19418().method_71156();
         this.x = cam.field_1352;
         this.y = cam.field_1351;
         this.z = cam.field_1350;
         this.vx = this.vy = this.vz = 0.0;
         this.yaw = mc.field_1724.method_36454();
         this.pitch = mc.field_1724.method_36455();
         this.heldInput = null;
         this.heldMove = null;
         this.previousCull = mc.field_1730;
         mc.field_1730 = false;
         if (mc.field_1769 != null) {
            mc.field_1769.method_3292();
         }

         this.active = true;
         this.lastNanos = System.nanoTime();
      }
   }

   @Override
   protected void onDisable() {
      if (this.active) {
         mc.field_1730 = this.previousCull;
         if (mc.field_1769 != null) {
            mc.field_1769.method_3292();
         }
      }

      this.active = false;
      this.heldInput = null;
      this.heldMove = null;
   }

   @Override
   public boolean persistEnabled() {
      return false;
   }

   public void captureInput(class_10185 current, class_241 move) {
      if (this.heldInput == null) {
         this.heldInput = new class_10185(
            current.comp_3159(), current.comp_3160(), current.comp_3161(), current.comp_3162(), false, current.comp_3164(), current.comp_3165()
         );
         this.heldMove = move;
      }
   }

   @Override
   public boolean onTurn(double dx, double dy) {
      this.yaw += (float)dx * 0.15F;
      this.pitch = Math.max(-90.0F, Math.min(90.0F, this.pitch + (float)dy * 0.15F));
      return true;
   }

   @Override
   public boolean onScroll(double amount) {
      double s = this.speed.get();
      double next = amount > 0.0 ? Math.max(s * 1.15, s + 0.1) : Math.min(s / 1.15, s - 0.1);
      this.speed.set(next);
      this.info(String.format("Speed %.1f", this.speed.get()));
      return true;
   }

   public void advance() {
      long now = System.nanoTime();
      float dt = Math.min(0.1F, (float)(now - this.lastNanos) / 1.0E9F);
      this.lastNanos = now;
      if (!(dt <= 0.0F)) {
         double tx = 0.0;
         double ty = 0.0;
         double tz = 0.0;
         if (mc.field_1755 == null) {
            class_315 o = mc.field_1690;
            double f = (o.field_1894.method_1434() ? 1 : 0) - (o.field_1881.method_1434() ? 1 : 0);
            double s = (o.field_1913.method_1434() ? 1 : 0) - (o.field_1849.method_1434() ? 1 : 0);
            double u = (o.field_1903.method_1434() ? 1 : 0) - (o.field_1832.method_1434() ? 1 : 0);
            double yawR = Math.toRadians(this.yaw);
            double fx = -Math.sin(yawR);
            double fz = Math.cos(yawR);
            double sx = Math.cos(yawR);
            double sz = Math.sin(yawR);
            double mx = fx * f + sx * s;
            double my = u;
            double mz = fz * f + sz * s;
            double len = Math.sqrt(mx * mx + my * my + mz * mz);
            double v = this.speed.get() * (o.field_1867.method_1434() ? 2.5 : 1.0);
            if (len > 1.0E-4) {
               tx = mx / len * v;
               ty = my / len * v;
               tz = mz / len * v;
            }
         }

         if (tx == 0.0 && ty == 0.0 && tz == 0.0) {
            this.vx = this.vy = this.vz = 0.0;
         } else {
            double sm = this.smoothness.get();
            double k = sm <= 0.0 ? 1.0 : 1.0 - Math.exp(-dt * (4.0 + (1.0 - sm) * 26.0));
            this.vx = this.vx + (tx - this.vx) * k;
            this.vy = this.vy + (ty - this.vy) * k;
            this.vz = this.vz + (tz - this.vz) * k;
            this.x = this.x + this.vx * dt;
            this.y = this.y + this.vy * dt;
            this.z = this.z + this.vz * dt;
         }
      }
   }
}
