package dev.byteplus.module.modules.visuals;

import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.render.Render2D;
import dev.byteplus.setting.BooleanSetting;
import dev.byteplus.setting.ColorSetting;
import dev.byteplus.setting.ModeSetting;
import dev.byteplus.setting.NumberSetting;
import dev.byteplus.util.Anim;

public class CustomCrosshairModule extends Module {
   public final ModeSetting style = this.add(new ModeSetting("Style", "Cross", "Cross", "Cross + Dot", "Dot", "Circle", "T", "Chevron"));
   public final ColorSetting color = this.add(new ColorSetting("Color", -1));
   public final NumberSetting size = this.add(new NumberSetting("Size", 4.0, 1.0, 12.0, 0.5));
   public final NumberSetting gap = this.add(new NumberSetting("Gap", 2.0, 0.0, 8.0, 0.5));
   public final NumberSetting thickness = this.add(new NumberSetting("Thickness", 1.0, 0.5, 3.0, 0.25));
   public final BooleanSetting outline = this.add(new BooleanSetting("Outline", true));
   public final BooleanSetting dynamic = this.add(new BooleanSetting("Dynamic", true));
   public final BooleanSetting hitMarker = this.add(new BooleanSetting("Hit Marker", true));
   private float spread;
   private static long lastHit;

   public CustomCrosshairModule() {
      super("Custom Crosshair", "Your own crosshair: styles, colour, size, gap, outline and a hit marker.", Category.VISUALS);
   }

   @Override
   public boolean showInArrayList() {
      return false;
   }

   public static void onHit() {
      lastHit = System.currentTimeMillis();
   }

   @Override
   public void onRenderOverlay(float sw, float sh, float dt) {
      if (mc.field_1724 != null && mc.field_1690.method_31044().method_31034()) {
         float cx = sw / 2.0F;
         float cy = sh / 2.0F;
         float target = 0.0F;
         if (this.dynamic.isOn()) {
            double speed = mc.field_1724.method_18798().method_37267();
            target = (float)Math.min(4.0, speed * 18.0) + (1.0F - mc.field_1724.method_7261(0.0F)) * 3.0F;
         }

         this.spread = Anim.approach(this.spread, target, 14.0F, dt);
         int c = this.color.argb();
         float s = this.size.getFloat();
         float g = this.gap.getFloat() + this.spread;
         float t = this.thickness.getFloat();
         int ol = -1073741824;
         float ow = t + 1.2F;
         switch ((String)this.style.get()) {
            case "Dot":
               this.dot(cx, cy, Math.max(1.0F, t * 1.2F), c, ol);
               break;
            case "Circle":
               if (this.outline.isOn()) {
                  Render2D.circleOutline(cx, cy, s + g * 0.5F, ow, ol);
               }

               Render2D.circleOutline(cx, cy, s + g * 0.5F, t, c);
               this.dot(cx, cy, t * 0.7F, c, ol);
               break;
            case "T":
               this.arms(cx, cy, s, g, t, ow, c, ol, false, true, true, true);
               break;
            case "Chevron":
               float a = s + g;
               if (this.outline.isOn()) {
                  Render2D.line(cx - a, cy + a * 0.6F, cx, cy, ow, ol);
                  Render2D.line(cx, cy, cx + a, cy + a * 0.6F, ow, ol);
               }

               Render2D.line(cx - a, cy + a * 0.6F, cx, cy, t, c);
               Render2D.line(cx, cy, cx + a, cy + a * 0.6F, t, c);
               break;
            case "Cross + Dot":
               this.arms(cx, cy, s, g, t, ow, c, ol, true, true, true, true);
               this.dot(cx, cy, t * 0.8F, c, ol);
               break;
            default:
               this.arms(cx, cy, s, g, t, ow, c, ol, true, true, true, true);
         }

         if (this.hitMarker.isOn()) {
            float age = (float)(System.currentTimeMillis() - lastHit) / 1000.0F;
            if (age < 0.35F) {
               float a = 1.0F - age / 0.35F;
               float in = 3.0F + g;
               float out = 7.0F + g + (1.0F - a) * 2.0F;
               int hc = Render2D.withAlpha(-1, a);

               for (int i = 0; i < 4; i++) {
                  float dx = (i & 1) == 0 ? -1.0F : 1.0F;
                  float dy = (i & 2) == 0 ? -1.0F : 1.0F;
                  Render2D.line(cx + dx * in, cy + dy * in, cx + dx * out, cy + dy * out, 1.4F, hc);
               }
            }
         }
      }
   }

   private void arms(float cx, float cy, float s, float g, float t, float ow, int c, int ol, boolean up, boolean down, boolean left, boolean right) {
      float[][] seg = new float[][]{
         {0.0F, -1.0F, up ? 1.0F : 0.0F}, {0.0F, 1.0F, down ? 1.0F : 0.0F}, {-1.0F, 0.0F, left ? 1.0F : 0.0F}, {1.0F, 0.0F, right ? 1.0F : 0.0F}
      };
      if (this.outline.isOn()) {
         for (float[] d : seg) {
            if (d[2] > 0.0F) {
               Render2D.line(cx + d[0] * g, cy + d[1] * g, cx + d[0] * (g + s), cy + d[1] * (g + s), ow, ol);
            }
         }
      }

      for (float[] d : seg) {
         if (d[2] > 0.0F) {
            Render2D.line(cx + d[0] * g, cy + d[1] * g, cx + d[0] * (g + s), cy + d[1] * (g + s), t, c);
         }
      }
   }

   private void dot(float cx, float cy, float r, int c, int ol) {
      if (this.outline.isOn()) {
         Render2D.circle(cx, cy, r + 0.6F, ol);
      }

      Render2D.circle(cx, cy, r, c);
   }
}
