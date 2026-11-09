package dev.byteplus.module.modules.visuals;

import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.setting.BooleanSetting;
import dev.byteplus.setting.ModeSetting;
import dev.byteplus.setting.NumberSetting;

public class CustomSkyModule extends Module {
   public static final String[] SKIES = new String[]{
      "Galaxy", "Aurora", "Nebula", "Starry Night", "Deep Space", "Sunset", "Synthwave", "Blood Moon", "Cotton Candy", "Void", "Black Hole", "Ringed Planet"
   };
   private static final float[][] HORIZON = new float[][]{
      {0.1F, 0.07F, 0.2F},
      {0.03F, 0.12F, 0.12F},
      {0.2F, 0.08F, 0.22F},
      {0.07F, 0.1F, 0.22F},
      {0.02F, 0.02F, 0.05F},
      {0.95F, 0.45F, 0.25F},
      {0.55F, 0.15F, 0.45F},
      {0.26F, 0.03F, 0.03F},
      {0.95F, 0.72F, 0.85F},
      {0.03F, 0.01F, 0.06F},
      {0.06F, 0.04F, 0.1F},
      {0.04F, 0.06F, 0.12F}
   };
   private static CustomSkyModule instance;
   public final ModeSetting sky = this.add(new ModeSetting("Sky", "Galaxy", SKIES));
   public final NumberSetting speed = this.add(new NumberSetting("Speed", 1.0, 0.0, 4.0, 0.1));
   public final NumberSetting brightness = this.add(new NumberSetting("Brightness", 1.0, 0.3, 2.0, 0.05));
   public final BooleanSetting sunAndMoon = this.add(new BooleanSetting("Sun & Moon", false));
   public final BooleanSetting hideClouds = this.add(new BooleanSetting("Hide Clouds", true));
   public final BooleanSetting fogBlend = this.add(new BooleanSetting("Blend Fog", true));

   public CustomSkyModule() {
      super("Custom Sky", "Animated skies: galaxy, aurora, black hole, ringed planet, blood moon, synthwave and more.", Category.VISUALS);
      instance = this;
   }

   public static boolean hidesClouds() {
      return instance != null && instance.isEnabled() && instance.hideClouds.isOn();
   }

   public int skyIndex() {
      for (int i = 0; i < SKIES.length; i++) {
         if (SKIES[i].equalsIgnoreCase(this.sky.get())) {
            return i;
         }
      }

      return 0;
   }

   public float[] horizon() {
      float[] h = HORIZON[this.skyIndex()];
      float b = this.brightness.getFloat();
      return new float[]{Math.min(1.0F, h[0] * b), Math.min(1.0F, h[1] * b), Math.min(1.0F, h[2] * b)};
   }
}
