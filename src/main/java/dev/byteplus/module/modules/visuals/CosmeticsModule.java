package dev.byteplus.module.modules.visuals;

import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.setting.BooleanSetting;
import dev.byteplus.setting.ModeSetting;
import dev.byteplus.setting.NumberSetting;
import dev.byteplus.util.Theme;
import java.awt.Color;
import java.util.Random;
import net.minecraft.class_2390;
import net.minecraft.class_2394;
import net.minecraft.class_2398;
import net.minecraft.class_2960;
import net.minecraft.class_8685;
import net.minecraft.class_12079.class_10726;
import net.minecraft.class_12079.class_12081;

public class CosmeticsModule extends Module {
   public final ModeSetting cape = this.add(
      new ModeSetting("Cape", "Byte+", "Byte+", "Galaxy", "Aurora", "Midnight", "Flame", "Ocean", "Sakura", "Mono", "None")
   );
   public final ModeSetting wings = this.add(new ModeSetting("Wings", "None", "None", "Angel", "Demon", "Byte+", "Crystal"));
   public final BooleanSetting halo = this.add(new BooleanSetting("Halo", false));
   public final ModeSetting haloColor = this.add(new ModeSetting("Halo Color", "Accent", "Accent", "Gold", "White", "Rainbow").visibleWhen(this::haloOn));
   public final ModeSetting trail = this.add(
      new ModeSetting("Trail", "Sparkle", "Sparkle", "Accent Dust", "Rainbow", "Hearts", "Flames", "Soul", "End Rod", "None")
   );
   public final NumberSetting density = this.add(new NumberSetting("Trail Density", 2.0, 1.0, 6.0, 1.0));
   private static CosmeticsModule instance;
   private final Random random = new Random();
   private int tick;

   public CosmeticsModule() {
      super("Cosmetics", "HD capes, 3D wings, a halo and particle trails (only you can see them).", Category.VISUALS);
      instance = this;
   }

   public static CosmeticsModule instance() {
      return instance;
   }

   private boolean haloOn() {
      return this.halo.isOn();
   }

   @Override
   public boolean showInArrayList() {
      return false;
   }

   public static class_8685 withCape(class_8685 skin) {
      if (instance != null && instance.isEnabled() && !instance.cape.is("None") && skin != null) {
         String file = instance.cape.get().toLowerCase().replace("+", "plus").replace(" ", "");
         class_12081 tex = new class_10726(class_2960.method_60655("byteplus", "cape/" + file));
         return new class_8685(skin.comp_1626(), tex, skin.comp_1628(), skin.comp_1629(), skin.comp_1630());
      } else {
         return null;
      }
   }

   @Override
   public void onTick() {
      if (!this.trail.is("None") && mc.field_1724 != null) {
         this.tick++;
         double speed = mc.field_1724.method_18798().method_37267();
         if (!(speed < 0.03) || !mc.field_1724.method_24828()) {
            for (int i = 0; i < this.density.getInt(); i++) {
               double x = mc.field_1724.method_23317() + (this.random.nextDouble() - 0.5) * 0.5;
               double y = mc.field_1724.method_23318() + 0.1 + this.random.nextDouble() * 0.4;
               double z = mc.field_1724.method_23321() + (this.random.nextDouble() - 0.5) * 0.5;
               mc.field_1687
                  .method_8466(
                     this.particle(i),
                     true,
                     true,
                     x,
                     y,
                     z,
                     (this.random.nextDouble() - 0.5) * 0.02,
                     0.015 + this.random.nextDouble() * 0.02,
                     (this.random.nextDouble() - 0.5) * 0.02
                  );
            }
         }
      }
   }

   private class_2394 particle(int i) {
      return (class_2394)(switch ((String)this.trail.get()) {
         case "Rainbow" -> new class_2390(Color.HSBtoRGB((this.tick * 3 + i * 20) % 360 / 360.0F, 0.7F, 1.0F) & 16777215, 1.3F);
         case "Accent Dust" -> new class_2390(Theme.ACCENT & 16777215, 1.3F);
         case "Hearts" -> class_2398.field_11201;
         case "Flames" -> class_2398.field_11240;
         case "Soul" -> class_2398.field_22246;
         case "End Rod" -> class_2398.field_11207;
         default -> i % 2 == 0 ? class_2398.field_29644 : class_2398.field_11207;
      });
   }
}
